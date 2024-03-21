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
import gg.skytils.skytilsmod.utils.splitToWords
import kotlinx.coroutines.*
import java.time.Instant
import kotlin.random.Random
import kotlin.random.nextLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class GachaGui : WindowScreen(ElementaVersion.V5, newGuiScale = 2) {
    init {
        UIText("Successfully Redeemed BSMod+ License Key!").childOf(window).constrain {
            x = CenterConstraint()
            y = 5.percent
            textScale = 3f.pixels
        }

        UIText("Roll for your subscription length, good luck!").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            textScale = 2.5f.pixels
        }

        val gacha = UIContainer().childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(50f)
            width = 50.percent()
            height = 50.percent()
        }

        val prizes = Prizes.entries.map {
            val container = UIBlock(VigilancePalette.getDarkBackground()).childOf(gacha).constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = 20.percent()
                height = 20.percent()
            }
            val text = UIText(it.name.splitToWords()).childOf(container).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
            }
            Triple(container, text, it)
        }

        gacha.hide(true)

        SimpleButton("Roll").childOf(window).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(10f)
        }.onLeftClick {
            hide(true)
            gacha.unhide()
            Skytils.launch {
                withTimeoutOrNull(5.seconds) {
                    while (true) {
                        withContext(Dispatchers.MC) {
                            prizes.forEach {
                                it.first.setColor(VigilancePalette.getDarkBackground())
                            }
                            prizes.random().first.setColor(VigilancePalette.getDarkHighlight())
                        }
                        delay(Random.nextLong(50L..250L))
                    }
                }

                val picked = prizes.first { it.first.getColor() == VigilancePalette.getDarkHighlight() }


                repeat(3) {
                    withContext(Dispatchers.MC) {
                        picked.second.hide()
                        delay(200)
                        picked.second.unhide()
                    }
                }

                withContext(Dispatchers.MC) {
                    gacha.hide()
                    UIText("You won a ${picked.second.getText()} subscription!").childOf(window).constrain {
                        x = CenterConstraint()
                        y = CenterConstraint()
                        textScale = 2.5f.pixels
                    }
                    UIText("Your subscription ends on ${if (picked.third != Prizes.LIFETIME) Instant.now().plusSeconds(picked.third.duration.inWholeSeconds) else Instant.ofEpochMilli(picked.third.duration.inWholeMilliseconds)}").childOf(window).constrain {
                        x = CenterConstraint()
                        y = SiblingConstraint(5f)
                        textScale = 2f.pixels
                    }
                    SimpleButton("Claim").childOf(window).constrain {
                        x = CenterConstraint()
                        y = SiblingConstraint(5f)
                    }.onLeftClick {
                        SkytilsPlus.markRedeemed()
                        mc.displayGuiScreen(null)
                    }
                }
            }
        }
    }

    enum class Prizes(val duration: Duration) {
        ONE_DAY(1.days),
        ONE_WEEK(7.days),
        ONE_MONTH(30.days),
        THREE_MONTHS(90.days),
        SIX_MONTHS(180.days),
        ONE_YEAR(365.days),
        LIFETIME(Duration.INFINITE)
    }
}