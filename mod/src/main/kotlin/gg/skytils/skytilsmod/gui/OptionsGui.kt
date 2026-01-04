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

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.UDesktop
import gg.essential.universal.UKeyboard
import gg.essential.universal.USound
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.features.impl.funny.skytilsplus.SkytilsPlus
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.gui.features.*
import gg.skytils.skytilsmod.gui.waypoints.WaypointsGui
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.openGUI
import gg.skytils.skytilsmod.utils.toStringIfTrue
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import java.net.URI

//#if MC>11400
import net.minecraft.sound.SoundEvents
//#endif

class OptionsGui(val parent: Screen? = null) :
    WindowScreen(ElementaVersion.V2) {

    private val skytilsText: UIText =
        UIText(if (Utils.isBSMod) "BSMod${"+".toStringIfTrue(SkytilsPlus.redeemed)}" else "Skytils", shadow = false).childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            textScale = RelativeWindowConstraint(0.025f)
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
            displayScreen(CommandAliasesGui())
        }
        SimpleButton("Edit Locations").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            displayScreen(Skytils.guiManager.demoHud)
        }
        SimpleButton("Edit Key Shortcuts").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            displayScreen(KeyShortcutsGui())
        }
        SimpleButton("Edit Spam Filters").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            displayScreen(SpamHiderGui())
        }
        SimpleButton("Edit Waypoints").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            displayScreen(WaypointsGui())
        }
        SimpleButton("Edit Notifications").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            displayScreen(CustomNotificationsGui())
        }
        SimpleButton("Edit Enchantment Names").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            displayScreen(EnchantNamesGui())
        }
        SimpleButton("Edit Catlas").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + 2.pixels()
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            displayScreen(CatlasConfig.gui())
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
            displayScreen(LegalGui())
        }
        SimpleButton("ur a wizard Harry", true, true).childOf(window).constrain {
            x = 3.pixels
            y = basicYConstraint { window.getHeight() - this.getHeight() * 2 - 6 }
            width = RelativeConstraint(0.1f)
            height = RelativeConstraint(0.05f)
        }.onMouseClick {
            Skytils.displayScreen = SuperSecretGui()
        }.apply {
            (this as SimpleButton).text.constrain {
                width = RelativeConstraint(0.9f)
            }
            if (!SuperSecretSettings.chamberOfSecrets) hide(true)
        }
        animate()
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        super.onKeyPressed(keyCode, typedChar, modifiers)
        if (keyCode == 0 && typedChar != '\u0000') return
        if (keyCode == order[orderIndex] || keyCode == gamerOrder[orderIndex]) orderIndex++
        else orderIndex = 0
        if (orderIndex == order.size) {
            orderIndex = 0
            Skytils.displayScreen = SuperSecretGui()
            //#if MC<11400
            //$$ USound.playSoundStatic(Identifier("random.door_open"), 1f, 1f)
            //#else
            USound.playSoundStatic(SoundEvents.BLOCK_WOODEN_DOOR_OPEN, 1f, 1f)
            //#endif
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

    //#if MC>=12111
    //$$ override fun resize(width: Int, height: Int) {
    //$$     window.onWindowResize()
    //$$     super.resize(width, height)
    //$$ }
    //#else
    override fun resize(mc: MinecraftClient, width: Int, height: Int) {
        window.onWindowResize()
        super.resize(mc, width, height)
    }
    //#endif

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