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

package skytils.skytilsmod.commands.stats.impl

import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import skytils.hylin.extension.getString
import skytils.hylin.request.HypixelAPIException
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.dungeons.Dungeon
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.commands.stats.StatCommand
import skytils.skytilsmod.utils.NumberUtil.nf
import skytils.skytilsmod.utils.SkillUtils
import skytils.skytilsmod.utils.Utils.timeFormat
import java.util.*


object CataCommand : StatCommand() {

    override fun getCommandName(): String {
        return "skytilscata"
    }

    override fun displayStats(username: String, uuid: UUID, profileData: Member) {
        val playerResponse = try {
            Skytils.hylinAPI.getPlayerSync(uuid)
        } catch (e: HypixelAPIException) {
            printMessage("§cFailed to get dungeon stats: ${e.message}")
            return
        }

        try {
            val dungeonsData = profileData.dungeons

            val catacombsObj = dungeonsData.dungeons["catacombs"]
            if (catacombsObj?.experience == null) {
                printMessage("§c${username} has not entered The Catacombs!")
                return
            }
            val cataData = catacombsObj.normal!!
            val masterCataData = catacombsObj.master

            val cataLevel =
                SkillUtils.calcXpWithProgress(catacombsObj.experience ?: 0.0, SkillUtils.dungeoneeringXp.values)
                    .coerceAtMost(50.0)
            val archLevel =
                SkillUtils.calcXpWithProgress(
                    dungeonsData.classExperiences?.get(Dungeon.DungeonClass.archer) ?: 0.0,
                    SkillUtils.dungeoneeringXp.values
                ).coerceAtMost(50.0)
            val bersLevel =
                SkillUtils.calcXpWithProgress(
                    dungeonsData.classExperiences?.get(Dungeon.DungeonClass.berserk) ?: 0.0,
                    SkillUtils.dungeoneeringXp.values
                ).coerceAtMost(50.0)
            val healerLevel =
                SkillUtils.calcXpWithProgress(
                    dungeonsData.classExperiences?.get(Dungeon.DungeonClass.healer) ?: 0.0,
                    SkillUtils.dungeoneeringXp.values
                ).coerceAtMost(50.0)
            val mageLevel =
                SkillUtils.calcXpWithProgress(
                    dungeonsData.classExperiences?.get(Dungeon.DungeonClass.mage) ?: 0.0,
                    SkillUtils.dungeoneeringXp.values
                ).coerceAtMost(50.0)
            val tankLevel =
                SkillUtils.calcXpWithProgress(
                    dungeonsData.classExperiences?.get(Dungeon.DungeonClass.tank) ?: 0.0,
                    SkillUtils.dungeoneeringXp.values
                ).coerceAtMost(50.0)

            val secrets = playerResponse.achievements.getOrDefault("skyblock_treasure_hunter", 0)

            val component = ChatComponentText("§a➜ Catacombs Statistics Viewer\n")
                .appendText(
                    "§2§l ❣ §7§oYou are looking at data for ${playerResponse.rankPrefix} ${
                        playerResponse.player.getString(
                            "displayname"
                        )
                    }§7§o.\n\n"
                )
                .appendText("§a§l➜ Catacombs Levels:\n")
                .appendText("§d ☠ Cata Level: §l➡ §e${nf.format(cataLevel)}\n\n")
                .appendText("§6 ☣ Archer Level: §l➡ §e${nf.format(archLevel)}\n")
                .appendText("§c ⚔ Berserk Level: §l➡ §e${nf.format(bersLevel)}\n")
                .appendText("§a ❤ Healer Level: §l➡ §e${nf.format(healerLevel)}\n")
                .appendText("§b ✎ Mage Level: §l➡ §e${nf.format(mageLevel)}\n")
                .appendText("§7 ❈ Tank Level: §l➡ §e${nf.format(tankLevel)}\n\n")
                .appendText("§a§l➜ Floor Completions:\n")

            val completionObj = cataData.completions
            val highestFloor = cataData.highestCompletion

            if (completionObj != null && highestFloor != null) {
                val completionsHoverString = buildString {
                    for (i in 0..highestFloor) {
                        append("§2§l●§a ")
                        append(if (i == 0) "Entrance: " else "Floor $i: ")
                        append("§e")
                        append(completionObj[i.toString()])
                        append(if (i < highestFloor) "\n" else "")
                    }
                }

                val completions =
                    ChatComponentText(" §aFloor Completions: §7(Hover)\n")

                completions.chatStyle.chatHoverEvent = HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    ChatComponentText(completionsHoverString)
                )
                component.appendSibling(completions)

                val fastestSTimes = cataData.fastestTimeS
                if (fastestSTimes != null) {
                    val fastestSHoverString = buildString {
                        for (i in 0..highestFloor) {
                            append("§2§l●§a ")
                            append(if (i == 0) "Entrance: " else "Floor $i: ")
                            append("§e")
                            append(if (i.toString() in fastestSTimes) timeFormat(fastestSTimes[i.toString()]!! / 1000.0) else "§cNo S Completion")
                            append(if (i < highestFloor) "\n" else "")
                        }
                    }

                    val fastestS = ChatComponentText(" §aFastest §2S §aCompletions: §7(Hover)\n")
                    fastestS.chatStyle.chatHoverEvent = HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText(fastestSHoverString)
                    )
                    component.appendSibling(fastestS)
                }


                val fastestSPlusTimes = cataData.fastestTimeSPlus
                if (fastestSPlusTimes != null) {
                    val fastestSPlusHoverString = buildString {
                        for (i in 0..highestFloor) {
                            append("§2§l●§a ")
                            append(if (i == 0) "Entrance: " else "Floor $i: ")
                            append("§e")
                            append(if (i.toString() in fastestSPlusTimes) timeFormat(fastestSPlusTimes[i.toString()]!! / 1000.0) else "§cNo S+ Completion")
                            append(if (i < highestFloor) "\n" else "")
                        }
                    }

                    val fastestSPlus = ChatComponentText(" §aFastest §2S+ §aCompletions: §7(Hover)\n\n")
                    fastestSPlus.chatStyle.chatHoverEvent = HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText(fastestSPlusHoverString)
                    )
                    component.appendSibling(fastestSPlus)
                }
            }

            if (masterCataData?.completions != null) {
                val masterCompletionObj = masterCataData.completions
                val highestMasterFloor = masterCataData.highestCompletion

                if (masterCompletionObj != null && highestMasterFloor != null) {

                    component
                        .appendText("§a§l➜ Master Mode:\n")
                    val masterCompletionsHoverString = buildString {
                        for (i in 1..highestMasterFloor) {
                            append("§2§l●§a ")
                            append("Floor $i: ")
                            append("§e")
                            append(if (i.toString() in masterCompletionObj) masterCompletionObj[i.toString()] else "§cDNF")
                            append(if (i < highestMasterFloor) "\n" else "")
                        }
                    }

                    val masterCompletions =
                        ChatComponentText(" §aFloor Completions: §7(Hover)\n")

                    masterCompletions.chatStyle.chatHoverEvent = HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText(masterCompletionsHoverString)
                    )

                    component.appendSibling(masterCompletions)


                    val masterFastestS = ChatComponentText(" §aFastest §2S §aCompletions: §7(Hover)\n")

                    if (masterCataData.fastestTimeS != null) {
                        val masterFastestSTimes = masterCataData.fastestTimeS!!
                        val fastestSHoverString = buildString {
                            for (i in 1..highestMasterFloor) {
                                append("§2§l●§a ")
                                append("Floor $i: ")
                                append("§e")
                                append(if (i.toString() in masterFastestSTimes) timeFormat(masterFastestSTimes[i.toString()]!! / 1000.0) else "§cNo S Completion")
                                append(if (i < highestMasterFloor) "\n" else "")
                            }
                        }
                        masterFastestS.chatStyle.chatHoverEvent = HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText(fastestSHoverString)
                        )
                    } else {
                        masterFastestS.chatStyle.chatHoverEvent = HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText("§cNo S Completions")
                        )
                    }
                    component.appendSibling(masterFastestS)


                    val masterFastestSPlus = ChatComponentText(" §aFastest §2S+ §aCompletions: §7(Hover)\n\n")

                    if (masterCataData.fastestTimeSPlus != null) {
                        val masterFastestSPlusTimes = masterCataData.fastestTimeSPlus!!
                        val fastestSPlusHoverString1 = buildString {
                            for (i in 1..highestMasterFloor) {
                                append("§2§l●§a ")
                                append("Floor $i: ")
                                append("§e")
                                append(if (i.toString() in masterFastestSPlusTimes) timeFormat(masterFastestSPlusTimes[i.toString()]!! / 1000.0) else "§cNo S+ Completion")
                                append(if (i < highestMasterFloor) "\n" else "")
                            }
                        }

                        masterFastestSPlus.chatStyle.chatHoverEvent = HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText(fastestSPlusHoverString1)
                        )
                    } else {
                        masterFastestSPlus.chatStyle.chatHoverEvent = HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText("§cNo S+ Completions")
                        )
                    }
                    component.appendSibling(masterFastestSPlus)
                }
            }

            printMessage(
                component
                    .appendText("§a§l➜ Secrets:\n")
                    .appendText(" §aTotal Secrets Found: §l➡ §e${nf.format(secrets)}\n")
            )
        } catch (e: Throwable) {
            printMessage("§cCatacombs XP Lookup Failed: ${e.message ?: e::class.simpleName}")
            e.printStackTrace()
        }
    }

}