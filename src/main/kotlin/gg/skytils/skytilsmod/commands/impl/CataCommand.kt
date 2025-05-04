/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import gg.essential.universal.utils.MCHoverEventAction
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.hypixel.types.skyblock.Member
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.API
import gg.skytils.skytilsmod.utils.*
import kotlinx.coroutines.launch
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Commands
object CataCommand {
    @Command("skytilscata [name]")
    suspend fun processCommand(
        @Argument("name")
        name: String? = null
    ) = Skytils.IO.launch {
        val username = name ?: mc.thePlayer.name
        UChat.chat("§aGetting data for ${username}...")
        val uuid = try {
            if (name == null) mc.thePlayer.uniqueID else MojangUtil.getUUIDFromUsername(username)
        } catch (e: MojangUtil.MojangException) {
            UChat.chat("$failPrefix §cFailed to get UUID, reason: ${e.message}")
            return@launch
        } ?: return@launch
        val profile = try {
            API.getSelectedSkyblockProfile(uuid)?.members?.get(uuid.nonDashedString())
        } catch (e: Exception) {
            e.printStackTrace()
            UChat.chat(
                "$failPrefix §cUnable to retrieve profile information: ${
                    e.message
                }"
            )
            return@launch
        } ?: return@launch
        displayStats(username, uuid, profile)
    }

    suspend fun displayStats(username: String, uuid: UUID, profileData: Member) {
        val playerResponse = try {
            API.getPlayerSync(uuid)
        } catch (e: Throwable) {
            UChat.chat(
                "$failPrefix §cFailed to get dungeon stats: ${
                    e.message
                }"
            )
            return
        }

        try {
            val dungeonsData = profileData.dungeons ?: run {
                UChat.chat("$failPrefix §c${username} has not entered any dungeons!")
                return
            }

            val catacombsObj = dungeonsData.dungeon_types["catacombs"]
            if (catacombsObj?.experience == null) {
                UChat.chat("$failPrefix §c${username} has not entered The Catacombs!")
                return
            }
            val cataData = catacombsObj.normal
            val masterCataData = catacombsObj.master

            val cataLevel =
                SkillUtils.calcXpWithProgress(catacombsObj.experience, SkillUtils.dungeoneeringXp.values)

            val archXP = dungeonsData.player_classes["archer"]?.experience ?: 0.0
            val bersXP = dungeonsData.player_classes["berserk"]?.experience ?: 0.0
            val healerXP = dungeonsData.player_classes["healer"]?.experience ?: 0.0
            val mageXP = dungeonsData.player_classes["mage"]?.experience ?: 0.0
            val tankXP = dungeonsData.player_classes["tank"]?.experience ?: 0.0

            val archLevel =
                SkillUtils.calcXpWithProgress(
                    archXP,
                    SkillUtils.dungeoneeringXp.values
                )
            val bersLevel =
                SkillUtils.calcXpWithProgress(
                    bersXP,
                    SkillUtils.dungeoneeringXp.values
                )
            val healerLevel =
                SkillUtils.calcXpWithProgress(
                    healerXP,
                    SkillUtils.dungeoneeringXp.values
                )
            val mageLevel =
                SkillUtils.calcXpWithProgress(
                    mageXP,
                    SkillUtils.dungeoneeringXp.values
                )
            val tankLevel =
                SkillUtils.calcXpWithProgress(
                    tankXP,
                    SkillUtils.dungeoneeringXp.values
                )

            val secrets = playerResponse?.achievements?.getOrDefault("skyblock_treasure_hunter", 0) ?: 0

            val classAvgOverflow = (archLevel + bersLevel + healerLevel + mageLevel + tankLevel) / 5.0
            val classAvgCapped =
                (archLevel.coerceAtMost(50.0) + bersLevel.coerceAtMost(50.0) + healerLevel.coerceAtMost(50.0) + mageLevel.coerceAtMost(
                    50.0
                ) + tankLevel.coerceAtMost(50.0)) / 5.0

            val component = UMessage("§a➜ Catacombs Statistics Viewer\n")
                .append(
                    "§2§l ❣ §7§oYou are looking at data for ${playerResponse?.formattedName}§7§o.\n\n"
                )
                .append("§a§l➜ Catacombs Levels:\n")
                .append(
                    UTextComponent("§d ☠ Cata Level: §l➡ §e${NumberUtil.nf.format(cataLevel)}\n").setHover(
                        MCHoverEventAction.SHOW_TEXT,
                        "§e${NumberUtil.nf.format(catacombsObj.experience)} XP"
                    )
                )
                .append("§9 ☠ Class Avg: §l➡ §e${NumberUtil.nf.format(classAvgCapped)} §7(${NumberUtil.nf.format(classAvgOverflow)})\n\n")
                .append(
                    UTextComponent("§6 ☣ Archer Level: §l➡ §e${NumberUtil.nf.format(archLevel)}\n").setHover(
                        MCHoverEventAction.SHOW_TEXT,
                        "§e${NumberUtil.nf.format(archXP)} XP"
                    )
                )
                .append(
                    UTextComponent("§c ⚔ Berserk Level: §l➡ §e${NumberUtil.nf.format(bersLevel)}\n").setHover(
                        MCHoverEventAction.SHOW_TEXT,
                        "§e${NumberUtil.nf.format(bersXP)} XP"
                    )
                )
                .append(
                    UTextComponent("§a ❤ Healer Level: §l➡ §e${NumberUtil.nf.format(healerLevel)}\n").setHover(
                        MCHoverEventAction.SHOW_TEXT,
                        "§e${NumberUtil.nf.format(healerXP)} XP"
                    )
                )
                .append(
                    UTextComponent("§b ✎ Mage Level: §l➡ §e${NumberUtil.nf.format(mageLevel)}\n").setHover(
                        MCHoverEventAction.SHOW_TEXT,
                        "§e${NumberUtil.nf.format(mageXP)} XP"
                    )
                )
                .append(
                    UTextComponent("§7 ❈ Tank Level: §l➡ §e${NumberUtil.nf.format(tankLevel)}\n\n").setHover(
                        MCHoverEventAction.SHOW_TEXT,
                        "§e${NumberUtil.nf.format(tankXP)} XP"
                    )
                )
                .append("§a§l➜ Floor Completions:\n")


            val completionObj = cataData.tier_completions
            val highestFloor = cataData.highest_tier_completed.toInt()

            if (highestFloor != 0) {
                if (completionObj.isNotEmpty()) {
                    component.append(
                        createNormalComponent(
                            " §aFloor Completions: §7(Hover)\n",
                            highestFloor,
                            completionObj
                        ) { completions ->
                            completions?.toInt() ?: 0
                        }
                    )
                }

                if (cataData.fastest_time_s.isNotEmpty()) {
                    component.append(
                        createNormalComponent(
                            " §aFastest §2S §aCompletions: §7(Hover)\n",
                            highestFloor,
                            cataData.fastest_time_s
                        ) { time ->
                            time?.toDuration(DurationUnit.MILLISECONDS)?.timeFormat() ?: "§cNo S Completions"
                        }
                    )
                }


                if (cataData.fastest_time_s_plus.isNotEmpty()) {
                    component.append(
                        createNormalComponent(
                            " §aFastest §2S+ §aCompletions: §7(Hover)\n\n",
                            highestFloor,
                            cataData.fastest_time_s_plus
                        ) { time ->
                            time?.toDuration(DurationUnit.MILLISECONDS)?.timeFormat() ?: "§cNo S+ Completions"
                        }
                    )
                }
            }

            masterCataData?.tier_completions?.let { masterCompletionObj ->
                val highestMasterFloor = masterCataData.highest_tier_completed.toInt()

                if (masterCompletionObj.isNotEmpty() && highestMasterFloor != 0) {

                    component
                        .append("§a§l➜ Master Mode:\n")

                    component.append(
                        createMasterComponent(
                            " §aFloor Completions: §7(Hover)\n",
                            highestMasterFloor,
                            masterCompletionObj
                        ) { completions ->
                            completions?.toInt() ?: "§cNo Completions"
                        }
                    )

                    component.append(
                        createMasterComponent(
                            " §aFastest §2S §aCompletions: §7(Hover)\n",
                            highestMasterFloor,
                            masterCataData.fastest_time_s,
                            "§cNo S Completions"
                        ) { time ->
                            time?.toDuration(DurationUnit.MILLISECONDS)?.timeFormat() ?: "§cNo S Completion"
                        }
                    )

                    component.append(
                        createMasterComponent(
                            " §aFastest §2S+ §aCompletions: §7(Hover)\n\n",
                            highestMasterFloor,
                            masterCataData.fastest_time_s_plus,
                            "§cNo S+ Completions"
                        ) { time ->
                            time?.toDuration(DurationUnit.MILLISECONDS)?.timeFormat() ?: "§cNo S+ Completion"
                        }
                    )
                }

            }

            component
                .append("§a§l➜ Miscellaneous:\n")
                .append(" §aTotal Secrets Found: §l➡ §e${NumberUtil.nf.format(secrets)}\n")
                .append(
                    " §aBlood Mobs Killed: §l➡ §e${
                        NumberUtil.nf.format(
                            (profileData.player_stats.kills["watcher_summon_undead"]?.toInt() ?: 0) +
                                    (profileData.player_stats.kills["master_watcher_summon_undead"]?.toInt() ?: 0)
                        )
                    }\n"
                )
                .chat()
        } catch (e: Throwable) {
            UChat.chat("${Skytils.Companion.failPrefix} §cCatacombs XP Lookup Failed: ${e.message ?: e::class.simpleName}")
            e.printStackTrace()
        }
    }

    private fun createNormalComponent(
        text: String, highestFloor: Int,
        hoverContent: Map<String, Double>,
        transform: (Double?) -> Any
    ): UTextComponent =
        UTextComponent(text).run {
            val hoverText = (0..highestFloor).associateWith { floor ->
                transform(hoverContent["$floor"])
            }.map { (floor, value) ->
                "§2§l●§a Floor ${if (floor == 0) "Entrance" else floor}: §e$value"
            }.joinToString("\n")
            setHoverText(hoverText)
        }

    private fun createMasterComponent(
        text: String, highestFloor: Int,
        hoverContent: Map<String, Double>,
        emptyHoverText: String = "",
        transform: (Double?) -> Any
    ): UTextComponent =
        UTextComponent(text).run {
            if (hoverContent.isEmpty()) {
                setHoverText(emptyHoverText)
            } else {
                val hoverText = (1..highestFloor).associateWith { floor ->
                    transform(hoverContent["$floor"])
                }.map { (floor, value) ->
                    "§2§l●§a Floor $floor: §e$value"
                }.joinToString("\n")
                setHoverText(hoverText)
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