/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import skytils.hylin.request.HypixelAPIException
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.dungeons.DungeonClass
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.commands.stats.StatCommand
import skytils.skytilsmod.utils.NumberUtil.nf
import skytils.skytilsmod.utils.SkillUtils
import skytils.skytilsmod.utils.append
import skytils.skytilsmod.utils.formattedName
import skytils.skytilsmod.utils.setHoverText
import java.util.*
import kotlin.time.Duration


object CataCommand : StatCommand("skytilscata") {

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

            val archLevel =
                SkillUtils.calcXpWithProgress(
                    dungeonsData.classExperiences?.get(DungeonClass.ARCHER) ?: 0.0,
                    SkillUtils.dungeoneeringXp.values
                )
            val bersLevel =
                SkillUtils.calcXpWithProgress(
                    dungeonsData.classExperiences?.get(DungeonClass.BERSERK) ?: 0.0,
                    SkillUtils.dungeoneeringXp.values
                )
            val healerLevel =
                SkillUtils.calcXpWithProgress(
                    dungeonsData.classExperiences?.get(DungeonClass.HEALER) ?: 0.0,
                    SkillUtils.dungeoneeringXp.values
                )
            val mageLevel =
                SkillUtils.calcXpWithProgress(
                    dungeonsData.classExperiences?.get(DungeonClass.MAGE) ?: 0.0,
                    SkillUtils.dungeoneeringXp.values
                )
            val tankLevel =
                SkillUtils.calcXpWithProgress(
                    dungeonsData.classExperiences?.get(DungeonClass.TANK) ?: 0.0,
                    SkillUtils.dungeoneeringXp.values
                )

            val secrets = playerResponse.achievements.getOrDefault("skyblock_treasure_hunter", 0)

            val classAvgOverflow = (archLevel + bersLevel + healerLevel + mageLevel + tankLevel) / 5.0
            val classAvgCapped =
                (archLevel.coerceAtMost(50.0) + bersLevel.coerceAtMost(50.0) + healerLevel.coerceAtMost(50.0) + mageLevel.coerceAtMost(
                    50.0
                ) + tankLevel.coerceAtMost(50.0)) / 5.0

            val component = UMessage("§a➜ Catacombs Statistics Viewer\n")
                .append(
                    "§2§l ❣ §7§oYou are looking at data for ${playerResponse.formattedName}§7§o.\n\n"
                )
                .append("§a§l➜ Catacombs Levels:\n")
                .append("§d ☠ Cata Level: §l➡ §e${nf.format(cataLevel)}\n")
                .append("§9 ☠ Class Avg: §l➡ §e${nf.format(classAvgCapped)} §7(${nf.format(classAvgOverflow)})\n\n")
                .append("§6 ☣ Archer Level: §l➡ §e${nf.format(archLevel)}\n")
                .append("§c ⚔ Berserk Level: §l➡ §e${nf.format(bersLevel)}\n")
                .append("§a ❤ Healer Level: §l➡ §e${nf.format(healerLevel)}\n")
                .append("§b ✎ Mage Level: §l➡ §e${nf.format(mageLevel)}\n")
                .append("§7 ❈ Tank Level: §l➡ §e${nf.format(tankLevel)}\n\n")
                .append("§a§l➜ Floor Completions:\n")

            val completionObj = cataData.completions
            val highestFloor = cataData.highestCompletion

            if (completionObj != null && highestFloor != null) {
                component.append(UTextComponent(" §aFloor Completions: §7(Hover)\n").setHoverText(buildString {
                    for (i in 0..highestFloor) {
                        append("§2§l●§a ")
                        append(if (i == 0) "Entrance: " else "Floor $i: ")
                        append("§e")
                        append(completionObj[i])
                        append(if (i < highestFloor) "\n" else "")
                    }
                }))

                val fastestSTimes = cataData.fastestTimeS
                if (fastestSTimes != null) {
                    component.append(UTextComponent(" §aFastest §2S §aCompletions: §7(Hover)\n").setHoverText(
                        buildString {
                            for (i in 0..highestFloor) {
                                append("§2§l●§a ")
                                append(if (i == 0) "Entrance: " else "Floor $i: ")
                                append("§e")
                                append(fastestSTimes[i]?.timeFormat() ?: "§cNo S Completion")
                                append(if (i < highestFloor) "\n" else "")
                            }
                        }
                    ))
                }


                val fastestSPlusTimes = cataData.fastestTimeSPlus
                if (fastestSPlusTimes != null) {
                    component.append(
                        UTextComponent(" §aFastest §2S+ §aCompletions: §7(Hover)\n\n").setHoverText(
                            buildString {
                                for (i in 0..highestFloor) {
                                    append("§2§l●§a ")
                                    append(if (i == 0) "Entrance: " else "Floor $i: ")
                                    append("§e")
                                    append(fastestSPlusTimes[i]?.timeFormat() ?: "§cNo S+ Completion")
                                    append(if (i < highestFloor) "\n" else "")
                                }
                            }
                        )
                    )
                }
            }

            if (masterCataData?.completions != null) {
                val masterCompletionObj = masterCataData.completions
                val highestMasterFloor = masterCataData.highestCompletion

                if (masterCompletionObj != null && highestMasterFloor != null) {

                    component
                        .append("§a§l➜ Master Mode:\n")

                    component.append(UTextComponent(" §aFloor Completions: §7(Hover)\n").setHoverText(buildString {
                        for (i in 1..highestMasterFloor) {
                            append("§2§l●§a ")
                            append("Floor $i: ")
                            append("§e")
                            append(if (i in masterCompletionObj) masterCompletionObj[i] else "§cDNF")
                            append(if (i < highestMasterFloor) "\n" else "")
                        }
                    }))


                    val masterFastestS = UTextComponent(" §aFastest §2S §aCompletions: §7(Hover)\n")

                    if (masterCataData.fastestTimeS != null) {
                        val masterFastestSTimes = masterCataData.fastestTimeS!!
                        masterFastestS.setHoverText(buildString {
                            for (i in 1..highestMasterFloor) {
                                append("§2§l●§a ")
                                append("Floor $i: ")
                                append("§e")
                                append(masterFastestSTimes[i]?.timeFormat() ?: "§cNo S Completion")
                                append(if (i < highestMasterFloor) "\n" else "")
                            }
                        })
                    } else {
                        masterFastestS.setHoverText("§cNo S Completions")
                    }
                    component.append(masterFastestS)


                    val masterFastestSPlus = UTextComponent(" §aFastest §2S+ §aCompletions: §7(Hover)\n\n")

                    if (masterCataData.fastestTimeSPlus != null) {
                        val masterFastestSPlusTimes = masterCataData.fastestTimeSPlus!!
                        masterFastestSPlus.setHoverText(buildString {
                            for (i in 1..highestMasterFloor) {
                                append("§2§l●§a ")
                                append("Floor $i: ")
                                append("§e")
                                append(masterFastestSPlusTimes[i]?.timeFormat() ?: "§cNo S+ Completion")
                                append(if (i < highestMasterFloor) "\n" else "")
                            }
                        })
                    } else {
                        masterFastestSPlus.setHoverText(
                            "§cNo S+ Completions"
                        )
                    }
                    component.append(masterFastestSPlus)
                }
            }

            component
                .append("§a§l➜ Secrets:\n")
                .append(" §aTotal Secrets Found: §l➡ §e${nf.format(secrets)}\n")
                .chat()
        } catch (e: Throwable) {
            printMessage("§cCatacombs XP Lookup Failed: ${e.message ?: e::class.simpleName}")
            e.printStackTrace()
        }
    }

    private fun Duration.timeFormat() = toComponents { minutes, seconds, nanoseconds ->
        buildString {
            if (minutes > 0) {
                append(minutes)
                append(':')
            }
            append("%02d".format(seconds))
            if (nanoseconds != 0) {
                append('.')
                append("%03d".format((nanoseconds / 1e6).toInt()))
            }
        }
    }
}
