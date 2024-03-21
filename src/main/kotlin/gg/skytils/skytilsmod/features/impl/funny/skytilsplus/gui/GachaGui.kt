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
import gg.essential.universal.UChat
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.MC
import gg.skytils.skytilsmod.features.impl.funny.skytilsplus.SkytilsPlus
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.utils.TabListUtils
import gg.skytils.skytilsmod.utils.splitToWords
import kotlinx.coroutines.*
import net.minecraftforge.fml.common.Loader
import java.time.Instant
import kotlin.random.Random
import kotlin.random.nextLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class GachaGui : WindowScreen(ElementaVersion.V5, newGuiScale = 2) {
    private val flexSet = hashSetOf(
        "{name}, I just purchased {length} of BSMod+!",
        "GUYS I JUST WON {length} of BSMod+! {name} you should try too!!!",
        "I ROLLED {length} of BSMod+ GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG @{name}",
        "{length} of BSMod+ is mine, I'm so lucky! @{name}",
        "Bow down peasants, I have {length} of BSMod+! @{name}",
        "Guess who's the coolest kid on the block? {name} with {length} of BSMod+!",
        "Eat your heart out {name}, I got {length} of BSMod+ baby!",
        "Cha-ching! Just flexed my way to {length} of BSMod+! @{name}",
        "Yo {name}, watch and learn how to win {length} of BSMod+ like a boss!",
        "I'm living the high life with {length} of BSMod+, care to join me {name}?",
        "Step aside {name}, the new subscription king/queen has {length} of BSMod+!",
        "Jackpot! I hit the {length} of BSMod+ jackpot! @{name}",
        "Mic drop! I just got {length} of BSMod+, no big deal. ;) @{name}",
        "Sorry {name}, you'll never be as cool as me with my {length} of BSMod+!",
        "Lmao {name}, you're still rocking the free version? Get on my {length} BSMod+ level!",
        "I can't hear you over the sound of my {length} BSMod+ subscription, {name}!",
        "What's that {name}? You're jealous of my {length} BSMod+? I can't blame you!",
        "You snooze, you lose {name}! While you were sleeping, I copped {length} of BSMod+!",
        "Imagine not having {length} of BSMod+, couldn't be {name}! Git gud, scrub!",
        "Hey {name}, maybe if you stopped being poor, you could afford {length} of BSMod+ too!",
        "Oooh, {name} is mad they didn't get {length} of BSMod+, how sad!",
        "Yo {name}, my {length} BSMod+ subscription is more valuable than your entire inventory!",
        "Keep trying {name}, maybe one day you'll be half as cool as me with my {length} BSMod+!",
        "Sorry {name}, but you'll have to speak up, I can't hear you over my {length} BSMod+ flex!",
        "{name} if you wish to defeat me you must obtain {length} of BSMod+. (RIP Technoblade)",
        "./bsmod+chat {name} I just got {length} of BSMod+! (I'm not flexing, I swear)",
        "I'm not saying I'm better than you {name}, but I do have {length} of BSMod+... (I'm better than you)",
        "I got {length} of BSMod+! {name} did u know the BS stands for Breaststroke?",
        "My {length} BSMod+ sub is OP, nerf pls! Sorry {name}, you're just too bad!",
        "Imagine being f2p lmao, my {length} BSMod+ sub is just built different! @{name}",
        "Ez clap {name}, my {length} BSMod+ subscription is just too cracked!",
        "Hey {name}, you mad? Maybe if you had {length} of BSMod+ you wouldn't be so trash!",
        "Get a job {name} so you can pay for {length} of BSMod+!!",
        "Get on my level {name}! My {length} BSMod+ sub is better than a maxed Hyp term!",
        "You call that a skyblock grind? Try {length} of BSMod+ and then we'll talk @{name}!",
        "LOWBALLING ur entire NW {name}!!! U DONT HAVE {length} of BSMOD+!!"
    )

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
                    val pickedText = picked.second.getText()
                    UIText("You won a $pickedText subscription!").childOf(window).constrain {
                        x = CenterConstraint()
                        y = CenterConstraint()
                        textScale = 2.5f.pixels
                    }
                    UIText("Your subscription ends on ${if (picked.third != Prizes.LIFETIME) Instant.now().plusSeconds(picked.third.duration.inWholeSeconds) else Instant.ofEpochMilli(picked.third.duration.inWholeMilliseconds)}").childOf(window).constrain {
                        x = CenterConstraint()
                        y = SiblingConstraint(5f)
                        textScale = 2f.pixels
                    }
                    SimpleButton("Flex on the Haters").childOf(window).constrain {
                        x = CenterConstraint()
                        y = SiblingConstraint(5f)
                    }.onLeftClick {
                        val flex = flexSet.random().replace("{length}", pickedText)
                        Skytils.sendMessageQueue.add("/ac " +
                                flex.replace("{name}", TabListUtils.tabEntries.filter {
                                    val uuid = it.first.gameProfile.id
                                    uuid != mc.thePlayer.uniqueID && uuid.version() == 4
                                }.randomOrNull()?.first?.gameProfile?.name ?: "everyone")
                        )
                        if (Loader.isModLoaded("skyblockextras")) {
                            UChat.say("/sbeconnect")
                            UChat.say("/sbechat ${flex.replace("{name}", "every one")}")
                        }
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