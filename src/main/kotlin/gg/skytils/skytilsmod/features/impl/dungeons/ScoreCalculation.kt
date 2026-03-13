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
import gg.essential.elementa.state.State
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.ChatMessageReceivedEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.DungeonPuzzleResetEvent
import gg.skytils.skytilsmod._event.MainThreadPacketReceiveEvent
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.utils.*
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.network.packet.s2c.play.TeamS2CPacket
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

object ScoreCalculation : EventSubscriber {

    private val deathsTabPattern = Regex("§r§a§lTeam Deaths: §r§f(?<deaths>\\d+)§r")
    private val missingPuzzlePattern = Regex("§r§b§lPuzzles: §r§f\\((?<count>\\d)\\)§r")
    private val failedPuzzlePattern =
        Regex("§r (?<puzzle>.+): §r§7\\[§r§c§l✖§r§7] §.+")
    private val solvedPuzzlePattern =
        Regex("§r (?<puzzle>.+): §r§7\\[§r§a§l✔§r§7] §.+")
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

    // clear stuff
    var completedRooms = BasicState(0)
    var clearedPercentage = BasicState(0)
    val totalRoomMap = mutableMapOf<Int, Int>()
    val totalRooms = (clearedPercentage.zip(completedRooms)).map { (clear, complete) ->
        printDevMessage({ "total clear $clear complete $complete" }, "scorecalcroom")
        val a = if (clear > 0 && complete > 0) {
            (100 * (complete / clear.toDouble())).roundToInt()
        } else 0
        printDevMessage({ "total? $a" }, "scorecalcroom")
        if (a == 0) return@map 0
        totalRoomMap[a] = (totalRoomMap[a] ?: 0) + 1
        totalRoomMap.toList().maxByOrNull { it.second }!!.first
    }
    val calcingCompletedRooms = completedRooms.map {
        it + (!DungeonFeatures.hasBossSpawned).ifTrue(1) + (DungeonTimer.bloodClearTime == -1L).ifTrue(1)
    }
    val calcingClearPercentage = calcingCompletedRooms.map { complete ->
        val total = totalRooms.get()
        printDevMessage({ "total $total complete $complete" }, "scorecalcroom")
        val a = if (total > 0) (complete / total.toDouble()).coerceAtMost(1.0) else 0.0
        printDevMessage({ "calced room clear $a" }, "scorecalcroom")
        a
    }
    val roomClearScore = calcingClearPercentage.map {
        (60 * it).coerceIn(0.0, 60.0)
    }

    // secrets stuff
    var floorReq = BasicState(floorRequirements["default"]!!)
    var foundSecrets: State<Int> = BasicState(0).also { state ->
        state.onSetValue {
            updateText(totalScore.get())
        }
    }
    var totalSecrets = BasicState(0)
    var totalSecretsNeeded = (floorReq.zip(totalSecrets)).map { (req, total) ->
        if (total == 0) return@map 1
        ceil(total * req.secretPercentage).toInt()
    }
    val percentageOfNeededSecretsFound = (foundSecrets.zip(totalSecretsNeeded)).map { (found, totalNeeded) ->
        found / totalNeeded.toDouble()
    }
    val secretScore = (totalSecrets.zip(percentageOfNeededSecretsFound)).map { (total, percent) ->
        if (total <= 0)
            0.0
        else
            (40f * percent).coerceIn(0.0, 40.0)
    }


    val discoveryScore = (roomClearScore.zip(secretScore)).map { (clear, secret) ->
        printDevMessage({ "clear $clear secret $secret" }, "scorecalcexplore")
        if (DungeonFeatures.dungeonFloor == "E") (clear * 0.7).toInt() + (secret * 0.7).toInt()
        else clear.toInt() + secret.toInt()
    }


    // death stuff
    var deaths = BasicState(0)
    var firstDeathHadSpirit = BasicState(false)
    val deathPenalty = (deaths.zip(firstDeathHadSpirit)).map { (deathCount, spirit) ->
        (2 * deathCount) - spirit.ifTrue(1)
    }

    // puzzle stuff
    var missingPuzzles = BasicState(0).also {
        it.onSetValue {
            printDevMessage({ "missing puzzles $it" }, "scorecalcpuzzle")
        }
    }
    var failedPuzzles = BasicState(0)
    val puzzlePenalty = (missingPuzzles.zip(failedPuzzles)).map { (missing, failed) ->
        printDevMessage("puzzle penalty changed", "scorecalcpuzzle")
        10 * (missing + failed)
    }

    val skillScore = (calcingClearPercentage.zip(deathPenalty.zip(puzzlePenalty))).map { (clear, penalties) ->
        printDevMessage({ "puzzle penalty ${penalties.second}" }, "scorecalcpuzzle")
        if (DungeonFeatures.dungeonFloor == "E")
            ((20.0 + clear * 80.0 - penalties.first - penalties.second) * 0.7).toInt()
        else (20.0 + clear * 80.0 - penalties.first - penalties.second).toInt()
    }

    // speed stuff
    var secondsElapsed = BasicState(0.0)
    val overtime = (secondsElapsed.zip(floorReq)).map { (seconds, req) ->
        seconds - req.speed
    }
    val totalElapsed = (secondsElapsed.zip(floorReq)).map { (seconds, req) ->
        seconds + 480 - req.speed
    }
    val speedScore = totalElapsed.map { time ->
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

    // bonus stuff
    var crypts = BasicState(0)
    var mimicKilled = BasicState(false)
    var isPaul = BasicState(false)
    val bonusScore = (crypts.zip(mimicKilled.zip(isPaul))).map { (crypts, bools) ->
        ((if (bools.first) 2 else 0) + crypts.coerceAtMost(5) + if (bools.second) 10 else 0)
    }

    val totalScore =
        ((skillScore.zip(discoveryScore)).zip(speedScore.zip(bonusScore))).map { (first, second) ->
            printDevMessage({ "skill score ${first.first}" }, "scorecalcpuzzle")
            printDevMessage({
                "skill ${first.first} disc ${first.second} speed ${second.first} bonus ${second.second}" },
                "scorecalctotal"
            )
            if (DungeonFeatures.dungeonFloor == "E")
                first.first.coerceIn(14, 70) + first.second + second.first + ceil(second.second * 0.7).toInt()
            else first.first.coerceIn(20, 100) + first.second + second.first + second.second
        }.also { state ->
            state.onSetValue { score ->
                updateText(score)
            }
        }

    val rank: String
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

    fun updateText(score: Int) {}


    fun onScoreboardChange(event: MainThreadPacketReceiveEvent<*>) {
        if (
            !Utils.inSkyblock ||
            event.packet !is TeamS2CPacket
        ) return
        if (event.packet.playerListOperation != null || event.packet.teamOperation != null) return
        val line = event.packet.playerNames.joinToString(
            " ",
            prefix = event.packet.team.get().prefix.formattedText,
            postfix = event.packet.team.get().suffix.formattedText
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

    fun onTabChange(event: MainThreadPacketReceiveEvent<*>) {
        if (
            !Utils.inDungeons ||
            event.packet !is PlayerListS2CPacket ||
            PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME !in event.packet.actions
        ) return
        event.packet.entries.forEach { playerData ->
            val name = playerData.displayName?.formattedText ?: return@forEach
            printDevMessage(name, "scorecalctab")
            when {
                name.contains("Deaths:") -> {
                    val matcher = deathsTabPattern.find(name) ?: return@forEach
                    deaths.set(matcher.groups["deaths"]?.value?.toIntOrNull() ?: 0)
                }

                name.contains("Puzzles:") -> {
                    val matcher = missingPuzzlePattern.find(name) ?: return@forEach
                    missingPuzzles.set(matcher.groups["count"]?.value?.toIntOrNull() ?: 0)
                    printDevMessage({ "puzzles ${missingPuzzles.get()}" }, "scorecalcpuzzle")
                    updateText(totalScore.get())
                }

                name.contains("✔") -> {
                    if (solvedPuzzlePattern.containsMatchIn(name)) {
                        missingPuzzles.set((missingPuzzles.get() - 1).coerceAtLeast(0))
                    }
                }

                name.contains("✖") -> {
                    if (failedPuzzlePattern.containsMatchIn(name)) {
                        missingPuzzles.set((missingPuzzles.get() - 1).coerceAtLeast(0))
                        failedPuzzles.set(failedPuzzles.get() + 1)
                    }
                }

                name.contains("Secrets Found:") -> {
                    printDevMessage(name, "scorecalcsecrets")
                    if (name.contains("%")) {
                        val matcher = secretsFoundPercentagePattern.find(name) ?: return@forEach
                        val percentagePer = (matcher.groups["percentage"]?.value?.toDoubleOrNull()
                            ?: 0.0)
                        printDevMessage({ "percent $percentagePer" }, "scorecalcsecrets")
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
                    printDevMessage({ "count ${completedRooms.get()} percent ${clearedPercentage.get()}" }, "scorecalc")
                    printDevMessage({ "Total rooms: ${totalRooms.get()}" }, "scorecalc")
                }
            }
        }
    }

/*    fun onTitle(event: MainThreadPacketReceiveEvent<*>) {
        if (!Utils.inDungeons || event.packet !is TitleS2CPacket || event.packet.action != TitleS2CPacket.Action.TITLE) return
        if (event.packet.text.method_10865() == "§eYou became a ghost!§r") {
            if (DungeonListener.hutaoFans.getIfPresent(mc.player.name) == true
                && DungeonListener.team[mc.player.name]?.deaths == 0
            ) firstDeathHadSpirit.set(
                true
            )
            printDevMessage({ "you died. spirit: ${firstDeathHadSpirit.get()}" }, "scorecalcdeath")
        }
    }*/

    init {
        tickTimer(5, repeats = true) {
            isPaul.set(
                (MayorInfo.allPerks.contains("EZPZ")) || MayorInfo.jerryMayor?.name == "Paul"
            )
        }
    }

    fun onChatReceived(event: ChatMessageReceivedEvent) {
        if (!Utils.inDungeons || mc.player == null) return
        val unformatted = event.message.string.stripControlCodes()
        if (unformatted.startsWith("Party > ") || (unformatted.contains(":") && !unformatted.contains(">"))) {
            if (unformatted.contains("\$SKYTILS-DUNGEON-SCORE-MIMIC$") || unformatted.containsAny(
                    "Mimic dead!", "Mimic Killed!", "Mimic Dead!"
                )
            ) {
                mimicKilled.set(true)
                return
            }
            if (unformatted.contains("\$SKYTILS-DUNGEON-SCORE-ROOM$")) {
                event.cancelled = true
                return
            }
        }
    }

    fun onPuzzleReset(event: DungeonPuzzleResetEvent) {
        missingPuzzles.set(missingPuzzles.get() + 1)
        failedPuzzles.set((failedPuzzles.get() - 1).coerceAtLeast(0))
    }

    fun clearScore(event: WorldUnloadEvent) {
        mimicKilled.set(false)
        firstDeathHadSpirit.set(false)
        floorReq.set(floorRequirements["default"]!!)
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

    data class FloorRequirement(val secretPercentage: Double = 1.0, val speed: Int = 10 * 60)

    private fun Boolean.ifTrue(num: Int) = if (this) num else 0

    override fun setup() {
        register(::clearScore)
        register(::onPuzzleReset)
        register(::onScoreboardChange)
        register(::onTabChange)
        register(::onChatReceived)
    }
}
