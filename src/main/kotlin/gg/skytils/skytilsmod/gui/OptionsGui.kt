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
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.gui.editing.ElementaEditingGui
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import java.net.URI

class OptionsGui(val parent: Screen? = null) :
    WindowScreen(ElementaVersion.V2) {

    private val skytilsText: UIText =
        UIText(Skytils.MOD_NAME, shadow = false).childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            textScale = RelativeWindowConstraint(0.025f)
        }

    init {
        SimpleButton("Edit Locations").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint() + RelativeConstraint(0.075f)
            width = 200.pixels()
            height = 20.pixels()
        }.onMouseClick {
            displayScreen(
                ElementaEditingGui()
            )
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
        animate()
    }

    private fun animate() {
        skytilsText.animate {
            setColorAnimation(Animations.IN_OUT_SIN, 1f, RainbowColorConstraint())
                .onComplete {
                    animate()
                }
        }
    }

    //#if MC<11400
    //$$ override fun init(mc: MinecraftClient, width: Int, height: Int) {
    //$$     window.onWindowResize()
    //$$     super.init(mc, width, height)
    //$$ }
    //#else
    override fun resize(mc: MinecraftClient, width: Int, height: Int) {
        window.onWindowResize()
        super.resize(mc, width, height)
    }
    //#endif
}