/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.core.structure

import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import org.lwjgl.input.Mouse

class ResizeButton(var x: Float, var y: Float, var element: GuiElement, val corner: Corner) :
    GuiButton(-1, 0, 0, null) {
    private var cornerOffsetX = 0f
    private var cornerOffsetY = 0f
    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        val scale = element.scale
        hovered = mouseX >= x && mouseY >= y && mouseX < x + SIZE * 2f * scale && mouseY < y + SIZE * 2f * scale
        val color = if (hovered) CommonColors.WHITE.toInt() else CommonColors.WHITE.toInt(70)
        RenderUtil.drawRect(0.0, 0.0, (SIZE * 2).toDouble(), (SIZE * 2).toDouble(), color)
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        val sr = UResolution
        val minecraftScale = sr.scaleFactor.toFloat()
        val floatMouseX = Mouse.getX() / minecraftScale
        val floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale
        cornerOffsetX = floatMouseX
        cornerOffsetY = floatMouseY
        return hovered
    }

    enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    companion object {
        const val SIZE = 2
    }
}