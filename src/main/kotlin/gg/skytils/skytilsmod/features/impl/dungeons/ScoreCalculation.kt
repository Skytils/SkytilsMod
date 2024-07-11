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
package gg.skytils.skytilsmod.features.impl.dungeons

import gg.essential.elementa.state.BasicState
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures.dungeonFloorNumber
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorChatComponentText
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

object ScoreCalculation {

    private val deathsTabPattern = Regex("§r§a§lTeam Deaths: §r§f(?<deaths>\\d+)§r")
    private val missingPuzzlePattern = Regex("§r§b§lPuzzles: §r§f\\((?<count>\\d)\\)§r")
    private val failedPuzzlePattern =
        Regex("§r (?<puzzle>.+): §r§7\\[§r§c§l✖§r§7] §.+")
    private val solvedPuzzlePattern =
        Regex("§r (?<puzzle>.+): §r§7\\[§r§a§l✔§r§7] §.+")
    private val discoveredPuzzlePattern =
        Regex("§r (?<puzzle>.+): §r§7\\[§r§6§l✦§r§7] §.+")
    private val secretsFoundPattern = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val secretsFoundPercentagePattern = Regex("§r Secrets Found: §r§[ae](?<percentage>[\\d.]+)%§r")
    private val cryptsPattern = Regex("§r Crypts: §r§6(?<crypts>\\d+)§r")
    private val dungeonClearedPattern = Regex("Cleared: (?<percentage>\\d+)% \\(\\d+\\)")
    private val timeElapsedPattern =
        Regex(" Elapsed: (?:(?<hrs>\\d+)h )?(?:(?<min>\\d+)m )?(?:(?<sec>\\d+)s)?")
    private val roomCompletedPattern = Regex("§r Completed Rooms: §r§d(?<count>\\d+)§r")

    val floorRequirements = hashMapOf(
        "E" to FloorRequirement(.3, 20 * 60),
        "F1" to FloorRequirement(.3),
        "F2" to FloorRequirement(.4),
        "F3" to FloorRequirement(.5),
        "F4" to FloorRequirement(.6, 12 * 60),
        "F5" to FloorRequirement(.7),
        "F6" to FloorRequirement(.85, 12 * 60),
        "F7" to FloorRequirement(speed = 14 * 60),
        "M1" to FloorRequirement(speed = 8 * 60),
        "M2" to FloorRequirement(speed = 8 * 60),
        "M3" to FloorRequirement(speed = 8 * 60),
        "M4" to FloorRequirement(speed = 8 * 60),
        "M5" to FloorRequirement(speed = 8 * 60),
        "M6" to FloorRequirement(speed = 8 * 60),
        "M7" to FloorRequirement(speed = 15 * 60),
        "default" to FloorRequirement()
    )

    // Raw vars parsed from the dungeon
    // clear stuff
    private var completedRooms = BasicState(0)
    private var clearedPercentage = BasicState(0)

    // death stuff
    private var deaths = BasicState(0)
    var firstDeathHadSpirit = BasicState(false)

    // puzzle stuff
    private var puzzleCount = 0
    private var completedPuzzleCount = 0
    private var missingPuzzles = BasicState(0).also {
        it.onSetValue {
            printDevMessage("missing puzzles $it", "scorecalcpuzzle")
        }
    }
    private val failedPuzzlesNames = hashSetOf<String>()
    private var failedPuzzles: BasicState<Int> = BasicState(0).also {
        it.onSetValue {
            updateText(totalScore.get())
        }
    }

    // secrets stuff
    var floorReq = BasicState(floorRequirements["default"]!!)
    private var foundSecrets: BasicState<Int> = BasicState(0).also {
        it.onSetValue {
            updateText(totalScore.get())
        }
    }
    private var totalSecrets = BasicState(0)

    // speed stuff
    private var secondsElapsed = BasicState(0.0)

    // bonus stuff
    private var crypts = BasicState(0)
    var mimicKilled = BasicState(false)
    private var isPaul = BasicState(false)

    init {
        tickTimer(5, repeats = true) {
            isPaul.set(
                (MayorInfo.currentMayor == "Paul" && MayorInfo.mayorPerks.contains("EZPZ")) || MayorInfo.jerryMayor?.name == "Paul"
            )
        }
    }

    // Calcs
    //  Room clear %, used in Skill and Exploration scores
    private val totalRoomMap = mutableMapOf<Int, Int>()
    private val totalRooms = clearedPercentage.zip(completedRooms).map { (clear, complete) ->
        printDevMessage("total clear $clear complete $complete", "scorecalcroom")
        val a = if (clear > 0 && complete > 0) {
            (100 * (complete / clear.toDouble())).roundToInt()
        } else 0
        printDevMessage("total? $a", "scorecalcroom")
        if (a == 0) return@map 0
        totalRoomMap[a] = (totalRoomMap[a] ?: 0) + 1
        totalRoomMap.toList().maxByOrNull { it.second }!!.first
    }
    private val effectiveCompletedRooms = completedRooms.map {
        it + (!DungeonFeatures.hasBossSpawned).ifTrue(1) + (DungeonTimer.bloodClearTime == -1L).ifTrue(1)
    }
    private val effectiveClearPercentage = effectiveCompletedRooms.map { complete ->
        val total = totalRooms.get()
        printDevMessage("total $total complete $complete", "scorecalcroom")
        val a = if (total > 0) (complete / total.toDouble()).coerceAtMost(1.0) else 0.0
        printDevMessage("calced room clear $a", "scorecalcroom")
        a
    }

    //  death penalty, used in Skill score
    private val deathPenalty = deaths.zip(firstDeathHadSpirit).map { (deathCount, spirit) ->
        (2 * deathCount) - spirit.ifTrue(1)
    }

    //  % of (max for score) secrets found, used in Explore score
    private val maxScoreSecrets = floorReq.zip(totalSecrets).map { (req, total) ->
        if (total == 0) return@map 1
        ceil(total * req.secretPercentage).toInt()
    }

    //  seconds past the floor speed requirement
    private val totalElapsed = secondsElapsed.zip(floorReq).map { (seconds, req) ->
        seconds + 480 - req.speed
    }

    //  final speed score
    private val speedScore = totalElapsed.map { time ->
        if (DungeonFeatures.dungeonFloor == "E") {
            when {
                time < 492.0 -> 70.0
                time < 600.0 -> (140 - time / 12.0) * 0.7
                time < 840.0 -> (115 - time / 24.0) * 0.7
                time < 1140.0 -> (108 - time / 30.0) * 0.7
                time < 3570.0 -> (98.5 - time / 40.0) * 0.7
                else -> 0.0
            }.toInt()
        } else {
            when {
                time < 492.0 -> 100.0
                time < 600.0 -> 140 - time / 12.0
                time < 840.0 -> 115 - time / 24.0
                time < 1140.0 -> 108 - time / 30.0
                time < 3570.0 -> 98.5 - time / 40.0
                else -> 0.0
            }.toInt()
        }
    }

    // Min secret stuff
    // The max number of secrets that can be skipped for 300 assuming 5 crypts, mimic and 100 speed score
    private val skippableSecrets = maxScoreSecrets.zip(failedPuzzles.zip(deathPenalty.zip(isPaul))).map {
        val totalSecrets = it.first
        val failedRooms = it.second.first
        val deathPenalty = it.second.second.first
        val paul = it.second.second.second.ifTrue(10)
        val mimic = ((dungeonFloorNumber ?: 0) >= 6).ifTrue(2)

        val neededScore = 40 - paul - 5 - mimic + deathPenalty + if (failedRooms > 0) {
            val total = totalRooms.get().toDouble()
            if (total == 0.0) return@map -1

            ceil(80 * failedRooms / total).toInt() + ceil(60 * failedRooms / total).toInt() + 10 * failedRooms
        } else {
            0
        }
        // Clear too bad to skip secrets
        if (neededScore > 40) return@map -2
        // Clear too good to have skipped secrets
        if (neededScore <= (foundSecrets.get() / totalSecrets.toDouble() * 40f).coerceIn(0.0, 40.0)) return@map -3

        return@map ceil(neededScore / 40.0 * totalSecrets).toInt()
    }

    // final bonus score
    private val bonusScore = crypts.zip(mimicKilled.zip(isPaul)).map { (crypts, bools) ->
        ((if (bools.first) 2 else 0) + crypts.coerceAtMost(5) + if (bools.second) 10 else 0)
    }

    // The min number of secrets for 300 score with current failed puzzles, deaths, bonus & speed score
    private val minSecrets = maxScoreSecrets.zip(failedPuzzles.zip(deathPenalty.zip(bonusScore.zip(speedScore)))).map {
        val totalSecrets = it.first
        val failedRooms = it.second.first
        val deathPenalty = it.second.second.first
        val (bonus, speed) = it.second.second.second

        val neededScore = 40 - bonus + deathPenalty + (100 - speed) + if (failedRooms > 0) {
            val total = totalRooms.get().toDouble()
            if (total == 0.0) return@map -1

            ceil(80 * failedRooms / total).toInt() + ceil(60 * failedRooms / total).toInt() + 10 * failedRooms
        } else {
            0
        }

        // Clear too bad for secrets to compensate
        if (neededScore > 40) return@map -2
        // Clear too good to need secrets to compensate
        if (neededScore <= (foundSecrets.get() / totalSecrets.toDouble() * 40f).coerceIn(0.0, 40.0)) return@map -3

        return@map ceil(neededScore / 40.0 * totalSecrets).toInt()
    }.also {
        it.onSetValue {
            updateText(totalScore.get())
        }
    }

    // puzzle penalty, used in Skill score
    private val puzzlePenalty = missingPuzzles.zip(failedPuzzles).map { (missing, failed) ->
        printDevMessage("puzzle penalty changed", "scorecalcpuzzle")
        10 * (missing + failed)
    }

    // final skill score
    private val skillScore = effectiveClearPercentage.zip(deathPenalty.zip(puzzlePenalty)).map { (clear, penalties) ->
        printDevMessage("puzzle penalty ${penalties.second}", "scorecalcpuzzle")
        if (DungeonFeatures.dungeonFloor == "E")
            ((20.0 + clear * 80.0 - penalties.first - penalties.second) * 0.7).toInt()
        else (20.0 + clear * 80.0 - penalties.first - penalties.second).toInt()
    }

    // secret and clear score, both used in Explore score
    private val percentageOfMaxSecretsFound = foundSecrets.zip(maxScoreSecrets).map { (found, totalNeeded) ->
        found / totalNeeded.toDouble()
    }

    private val secretScore = totalSecrets.zip(percentageOfMaxSecretsFound).map { (total, percent) ->
        if (total <= 0)
            0.0
        else
            (40f * percent).coerceIn(0.0, 40.0)
    }
    private val roomClearScore = effectiveClearPercentage.map {
        (60 * it).coerceIn(0.0, 60.0)
    }

    // final explore score
    private val exploreScore = roomClearScore.zip(secretScore).map { (clear, secret) ->
        printDevMessage("clear $clear secret $secret", "scorecalcexplore")
        if (DungeonFeatures.dungeonFloor == "E") (clear * 0.7).toInt() + (secret * 0.7).toInt()
        else clear.toInt() + secret.toInt()
    }

    private var hasSaid270 = false
    private var hasSaid300 = false

    val totalScore =
        ((skillScore.zip(exploreScore)).zip(speedScore.zip(bonusScore))).map { (first, second) ->
            printDevMessage("skill score ${first.first}", "scorecalcpuzzle")
            printDevMessage(
                "skill ${first.first} disc ${first.second} speed ${second.first} bonus ${second.second}",
                "scorecalctotal"
            )
            if (DungeonFeatures.dungeonFloor == "E")
                first.first.coerceIn(14, 70) + first.second + second.first + (second.second * 0.7).toInt()
            else first.first.coerceIn(20, 100) + first.second + second.first + second.second
        }.also { state ->
            state.onSetValue { score ->
                updateText(score)
                if (!Utils.inDungeons) return@onSetValue
                if (score < 200) {
                    hasSaid270 = false
                    hasSaid300 = false
                    return@onSetValue
                }
                if (!hasSaid270 && score >= 270) {
                    hasSaid270 = true
                    if (Skytils.config.createTitleOn270Score) GuiManager.createTitle(
                        "§c§l" + Skytils.config.messageTitle270Score.ifBlank { "270" },
                        20
                    )
                    if (Skytils.config.sendMessageOn270Score) Skytils.sendMessageQueue.add("/pc Skytils-SC > ${Skytils.config.message270Score.ifBlank { "270 score" }}")
                }
                if (!hasSaid300 && score >= 300) {
                    hasSaid300 = true
                    if (Skytils.config.createTitleOn300Score) GuiManager.createTitle(
                        "§c§l" + Skytils.config.messageTitle300Score.ifBlank { "300" },
                        20
                    )
                    if (Skytils.config.sendMessageOn300Score) Skytils.sendMessageQueue.add("/pc Skytils-SC > ${Skytils.config.message300Score.ifBlank { "300 score" }}")
                }
            }
        }

    private val rank: String
        get() {
            val score = totalScore.get()
            return when {
                score < 100 -> "§cD"
                score < 160 -> "§9C"
                score < 230 -> "§aB"
                score < 270 -> "§5A"
                score < 300 -> "§eS"
                else -> "§6§lS+"
            }
        }

    fun updateText(score: Int) {
        Utils.checkThreadAndQueue {
            ScoreCalculationElement.text.clear()
            if (!Utils.inDungeons) return@checkThreadAndQueue
            if (Skytils.config.showDungeonStatus) {
                ScoreCalculationElement.text.add("§9Dungeon Status")
                ScoreCalculationElement.text.add("• §eDeaths: ${if (deaths.get() == 0) {
                    "§a"
                } else {
                    "§c"
                }}${deaths.get()} ${if (firstDeathHadSpirit.get()) "§7(§6Spirit§7)" else ""}")
                ScoreCalculationElement.text.add("• §ePuzzles: ${if (completedPuzzleCount == puzzleCount) {
                    "§a"
                } else if (failedPuzzles.get() > 0) {
                    "§c"
                } else {
                    "§e"
                }}$completedPuzzleCount§7/§a$puzzleCount")
                ScoreCalculationElement.text.add("• §eSecrets: " + if (totalSecrets.get() == 0) {
                    "§7?"
                } else {
                    val found = foundSecrets.get()
                    (if (found == totalSecrets.get()) {
                        "§a"
                    } else if (found >= minSecrets.get()) {
                        "§e"
                    } else {
                        "§c"
                    }) + found + "§7/§a" + totalSecrets.get() + " §e(min: " + when (val sec = minSecrets.get()) {
                        -1 -> "§7?"
                        -2 -> "§c✘"
                        -3 -> "§a✔"
                        else -> "§a${(sec - found).takeUnless { it <= 0 } ?: "✔"}"
                    } + "§e, could skip: " + when (val skip = skippableSecrets.get()) {
                        -1 -> "§7?"
                        -2 -> "§c✘"
                        -3 -> "§a✔"
                        else -> "§a${totalSecrets.get()  - skip}"
                    } + "§e)"
                })
                ScoreCalculationElement.text.add("• §eCrypts: ${if (crypts.get() >= 5) {
                    "§a"
                } else {
                    "§c"
                }}${crypts.get().coerceAtMost(5)}")
                ScoreCalculationElement.text.add("• §eMimic: §l${if ((dungeonFloorNumber ?: 0) >= 6) {
                    if (mimicKilled.get()) {
                        "§a✔"
                    } else {
                        "§c✘"
                    }
                } else {
                    "§7-"
                }}")
                ScoreCalculationElement.text.add("")
            }
            if (Skytils.config.showScoreBreakdown) {
                ScoreCalculationElement.text.add("§6Dungeon Score")
                if (DungeonFeatures.dungeonFloor == "E")
                    ScoreCalculationElement.text.add("• §eSkill Score:§a ${skillScore.get().coerceIn(14, 70)}")
                else
                    ScoreCalculationElement.text.add("• §eSkill Score:§a ${skillScore.get().coerceIn(20, 100)}")
                ScoreCalculationElement.text.add(
                    "• §eExplore Score:§a ${exploreScore.get()} §7(§e${
                        roomClearScore.get().toInt()
                    } §7+ §6${secretScore.get().toInt()}§7)"
                )
                ScoreCalculationElement.text.add("• §eSpeed Score:§a ${speedScore.get()}")

                if (DungeonFeatures.dungeonFloor == "E") {
                    ScoreCalculationElement.text.add("• §eBonus Score:§a ${(bonusScore.get() * 0.7).toInt()}")
                    ScoreCalculationElement.text.add("• §eTotal Score:§a $score" + if (isPaul.get()) " §7(§6+7§7)" else "")
                } else {
                    ScoreCalculationElement.text.add("• §eBonus Score:§a ${bonusScore.get()}")
                    ScoreCalculationElement.text.add("• §eTotal Score:§a $score" + if (isPaul.get()) " §7(§6+10§7)" else "")
                }
                ScoreCalculationElement.text.add("• §eRank: $rank")
            } else {
                val color = when {
                    score < 270 -> 'c'
                    score < 300 -> 'e'
                    else -> 'a'
                }
                ScoreCalculationElement.text.add("• §6Dungeon Score: §$color$score §7($rank§7)")
            }
        }
    }


    @SubscribeEvent
    fun onScoreboardChange(event: MainReceivePacketEvent<*, *>) {
        if (
            !Utils.inSkyblock ||
            event.packet !is S3EPacketTeams
        ) return
        if (event.packet.action != 2) return
        val line = event.packet.players.joinToString(
            " ",
            prefix = event.packet.prefix,
            postfix = event.packet.suffix
        ).stripControlCodes()
        printDevMessage(line, "scorecalcscoreboard")
        if (line.startsWith("Cleared: ")) {
            val matcher = dungeonClearedPattern.find(line)
            if (matcher != null) {
                if (DungeonTimer.dungeonStartTime == -1L)
                    DungeonTimer.dungeonStartTime = System.currentTimeMillis()
                clearedPercentage.set(matcher.groups["percentage"]?.value?.toIntOrNull() ?: 0)
                return
            }
        }
        if (line.startsWith("Time Elapsed:")) {
            if (DungeonTimer.dungeonStartTime == -1L)
                DungeonTimer.dungeonStartTime = System.currentTimeMillis()
            val matcher = timeElapsedPattern.find(line)
            if (matcher != null) {
                val hours = matcher.groups["hrs"]?.value?.toIntOrNull() ?: 0
                val minutes = matcher.groups["min"]?.value?.toIntOrNull() ?: 0
                val seconds = matcher.groups["sec"]?.value?.toIntOrNull() ?: 0
                secondsElapsed.set((hours * 3600 + minutes * 60 + seconds).toDouble())
                return
            }
        }
    }

    @SubscribeEvent
    fun onTabChange(event: MainReceivePacketEvent<*, *>) {
        if (
            !Utils.inDungeons ||
            event.packet !is S38PacketPlayerListItem ||
            (event.packet.action != S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME &&
                    event.packet.action != S38PacketPlayerListItem.Action.ADD_PLAYER)
        ) return
        event.packet.entries.forEach { playerData ->
            val name = playerData?.displayName?.formattedText ?: playerData?.profile?.name ?: return@forEach
            printDevMessage(name, "scorecalctab")
            when {
                name.contains("Deaths:") -> {
                    val matcher = deathsTabPattern.find(name) ?: return@forEach
                    deaths.set(matcher.groups["deaths"]?.value?.toIntOrNull() ?: 0)
                }

                name.contains("Puzzles:") -> {
                    val matcher = missingPuzzlePattern.find(name) ?: return@forEach
                    missingPuzzles.set(matcher.groups["count"]?.value?.toIntOrNull() ?: 0)
                    puzzleCount = missingPuzzles.get()
                    printDevMessage("puzzles ${missingPuzzles.get()}", "scorecalcpuzzle")
                    updateText(totalScore.get())
                }

                name.contains("✔") -> {
                    if (solvedPuzzlePattern.containsMatchIn(name)) {
                        completedPuzzleCount++
                        missingPuzzles.set((missingPuzzles.get() - 1).coerceAtLeast(0))
                    }
                }

                name.contains("✖") -> {
                    failedPuzzlePattern.find(name)?.let {
                        val (puzzleName) = it.destructured
                        missingPuzzles.set((missingPuzzles.get() - 1).coerceAtLeast(0))
                        failedPuzzles.set(failedPuzzles.get() + 1)
                        failedPuzzlesNames.add(puzzleName)
                    }
                }

                name.contains("✦") -> {
                    if (failedPuzzlesNames.isNotEmpty()) {
                        discoveredPuzzlePattern.find(name)?.let {
                            val (puzzleName) = it.destructured
                            if (failedPuzzlesNames.remove(puzzleName)) {
                                missingPuzzles.set(missingPuzzles.get() + 1)
                                failedPuzzles.set((failedPuzzles.get() - 1).coerceAtLeast(0))
                                if (puzzleName == "Higher Or Lower") {
                                    DungeonFeatures.blazes = 0
                                }
                            }
                        }
                    }
                }

                name.contains("Secrets Found:") -> {
                    printDevMessage(name, "scorecalcsecrets")
                    if (name.contains("%")) {
                        val matcher = secretsFoundPercentagePattern.find(name) ?: return@forEach
                        val percentagePer = (matcher.groups["percentage"]?.value?.toDoubleOrNull()
                            ?: 0.0)
                        printDevMessage("percent $percentagePer", "scorecalcsecrets")
                        totalSecrets.set(
                            if (foundSecrets.get() > 0 && percentagePer > 0) floor(100f / percentagePer * foundSecrets.get() + 0.5).toInt() else 0
                        )
                    } else {
                        val matcher = secretsFoundPattern.find(name) ?: return@forEach
                        foundSecrets.set(matcher.groups["secrets"]?.value?.toIntOrNull() ?: 0)
                    }
                }

                name.contains("Crypts:") -> {
                    val matcher = cryptsPattern.find(name) ?: return@forEach
                    crypts.set(matcher.groups["crypts"]?.value?.toIntOrNull() ?: 0)
                }

                name.contains("Completed Rooms") -> {
                    val matcher = roomCompletedPattern.find(name) ?: return@forEach
                    completedRooms.set(matcher.groups["count"]?.value?.toIntOrNull() ?: return@forEach)
                    printDevMessage("count ${completedRooms.get()} percent ${clearedPercentage.get()}", "scorecalc")
                    printDevMessage("Total rooms: ${totalRooms.get()}", "scorecalc")
                }
            }
        }
    }

    @SubscribeEvent
    fun onTitle(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inDungeons || event.packet !is S45PacketTitle || event.packet.type != S45PacketTitle.Type.TITLE) return
        if (event.packet.message.formattedText == "§eYou became a ghost!§r") {
            if (DungeonListener.hutaoFans.getIfPresent(mc.thePlayer.name) == true
                && DungeonListener.team[mc.thePlayer.name]?.deaths == 0
            ) firstDeathHadSpirit.set(
                true
            )
            printDevMessage("you died. spirit: ${firstDeathHadSpirit.get()}", "scorecalcdeath")
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChatReceived(event: ClientChatReceivedEvent) {
        if (!Utils.inDungeons || mc.thePlayer == null || event.type == 2.toByte()) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (Skytils.config.scoreCalculationReceiveAssist) {
            if (unformatted.startsWith("Party > ") || (unformatted.contains(":") && !unformatted.contains(">"))) {
                if (unformatted.contains("\$SKYTILS-DUNGEON-SCORE-MIMIC$") || (Skytils.config.receiveHelpFromOtherModMimicDead && unformatted.containsAny(
                        "Mimic dead!", "Mimic Killed!", "Mimic Dead!"
                    ))
                ) {
                    mimicKilled.set(true)
                    return
                }
                if (unformatted.contains("\$SKYTILS-DUNGEON-SCORE-ROOM$")) {
                    event.isCanceled = true
                    return
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun canYouPleaseStopCryingThanks(event: ClientChatReceivedEvent) {
        if (!Utils.inDungeons || event.type != 0.toByte()) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if ((unformatted.startsWith("Party > ") || unformatted.startsWith("P > ")) && unformatted.contains(": Skytils-SC > ")) {
            event.message.siblings.filterIsInstance<ChatComponentText>().forEach {
                it as AccessorChatComponentText
                if (it.text.startsWith("Skytils-SC > ")) {
                    it.text = it.text.substringAfter("Skytils-SC > ")
                } else if (it.text.startsWith("\$SKYTILS-DUNGEON-SCORE-MIMIC\$")) {
                    it.text = it.text.replace("\$SKYTILS-DUNGEON-SCORE-MIMIC\$", "Mimic Killed!")
                }
            }
        }
    }

    @SubscribeEvent
    fun clearScore(event: WorldEvent.Unload) {
        mimicKilled.set(false)
        firstDeathHadSpirit.set(false)
        floorReq.set(floorRequirements["default"]!!)
        puzzleCount = 0
        completedPuzzleCount = 0
        missingPuzzles.set(0)
        failedPuzzles.set(0)
        secondsElapsed.set(0.0)
        foundSecrets.set(0)
        totalSecrets.set(0)
        completedRooms.set(0)
        clearedPercentage.set(0)
        deaths.set(0)
        crypts.set(0)
        totalRoomMap.clear()
    }

    init {
        ScoreCalculationElement()
        HugeCryptsCounter()
    }

    class HugeCryptsCounter : GuiElement("Dungeon Crypts Counter", scale = 2f, x = 200, y = 200) {
        override fun render() {
            if (toggled && Utils.inDungeons && DungeonTimer.dungeonStartTime != -1L) {

                val leftAlign = scaleX < sr.scaledWidth / 2f
                ScreenRenderer.fontRenderer.drawString(
                    "Crypts: ${crypts.get()}",
                    if (leftAlign) 0f else width.toFloat(),
                    0f,
                    alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT,
                    customColor = if (crypts.get() < 5) CommonColors.RED else CommonColors.LIGHT_GREEN
                )
            }
        }

        override fun demoRender() {

            val leftAlign = scaleX < sr.scaledWidth / 2f
            ScreenRenderer.fontRenderer.drawString(
                "Crypts: 5",
                if (leftAlign) 0f else width.toFloat(),
                0f,
                alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT,
                customColor = CommonColors.LIGHT_GREEN
            )
        }

        override val toggled: Boolean
            get() = Skytils.config.bigCryptsCounter
        override val height: Int
            get() = fr.FONT_HEIGHT
        override val width: Int
            get() = fr.getStringWidth("Crypts: 5")

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class ScoreCalculationElement : GuiElement("Dungeon Score Estimate", x = 200, y = 100) {
        override fun render() {
            if (toggled && Utils.inDungeons) {
                RenderUtil.drawAllInList(this, text)
            }
        }

        override fun demoRender() {
            if (Skytils.config.showDungeonStatus) {
                RenderUtil.drawAllInList(this, demoStatus + if (Skytils.config.showScoreBreakdown) demoScore else demoMin)
            } else {
                RenderUtil.drawAllInList(this, if (Skytils.config.showScoreBreakdown) demoScore else demoMin)
            }
        }

        companion object {
            private val demoStatus = listOf(
                "§9Dungeon Status",
                "• §eDeaths: §c1 §7(§6Spirit§7)",
                "• §ePuzzles: §a5§7/§a5",
                "• §eSecrets: §e30§7/§a40 §e(min: §a✔§e, could skip: §a20§e)",
                "• §eCrypts: §c4",
                "• §eMimic:§l§a ✔",
                ""
            )
            private val demoScore = listOf(
                "§6Dungeon Score",
                "• §eSkill Score:§a 100",
                "• §eExplore Score:§a 100 §7(§e60 §7+ §640§7)",
                "• §eSpeed Score:§a 100",
                "• §eBonus Score:§a 17",
                "• §eTotal Score:§a 317 §7(§6+10§7)",
                "• §eRank: §6§lS+",
            )
            private val demoMin = listOf("• §6Dungeon Score: §a 300 §7(§6§lS+§7)")
            val text = ArrayList<String>()
        }

        override val height: Int
            get() = (if (Skytils.config.showDungeonStatus) {
                7
            } else {
                0
            } + if (Skytils.config.showScoreBreakdown) {
                7
            } else {
                1
            }) * ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = (if (Skytils.config.showDungeonStatus) {
                demoStatus
            } else {
                emptyList()
            } + if (Skytils.config.showScoreBreakdown) {
                demoScore
            } else {
                demoMin
            }).maxOf { ScreenRenderer.fontRenderer.getStringWidth(it) }

        override val toggled: Boolean
            get() = Skytils.config.showScoreCalculation

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    data class FloorRequirement(val secretPercentage: Double = 1.0, val speed: Int = 10 * 60)

    private fun Boolean.ifTrue(num: Int) = if (this) num else 0
}
