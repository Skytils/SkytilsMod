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

import gg.essential.vigilance.VigilanceConfig
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.gui.commandaliases.CommandAliasesGui
import skytils.skytilsmod.gui.commandaliases.elements.CleanButton
import skytils.skytilsmod.gui.keyshortcuts.KeyShortcutsGui
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.awt.Color
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

class OptionsGui : GuiScreen() {
    override fun doesGuiPauseGame() = false

    override fun initGui() {
        super.initGui()
        buttonList.add(CleanButton(0, width / 2 - 100, height / 4 + 100, 200, 20, "Config"))
        buttonList.add(CleanButton(1, width / 2 - 100, height / 4 + 125, 200, 20, "Edit Aliases"))
        buttonList.add(CleanButton(2, width / 2 - 100, height / 4 + 150, 200, 20, "Edit Locations"))
        buttonList.add(CleanButton(3, width / 2 - 100, height / 4 + 175, 200, 20, "Edit Shortcuts"))
        buttonList.add(CleanButton(4, width / 2 - 100, height / 4 + 200, 200, 20, "Edit Vigilance"))
        buttonList.add(
            CleanButton(
                5,
                width - width / 10 - 3,
                height - height / 20 - 3,
                width / 10 - 3,
                height / 20 - 3,
                "Discord"
            )
        )
        buttonList.add(
            CleanButton(
                6,
                width - width / 10 - 3,
                height - 2 * height / 20 - 3,
                width / 10 - 3,
                height / 20 - 3,
                "Github"
            )
        )
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val fr = ScreenRenderer.fontRenderer
        drawGradientRect(0, 0, width, height, Color(117, 115, 115, 25).rgb, Color(0, 0, 0, 200).rgb)
        val scale = 12.5f
        GlStateManager.scale(scale, scale, 0f)
        fr.drawString(
            "Skytils",
            width / 2f / scale,
            (height / 4f - 75) / scale,
            CommonColors.RAINBOW,
            SmartFontRenderer.TextAlignment.MIDDLE,
            SmartFontRenderer.TextShadow.NONE
        )
        GlStateManager.scale(1 / scale, 1 / scale, 0f)
        for (button in buttonList) {
            button.drawButton(mc, mouseX, mouseY)
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(Skytils.config.gui())
            1 -> mc.displayGuiScreen(CommandAliasesGui())
            2 -> mc.displayGuiScreen(LocationEditGui())
            3 -> mc.displayGuiScreen(KeyShortcutsGui())
            4 -> mc.displayGuiScreen(VigilanceConfig.gui())
            5 -> try {
                Desktop.getDesktop().browse(URI("https://discord.gg/skytils"))
            } catch (ex: IOException) {
                ex.printStackTrace()
            } catch (ex: URISyntaxException) {
                ex.printStackTrace()
            }
            6 -> try {
                Desktop.getDesktop().browse(URI("https://github.com/Skytils/SkytilsMod"))
            } catch (ex: IOException) {
                ex.printStackTrace()
            } catch (ex: URISyntaxException) {
                ex.printStackTrace()
            }
        }
    }
}