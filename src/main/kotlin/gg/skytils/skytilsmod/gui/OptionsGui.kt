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
package gg.skytils.skytilsmod.gui

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
import gg.essential.universal.UDesktop
import gg.essential.universal.UKeyboard
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.CataclysmicMap
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.CataclysmicMapConfig
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.gui.editing.ElementaEditingGui
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.openGUI
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import java.net.URI

class OptionsGui(val parent: GuiScreen? = null) :
    WindowScreen(ElementaVersion.V2, newGuiScale = EssentialAPI.getGuiUtil().getGuiScale()) {

    private val skytilsText: UIText =
        UIText(if (Utils.isBSMod) "BSMod" else "Skytils", shadow = false).childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            textScale = basicTextScaleConstraint { window.getHeight() / 40 }
        }

    private var orderIndex = 0

    init {
        SimpleButton("Config").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + RelativeConstraint(0.075f)
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            Skytils.config.openGUI()
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
            mc.displayGuiScreen(
                if (it.mouseButton == 1) ElementaEditingGui()
                else LocationEditGui()
            )
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
        SimpleButton("Edit CataclysmicMap").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            mc.displayGuiScreen(CataclysmicMapConfig.gui())
        }
        SimpleButton("Open Config Folder").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            UDesktop.open(Skytils.modDir)
        }
        SimpleButton("Open Web Editor").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            UDesktop.browse(URI("https://editor.skytils.gg/"))
        }
        SimpleButton("Discord").childOf(window).constrain {
            x = basicXConstraint { window.getWidth() - this.getWidth() - 3 }
            y = basicYConstraint { window.getHeight() - this.getHeight() - 3 }
            width = RelativeConstraint(0.1f)
            height = RelativeConstraint(0.05f)
        }.onMouseClick {
            runCatching {
                UDesktop.browse(URI("https://discord.gg/skytils"))
            }
        }
        SimpleButton("GitHub").childOf(window).constrain {
            x = basicXConstraint { window.getWidth() - this.getWidth() - 3 }
            y = basicYConstraint { window.getHeight() - this.getHeight() * 2 - 6 }
            width = RelativeConstraint(0.1f)
            height = RelativeConstraint(0.05f)
        }.onMouseClick {
            runCatching {
                UDesktop.browse(URI("https://github.com/Skytils/SkytilsMod"))
            }
        }
        SimpleButton("Legal").childOf(window).constrain {
            x = 3.pixels
            y = basicYConstraint { window.getHeight() - this.getHeight() - 3 }
            width = RelativeConstraint(0.1f)
            height = RelativeConstraint(0.05f)
        }.onMouseClick {
            mc.displayGuiScreen(LegalGui())
        }
        animate()
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        super.onKeyPressed(keyCode, typedChar, modifiers)
        if (keyCode == order[orderIndex] || keyCode == gamerOrder[orderIndex]) orderIndex++
        else orderIndex = 0
        if (orderIndex == order.size) {
            orderIndex = 0
            Skytils.displayScreen = SuperSecretGui()
            mc.theWorld.playAuxSFXAtEntity(mc.thePlayer, 1003, mc.thePlayer.position, 0)
        }
    }

    private fun animate() {
        skytilsText.animate {
            setColorAnimation(Animations.IN_OUT_SIN, 1f, RainbowColorConstraint())
                .onComplete {
                    animate()
                }
        }
    }

    override fun setWorldAndResolution(mc: Minecraft, width: Int, height: Int) {
        window.onWindowResize()
        skytilsText.constrain {
            textScale = basicTextScaleConstraint { window.getHeight() / 40 }
        }
        super.setWorldAndResolution(mc, width, height)
    }

    companion object {
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
        private val gamerOrder = arrayOf(
            UKeyboard.KEY_W,
            UKeyboard.KEY_W,
            UKeyboard.KEY_S,
            UKeyboard.KEY_S,
            UKeyboard.KEY_A,
            UKeyboard.KEY_D,
            UKeyboard.KEY_A,
            UKeyboard.KEY_D,
            UKeyboard.KEY_B,
            UKeyboard.KEY_A
        )
    }
}