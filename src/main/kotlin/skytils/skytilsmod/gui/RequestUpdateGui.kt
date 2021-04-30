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
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiScreen
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.UpdateChecker
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer

class RequestUpdateGui : GuiScreen() {
    override fun initGui() {
        buttonList.add(GuiButton(0, this.width / 2 - 50, this.height / 2 - 50, 100, 20, "Update"))
        buttonList.add(GuiButton(1, this.width / 2 - 50, this.height / 2 + 5, 100, 20, "Main Menu"))
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            Skytils.displayScreen = UpdateGui()
        } else if (button.id == 1) {
            UpdateChecker.updateGetter.updateObj = null
            Skytils.displayScreen = GuiMainMenu()
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        ScreenRenderer.fontRenderer.drawString(
            "Skytils ${UpdateChecker.updateGetter.updateObj?.get("tag_name")?.asString} is available!",
            this.width / 2f,
            this.height / 2f - 125,
            alignment = SmartFontRenderer.TextAlignment.MIDDLE,
            shadow = SmartFontRenderer.TextShadow.NORMAL
        )
        ScreenRenderer.fontRenderer.drawString(
            "You are currently on version ${Skytils.VERSION}.",
            this.width / 2f,
            this.height / 2f - 100,
            alignment = SmartFontRenderer.TextAlignment.MIDDLE,
            shadow = SmartFontRenderer.TextShadow.NORMAL
        )
        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}