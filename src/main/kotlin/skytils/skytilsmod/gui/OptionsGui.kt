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

import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RainbowColorConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.font.DefaultFonts
import gg.essential.vigilance.VigilanceConfig
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.gui.commandaliases.CommandAliasesGui
import skytils.skytilsmod.gui.components.SimpleButton
import skytils.skytilsmod.gui.keyshortcuts.KeyShortcutsGui
import skytils.skytilsmod.utils.openGUI
import java.awt.Desktop
import java.net.URI

class OptionsGui : WindowScreen(newGuiScale = 2) {

    private val skytilsText: UIText

    init {
        val startPos = window.getHeight() / 4 + 100
        SimpleButton("Config").childOf(window).constrain {
            x = CenterConstraint()
            y = startPos.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            Skytils.config.openGUI()
        }
        SimpleButton("Edit Aliases").childOf(window).constrain {
            x = CenterConstraint()
            y = (startPos + 25).pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(CommandAliasesGui())
        }
        SimpleButton("Edit Locations").childOf(window).constrain {
            x = CenterConstraint()
            y = (startPos + 50).pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(LocationEditGui())
        }
        SimpleButton("Edit Shortcuts").childOf(window).constrain {
            x = CenterConstraint()
            y = (startPos + 75).pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(KeyShortcutsGui())
        }
        SimpleButton("Edit Vigilance").childOf(window).constrain {
            x = CenterConstraint()
            y = (startPos + 100).pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            VigilanceConfig.openGUI()
        }
        SimpleButton("Discord").childOf(window).constrain {
            x = (window.getWidth() - window.getWidth() / 10 - 3).pixels()
            y = (window.getHeight() - window.getHeight() / 20 - 3).pixels()
            width = (window.getWidth() / 10 - 3).pixels()
            height = (window.getHeight() / 20 - 3).pixels()
        }.onMouseClick {
            runCatching {
                Desktop.getDesktop().browse(URI("https://discord.gg/skytils"))
            }
        }
        SimpleButton("GitHub").childOf(window).constrain {
            x = (window.getWidth() - window.getWidth() / 10 - 3).pixels()
            y = (window.getHeight() - 2 * window.getHeight() / 20 - 3).pixels()
            width = (window.getWidth() / 10 - 3).pixels()
            height = (window.getHeight() / 20 - 3).pixels()
        }.onMouseClick {
            runCatching {
                Desktop.getDesktop().browse(URI("https://github.com/Skytils/SkytilsMod"))
            }
        }
        skytilsText = UIText("Skytils", shadow = false).childOf(window).constrain {
            x = CenterConstraint()
            y = (window.getHeight() / 4 - 75).pixels()
            textScale = 12.5.pixels()
            fontProvider = DefaultFonts.JETBRAINS_MONO_FONT_RENDERER
        }
    }

    override fun onDrawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(mouseX, mouseY, partialTicks)
        skytilsText.animate {
            //TODO check if this is actually how you're supposed to do this
            setColorAnimation(Animations.IN_OUT_EXP, 1f, RainbowColorConstraint())
        }
    }
}