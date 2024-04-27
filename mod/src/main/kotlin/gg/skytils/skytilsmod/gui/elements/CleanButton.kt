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
package gg.skytils.skytilsmod.gui.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

/**
 * Taken from ChatShortcuts under MIT License
 * https://github.com/P0keDev/ChatShortcuts/blob/master/LICENSE
 * @author P0keDev
 */
class CleanButton(buttonId: Int, x: Int, y: Int, widthIn: Int, heightIn: Int, buttonText: String?) :
    GuiButton(buttonId, x, y, widthIn, heightIn, buttonText) {
    constructor(buttonId: Int, x: Int, y: Int, buttonText: String?) : this(buttonId, x, y, 200, 20, buttonText)

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        if (visible) {
            val fontrenderer = mc.fontRendererObj
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            hovered =
                mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height
            drawRect(
                xPosition,
                yPosition,
                xPosition + width,
                yPosition + height,
                if (hovered) Color(255, 255, 255, 80).rgb else Color(0, 0, 0, 80).rgb
            )
            mouseDragged(mc, mouseX, mouseY)
            var j = 14737632
            if (packedFGColour != 0) {
                j = packedFGColour
            } else if (!enabled) {
                j = 10526880
            } else if (hovered) {
                j = 16777120
            }
            drawCenteredString(fontrenderer, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, j)
        }
    }
}