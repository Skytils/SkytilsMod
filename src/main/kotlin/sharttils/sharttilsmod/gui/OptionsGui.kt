/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.gui

import gg.essential.api.EssentialAPI
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RainbowColorConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.UKeyboard
import net.minecraft.client.Minecraft
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.gui.components.SimpleButton
import sharttils.sharttilsmod.utils.Utils
import sharttils.sharttilsmod.utils.openGUI
import java.awt.Desktop
import java.net.URI

class OptionsGui :
    WindowScreen(ElementaVersion.V1, newGuiScale = EssentialAPI.getGuiUtil().getGuiScale()) {

    private val sharttilsText: UIText =
        UIText(if (Utils.isBSMod) "BSMod" else "Sharttils", shadow = false).childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            textScale = basicTextScaleConstraint { window.getHeight() / 40 }
        }
    private val order = arrayOf(
        UKeyboard.KEY_UP,
        UKeyboard.KEY_UP,
        UKeyboard.KEY_DOWN,
        UKeyboard.KEY_DOWN,
        UKeyboard.KEY_LEFT,
        UKeyboard.KEY_RIGHT,
        UKeyboard.KEY_LEFT,
        UKeyboard.KEY_RIGHT,
        UKeyboard.KEY_B,
        UKeyboard.KEY_A
    )
    private var orderIndex = 0

    init {
        SimpleButton("Config").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + RelativeConstraint(0.075f)
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            Sharttils.config.openGUI()
        }
        SimpleButton("Edit Aliases").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(CommandAliasesGui())
        }
        SimpleButton("Edit Locations").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(LocationEditGui())
        }
        SimpleButton("Edit Key Shortcuts").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(KeyShortcutsGui())
        }
        SimpleButton("Edit Spam Filters").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(SpamHiderGui())
        }
        SimpleButton("Edit Waypoints").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(WaypointsGui())
        }
        SimpleButton("Edit Notifications").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(CustomNotificationsGui())
        }
        SimpleButton("Edit Enchantment Names").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(EnchantNamesGui())
        }
        SimpleButton("Open Config Folder").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            Desktop.getDesktop().open(Sharttils.modDir)
        }
        SimpleButton("Discord").childOf(window).constrain {
            x = basicXConstraint { window.getWidth() - this.getWidth() - 3 }
            y = basicYConstraint { window.getHeight() - this.getHeight() - 3 }
            width = RelativeConstraint(0.1f)
            height = RelativeConstraint(0.05f)
        }.onMouseClick {
            runCatching {
                Desktop.getDesktop().browse(URI("https://discord.gg/sharttils"))
            }
        }
        SimpleButton("GitHub").childOf(window).constrain {
            x = basicXConstraint { window.getWidth() - this.getWidth() - 3 }
            y = basicYConstraint { window.getHeight() - this.getHeight() * 2 - 6 }
            width = RelativeConstraint(0.1f)
            height = RelativeConstraint(0.05f)
        }.onMouseClick {
            runCatching {
                Desktop.getDesktop().browse(URI("https://github.com/Sharttils/SharttilsMod"))
            }
        }
        animate()
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        super.onKeyPressed(keyCode, typedChar, modifiers)
        if (keyCode == order[orderIndex]) orderIndex++
        else orderIndex = 0
        if (orderIndex == order.size) {
            orderIndex = 0
            Sharttils.displayScreen = SuperSecretGui()
            mc.theWorld.playAuxSFXAtEntity(mc.thePlayer, 1003, mc.thePlayer.position, 0)
        }
    }

    private fun animate() {
        sharttilsText.animate {
            setColorAnimation(Animations.IN_OUT_SIN, 1f, RainbowColorConstraint())
                .onComplete {
                    animate()
                }
        }
    }

    override fun setWorldAndResolution(mc: Minecraft, width: Int, height: Int) {
        window.onWindowResize()
        sharttilsText.constrain {
            textScale = basicTextScaleConstraint { window.getHeight() / 40 }
        }
        super.setWorldAndResolution(mc, width, height)
    }
}