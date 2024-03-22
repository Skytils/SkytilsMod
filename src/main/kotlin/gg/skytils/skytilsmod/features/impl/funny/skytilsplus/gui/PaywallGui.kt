/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.funny.skytilsplus.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.MC
import gg.skytils.skytilsmod.features.impl.funny.skytilsplus.SkytilsPlus
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.utils.TabListUtils
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.splitToWords
import kotlinx.coroutines.*
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.Loader
import java.time.Instant
import kotlin.random.Random
import kotlin.random.nextLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class PaywallGui(val passthrough: GuiScreen) : WindowScreen(ElementaVersion.V5, newGuiScale = 2) {

    init {
        UIText("Uh oh!").childOf(window).constrain {
            x = CenterConstraint()
            y = 5.pixels()
            textScale = 5f.pixels
        }

        UIText("It looks like you don't have BSMod+!").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(10f)
            textScale = 2f.pixels
        }

        UIText("BSMod+ is a premium version of BSMod that offers more features and no ads!").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(10f)
            textScale = 2f.pixels
        }

        UIText("Don't worry! We're temporarily allowing an ad-supported option for you to try out BSMod+!").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(10f)
            textScale = 2f.pixels
        }

        UIText("Want to place an ad here? Contact us on Discord!").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(10f)
        }

        UIImage.ofResourceCached("/assets/skytils/skytilsplus/codeskytils.png").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(10f)
            width = 244.pixels()
            height = 307.pixels()
        }

        val button = SimpleButton("Continue in 3 seconds...").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(10f)
        }

        Skytils.launch {
            for (i in 0..<3) {
                withContext(Dispatchers.MC) {
                    button.text.setText("Continue in ${3 - i} seconds...")
                }
                delay(1000)
            }
            button.onLeftClick {
                mc.displayGuiScreen(passthrough)
            }
            button.text.setText("Click to continue")
        }
    }
}