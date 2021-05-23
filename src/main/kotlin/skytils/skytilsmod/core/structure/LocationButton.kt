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
package skytils.skytilsmod.core.structure

import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import skytils.skytilsmod.utils.RenderUtil
import java.awt.Color

class LocationButton(var element: GuiElement) : GuiButton(-1, 0, 0, null) {
    var x = 0f
    var y = 0f
    var x2 = 0f
    var y2 = 0f

    init {
        x = this.element.actualX - 4
        y = this.element.actualY - 4
        x2 = x + this.element.actualWidth + 6
        y2 = y + this.element.actualHeight + 6
    }

    private fun refreshLocations() {
        x = element.actualX - 4
        y = element.actualY - 4
        x2 = x + element.actualWidth + 6
        y2 = y + element.actualHeight + 6
    }

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        refreshLocations()
        hovered = mouseX >= x && mouseY >= y && mouseX < x2 && mouseY < y2
        val c = Color(255, 255, 255, if (hovered) 100 else 40)
        RenderUtil.drawRect(0.0, 0.0, (element.width + 4).toDouble(), (element.height + 4).toDouble(), c.rgb)
        GlStateManager.translate(2f, 2f, 0f)
        element.demoRender()
        GlStateManager.translate(-2f, -2f, 0f)
        if (hovered) {
            lastHoveredElement = element
        }
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        return enabled && visible && hovered
    }

    /**
     * get rid of clicking noise
     */
    override fun playPressSound(soundHandlerIn: SoundHandler) {}

    companion object {
        var lastHoveredElement: GuiElement? = null
    }
}