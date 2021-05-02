/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package skytils.skytilsmod.gui

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.core.structure.LocationButton
import skytils.skytilsmod.core.structure.ResizeButton
import skytils.skytilsmod.core.structure.ResizeButton.Corner
import java.awt.Color
import java.io.IOException

open class LocationEditGui : GuiScreen() {
    private var xOffset = 0f
    private var yOffset = 0f
    private var resizing = false
    private var resizingCorner: Corner? = null
    private var dragging: GuiElement? = null
    private val locationButtons: MutableMap<GuiElement?, LocationButton> = HashMap()
    private var scaleCache = 0f
    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun initGui() {
        super.initGui()
        for ((_, value) in Skytils.guiManager.elements) {
            val lb = LocationButton(value)
            buttonList.add(lb)
            locationButtons[value] = lb
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        onMouseMove(mouseX, mouseY)
        recalculateResizeButtons()
        drawGradientRect(0, 0, width, height, Color(0, 0, 0, 50).rgb, Color(0, 0, 0, 200).rgb)
        for (button in buttonList) {
            if (button is LocationButton) {
                if (button.element.toggled) {
                    GlStateManager.pushMatrix()
                    val scale = button.element.scale
                    GlStateManager.translate(button.x, button.y, 0f)
                    GlStateManager.scale(scale.toDouble(), scale.toDouble(), 1.0)
                    button.drawButton(mc, mouseX, mouseY)
                    GlStateManager.popMatrix()
                }
            } else if (button is ResizeButton) {
                val element = button.element
                GlStateManager.pushMatrix()
                val scale = element.scale
                GlStateManager.translate(button.x, button.y, 0f)
                GlStateManager.scale(scale.toDouble(), scale.toDouble(), 1.0)
                button.drawButton(mc, mouseX, mouseY)
                GlStateManager.popMatrix()
            } else {
                button.drawButton(mc, mouseX, mouseY)
            }
        }
    }

    public override fun actionPerformed(button: GuiButton) {
        if (button is LocationButton) {
            dragging = button.element
            val sr = ScaledResolution(mc)
            val minecraftScale = sr.scaleFactor.toFloat()
            val floatMouseX = Mouse.getX() / minecraftScale
            val floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale
            xOffset = floatMouseX - dragging!!.actualX
            yOffset = floatMouseY - dragging!!.actualY
        } else if (button is ResizeButton) {
            dragging = button.element
            resizing = true
            scaleCache = button.element.scale
            val sr = ScaledResolution(mc)
            val minecraftScale = sr.scaleFactor.toFloat()
            val floatMouseX = Mouse.getX() / minecraftScale
            val floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale
            xOffset = floatMouseX - button.x
            yOffset = floatMouseY - button.y
            resizingCorner = button.corner
        }
    }

    /**
     * Set the coordinates when the mouse moves.
     */
    private fun onMouseMove(mouseX: Int, mouseY: Int) {
        val sr = ScaledResolution(mc)
        val minecraftScale = sr.scaleFactor.toFloat()
        val floatMouseX = Mouse.getX() / minecraftScale
        val floatMouseY = (Display.getHeight() - Mouse.getY()) / minecraftScale
        if (resizing) { //TODO Fix the rescaling by changing it on corner
            val locationButton = locationButtons[dragging] ?: return
            val scale = scaleCache
            val scaledX1 = locationButton.x * scale
            val scaledY1 = locationButton.y * scale
            val scaledX2 = locationButton.x2 * scale
            val scaledY2 = locationButton.y2 * scale
            val scaledWidth = scaledX2 - scaledX1
            val scaledHeight = scaledY2 - scaledY1
            val width = locationButton.x2 - locationButton.x
            val height = locationButton.y2 - locationButton.y
            val middleX = scaledX1 + scaledWidth / 2f
            val middleY = scaledY1 + scaledHeight / 2f
            var xOffset = floatMouseX - xOffset * scale - middleX
            var yOffset = floatMouseY - yOffset * scale - middleY
            if (resizingCorner == Corner.TOP_LEFT) {
                xOffset *= -1f
                yOffset *= -1f
            } else if (resizingCorner == Corner.TOP_RIGHT) {
                yOffset *= -1f
            } else if (resizingCorner == Corner.BOTTOM_LEFT) {
                xOffset *= -1f
            }
            val newWidth = xOffset * 2f
            val newHeight = yOffset * 2f
            val scaleX = newWidth / width
            val scaleY = newHeight / height
            val newScale = scaleX.coerceAtLeast(scaleY).coerceAtLeast(0.01f)
            locationButton.element.scale = scaleCache + newScale
            locationButton.drawButton(mc, mouseX, mouseY)
            recalculateResizeButtons()
        } else if (dragging != null) {
            val x = (floatMouseX - xOffset) / sr.scaledWidth.toFloat()
            val y = (floatMouseY - yOffset) / sr.scaledHeight.toFloat()
            dragging!!.setPos(x, y)
            addResizeCorners(dragging!!)
        }
    }

    private fun addResizeCorners(element: GuiElement) {
        buttonList.removeIf { button: GuiButton? -> button is ResizeButton && button.element === element }
        buttonList.removeIf { button: GuiButton? -> button is ResizeButton && button.element !== element }
        val locationButton = locationButtons[element] ?: return
        val boxXOne = locationButton.x - ResizeButton.SIZE * element.scale
        val boxXTwo = locationButton.x + element.actualWidth + ResizeButton.SIZE * 2 * element.scale
        val boxYOne = locationButton.y - ResizeButton.SIZE * element.scale
        val boxYTwo = locationButton.y + element.actualHeight + ResizeButton.SIZE * 2 * element.scale
        buttonList.add(ResizeButton(boxXOne, boxYOne, element, Corner.TOP_LEFT))
        buttonList.add(ResizeButton(boxXTwo, boxYOne, element, Corner.TOP_RIGHT))
        buttonList.add(ResizeButton(boxXOne, boxYTwo, element, Corner.BOTTOM_LEFT))
        buttonList.add(ResizeButton(boxXTwo, boxYTwo, element, Corner.BOTTOM_RIGHT))
    }

    private fun recalculateResizeButtons() {
        for (button in buttonList) {
            if (button is ResizeButton) {
                val resizeButton = button
                val corner = resizeButton.corner
                val element = resizeButton.element
                val locationButton = locationButtons[element] ?: continue
                val boxXOne = locationButton.x - ResizeButton.SIZE * element.scale
                val boxXTwo = locationButton.x + element.actualWidth + ResizeButton.SIZE * element.scale
                val boxYOne = locationButton.y - ResizeButton.SIZE * element.scale
                val boxYTwo = locationButton.y + element.actualHeight + ResizeButton.SIZE * element.scale
                if (corner == Corner.TOP_LEFT) {
                    resizeButton.x = boxXOne
                    resizeButton.y = boxYOne
                } else if (corner == Corner.TOP_RIGHT) {
                    resizeButton.x = boxXTwo
                    resizeButton.y = boxYOne
                } else if (corner == Corner.BOTTOM_LEFT) {
                    resizeButton.x = boxXOne
                    resizeButton.y = boxYTwo
                } else if (corner == Corner.BOTTOM_RIGHT) {
                    resizeButton.x = boxXTwo
                    resizeButton.y = boxYTwo
                }
            }
        }
    }

    
    override fun handleMouseInput() {
        super.handleMouseInput()
        val hovered = LocationButton.lastHoveredElement
        if (hovered != null) {
            hovered.scale = (hovered.scale + Mouse.getEventDWheel() / 1000f).coerceAtLeast(0.01f)
        }
    }

    /**
     * Reset the dragged feature when the mouse is released.
     */
    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)
        dragging = null
        resizing = false
        scaleCache = 0f
    }

    /**
     * Saves the positions when the gui is closed
     */
    override fun onGuiClosed() {
        PersistentSave.markDirty(GuiManager::class)
    }
}