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
package skytils.skytilsmod.features.impl.dungeons

import gg.essential.elementa.state.*
import gg.essential.universal.UResolution
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.network.play.server.S3EPacketTeams
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.impl.PacketEvent
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import kotlin.math.*

object ScoreCalculation {

    private var ticks = 0

    private val deathsTabPattern = Regex("§r§a§lDeaths: §r§f\\((?<deaths>\\d+)\\)§r")
    private val missingPuzzlePattern = Regex("§r§b§lPuzzles: §r§f\\((?<count>\\d)\\)§r")
    private val failedPuzzlePattern =
        Regex("§r (?<puzzle>.+): §r§7\\[§r§c§l✖§r§7] §.+")
    private val solvedPuzzlePattern =
        Regex("§r (?<puzzle>.+): §r§7\\[§r§a§l✔§r§7] §.+")
    private val secretsFoundPattern = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val secretsFoundPercentagePattern = Regex("§r Secrets Found: §r§[ae](?<percentage>[\\d.]+)%§r")
    private val cryptsPattern = Regex("§r Crypts: §r§6(?<crypts>\\d+)§r")
    private val dungeonClearedPattern = Regex("Dungeon Cleared: (?<percentage>\\d+)%")
    private val timeElapsedPattern =
        Regex(" Elapsed: (?:(?<hrs>\\d+)h )?(?:(?<min>\\d+)m )?(?:(?<sec>\\d+)s)?")
    private val roomCompletedPattern = Regex("§r Completed Rooms: §r§d(?<count>\\d+)§r")

    val floorRequirements = hashMapOf(
        // idk what entrance is so i put it as the same as f1
        "E" to FloorRequirement(.3),
        "F1" to FloorRequirement(.3),
        "F2" to FloorRequirement(.4),
        "F3" to FloorRequirement(.5),
        "F4" to FloorRequirement(.6, 12 * 60),
        "F5" to FloorRequirement(.7),
        "F6" to FloorRequirement(.85),
        "F7" to FloorRequirement(speed = 12 * 60),
        "M1" to FloorRequirement(speed = 8 * 60),
        "M2" to FloorRequirement(speed = 8 * 60),
        "M3" to FloorRequirement(speed = 8 * 60),
        "M4" to FloorRequirement(speed = 8 * 60),
        "M5" to FloorRequirement(speed = 8 * 60),
        "M6" to FloorRequirement(speed = 8 * 60),
        //still waiting on m7 release lol
        "M7" to FloorRequirement(speed = 8 * 60),
        "default" to FloorRequirement()
    )


    // clear stuff
    var completedRooms = BasicState(0)
    var clearedPercentage = BasicState(0)
    var totalRooms = (completedRooms.zip(clearedPercentage)).map { (complete, clear) ->
        if (complete > 0 && clear > 0) {
            (100 * (complete / clear.toDouble())).roundToInt()
        } else 0
    }
    val calcingCompletedRooms = completedRooms.map {
        it + (!DungeonFeatures.hasBossSpawned).ifTrue(1) + (DungeonTimer.bloodClearTime == -1L).ifTrue(1)
    }
    val calcingClearPercentage = calcingCompletedRooms.map { complete ->
        val total = totalRooms.get()
        printDevMessage("total $total complete $complete", "scorecalcroom")
        val a = if (total > 0) (complete / total.toDouble()).coerceAtMost(1.0) else 0.0
        printDevMessage("calced room clear $a", "scorecalcroom")
        a
    }
    val roomClearScore = calcingClearPercentage.map {
        (60 * it).coerceIn(0.0, 60.0)
    }

    // secrets stuff
    var floorReq = BasicState(floorRequirements["default"]!!)
    var foundSecrets: State<Int> = BasicState(0).also { state ->
        state.onSetValue {
            updateText(totalScore.get().toInt())
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
        printDevMessage("clear $clear secret $secret", "scorecalcexplore")
        (clear + secret).roundToInt()
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
            printDevMessage("missing puzzles $it", "scorecalcpuzzle")
        }
    }
    var failedPuzzles = BasicState(0)
    val puzzlePenalty = (missingPuzzles.zip(failedPuzzles)).map { (missing, failed) ->
        printDevMessage("puzzle penalty changed", "scorecalcpuzzle")
        10 * (missing + failed)
    }

    val skillScore = (calcingClearPercentage.zip(deathPenalty.zip(puzzlePenalty))).map { (clear, penalties) ->
        printDevMessage("puzzle penalty ${penalties.second}", "scorecalcpuzzle")
        (20.0 + clear * 80.0 - penalties.first - penalties.second)
            .roundToInt()
    }

    // speed stuff
    var secondsElapsed = BasicState(0.0)
    val overtime = (secondsElapsed.zip(floorReq)).map { (seconds, req) ->
        seconds - req.speed
    }

    // formula works in F6, but calc lower in F7
    // TODO: 1/10/22 Fix this to not use a magic number
    val weirdOffset: Int
        get() = if (Utils.equalsOneOf(DungeonFeatures.dungeonFloor, "F7", "M7")) 7 else 6
    val arbitraryCalculation = overtime.map {
        ((-5.0 * weirdOffset + sqrt((5.0 * weirdOffset).pow(2) + 20.0 * weirdOffset * it)) / (10.0 * weirdOffset)).toInt()
    }

    val speedScore = (overtime.zip(arbitraryCalculation)).map { (over, calculation) ->
        (100 - 10 * calculation - (over - (5 * weirdOffset * calculation + 5 * weirdOffset * calculation * calculation)) / ((calculation + 1) * weirdOffset)).coerceIn(
            0.0,
            100.0
        )
    }

    // bonus stuff
    var crypts = BasicState(0)
    var mimicKilled = BasicState(false)
    var isPaul = BasicState(false)
    val bonusScore = (crypts.zip(mimicKilled.zip(isPaul))).map { (crypts, bools) ->
        (if (bools.first) 2 else 0) + crypts.coerceAtMost(5) + if (bools.second) 10 else 0
    }

    var hasSaid270 = false
    var hasSaid300 = false

    val totalScore =
        ((skillScore.zip(discoveryScore)).zip(speedScore.zip(bonusScore))).map { (first, second) ->
            printDevMessage("skill score ${first.first}", "scorecalcpuzzle")
            first.first.coerceIn(20, 100) + first.second + second.first + second.second
        }.also { state ->
            state.onSetValue { score ->
                if (Skytils.config.sendMessageOn270Score && !hasSaid270 && score >= 270) {
                    hasSaid270 = true
                    Skytils.sendMessageQueue.add("/pc Skytils > 270 score")
                }
                if (Skytils.config.sendMessageOn300Score && !hasSaid300 && score >= 300) {
                    hasSaid300 = true
                    Skytils.sendMessageQueue.add("/pc Skytils > 300 score")
                }
                updateText(score.toInt())
            }
        }

    val rank: State<String> = totalScore.map {
        when {
            it < 100 -> "§cD"
            it < 160 -> "§9C"
            it < 230 -> "§aB"
            it < 270 -> "§5A"
            it < 300 -> "§eS"
            else -> "§6S+"
        }
    }

    fun updateText(score: Int) {
        Utils.checkThreadAndQueue {
            ScoreCalculationElement.text.clear()
            if (Skytils.config.minimizedScoreCalculation) {
                val color = when {
                    score < 270 -> 'c'
                    score < 300 -> 'e'
                    else -> 'a'
                }
                ScoreCalculationElement.text.add("§6Score: §$color$score §7(${rank.get()}§7)")
            } else {
                ScoreCalculationElement.text.add("§9Dungeon Status")
                ScoreCalculationElement.text.add("§f• §eDeaths:§c ${deaths.get()} ${if (firstDeathHadSpirit.get()) "§7(§6Spirit§7)" else ""}")
                ScoreCalculationElement.text.add("§f• §eMissing Puzzles:§c ${missingPuzzles.get()}")
                ScoreCalculationElement.text.add("§f• §eFailed Puzzles:§c ${failedPuzzles.get()}")
                if (discoveryScore.get() > 0) ScoreCalculationElement.text.add("§f• §eSecrets: ${if (foundSecrets.get() >= totalSecretsNeeded.get()) "§a" else "§c"}${foundSecrets.get()}§7/§a${totalSecretsNeeded.get()} §7(§6Total: ${totalSecrets.get()}§7)")
                ScoreCalculationElement.text.add("§f• §eCrypts:§a ${crypts.get()}")
                if (Utils.equalsOneOf(DungeonFeatures.dungeonFloor, "F6", "F7", "M6", "M7")) {
                    ScoreCalculationElement.text.add("§f• §eMimic:${if (mimicKilled.get()) "§a ✓" else " §c X"}")
                }
                ScoreCalculationElement.text.add("")
                ScoreCalculationElement.text.add("§6Score:")
                ScoreCalculationElement.text.add("§f• §eSkill Score:§a ${skillScore.get().coerceIn(20, 100)}")
                ScoreCalculationElement.text.add(
                    "§f• §eExplore Score:§a ${discoveryScore.get()} §7(§e${
                        roomClearScore.get().roundToInt()
                    } §7+ §6${secretScore.get().roundToInt()}§7)"
                )
                ScoreCalculationElement.text.add("§f• §eSpeed Score:§a ${speedScore.get()}")
                ScoreCalculationElement.text.add("§f• §eBonus Score:§a ${bonusScore.get()}")
                ScoreCalculationElement.text.add("§f• §eTotal Score:§a $score" + if (isPaul.get()) " §7(§6+10§7)" else "")
                ScoreCalculationElement.text.add("§f• §eRank: ${rank.get()}")

            }
        }
    }


    @SubscribeEvent
    fun onScoreboardChange(event: PacketEvent.ReceiveEvent) {
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
        if (line.startsWith("Dungeon Cleared: ")) {
            val matcher = dungeonClearedPattern.find(line)
            if (matcher != null) {
                clearedPercentage.set(matcher.groups["percentage"]?.value?.toIntOrNull() ?: 0)
                return
            }
        }
        if (line.startsWith("Time Elapsed:")) {
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
    fun onTabChange(event: PacketEvent.ReceiveEvent) {
        if (
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
                    println(name)
                    val matcher = missingPuzzlePattern.find(name) ?: return@forEach
                    missingPuzzles.set(matcher.groups["count"]?.value?.toIntOrNull() ?: 0)
                    printDevMessage("puzzles ${missingPuzzles.get()}", "scorecalcpuzzle")
                }
                name.contains("✔") -> {
                    if (solvedPuzzlePattern.containsMatchIn(name)) {
                        missingPuzzles.set(missingPuzzles.get() - 1)
                    }
                }
                name.contains("✖") -> {
                    if (failedPuzzlePattern.containsMatchIn(name)) {
                        missingPuzzles.set(missingPuzzles.get() - 1)
                        failedPuzzles.set(failedPuzzles.get() + 1)
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
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (mc.thePlayer != null && mc.theWorld != null && Utils.inDungeons) {
            if (Skytils.config.showScoreCalculation && ticks % 5 == 0) {
                isPaul.set(
                    (MayorInfo.currentMayor == "Paul" && MayorInfo.mayorPerks.contains("EZPZ")) || MayorInfo.jerryMayor?.name == "Paul"
                )
                ticks = 0
            }
        }
        ticks++
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChatReceived(event: ClientChatReceivedEvent) {
        if (!Utils.inDungeons || mc.thePlayer == null) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (Skytils.config.scoreCalculationReceiveAssist) {
            if (unformatted.startsWith("Party > ")) {
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

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        if (!Utils.inDungeons) return
        if (event.entity is EntityZombie) {
            val entity = event.entity as EntityZombie
            if (entity.isChild && entity.getCurrentArmor(0) == null && entity.getCurrentArmor(1) == null && entity.getCurrentArmor(
                    2
                ) == null && entity.getCurrentArmor(3) == null
            ) {
                if (!mimicKilled.get()) {
                    mimicKilled.set(true)
                    if (Skytils.config.scoreCalculationAssist) {
                        Skytils.sendMessageQueue.add("/pc \$SKYTILS-DUNGEON-SCORE-MIMIC$")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        mimicKilled.set(false)
        firstDeathHadSpirit.set(false)
        floorReq.set(floorRequirements["default"]!!)
        if (Utils.inDungeons) {
            missingPuzzles.set(0)
            failedPuzzles.set(0)
        }
        hasSaid270 = false
        hasSaid300 = false
    }

    init {
        ScoreCalculationElement()
        HugeCryptsCounter()
    }

    class HugeCryptsCounter : GuiElement("Dungeon Crypts Counter", 2f, FloatPair(200, 200)) {
        override fun render() {
            if (toggled && Utils.inDungeons && DungeonTimer.dungeonStartTime != -1L) {
                val sr = UResolution
                val leftAlign = actualX < sr.scaledWidth / 2f
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
            val sr = UResolution
            val leftAlign = actualX < sr.scaledWidth / 2f
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

    class ScoreCalculationElement : GuiElement("Dungeon Score Estimate", FloatPair(200, 100)) {
        override fun render() {
            if (toggled && Utils.inDungeons) {
                RenderUtil.drawAllInList(this, text)
            }
        }

        override fun demoRender() {
            RenderUtil.drawAllInList(this, demoText)
        }

        companion object {
            private val demoText = listOf(
                "§9Dungeon Status",
                "§f• §eDeaths:§c 0",
                "§f• §eMissing Puzzles:§c 0",
                "§f• §eFailed Puzzles:§c 0",
                "§f• §eSecrets: §a50§7/§a50 §7(§6Total: 50§7)",
                "§f• §eCrypts:§a 5",
                "§f• §eMimic: §a ✓",
                "",
                "§6Score:",
                "§f• §eSkill Score:§a 100",
                "§f• §eExplore Score:§a 100 §7(§e60 §7+ §640§7)",
                "§f• §eSpeed Score:§a 100",
                "§f• §eBonus Score:§a 17",
                "§f• §eTotal Score:§a 317 §7(§6+10§7)",
                "§f• §eRank: §6S+"
            )
            val text = ArrayList<String>()
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * 4
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§f• §eExplore Score:§a 100 §7(§e60 §7+ §640§7)")

        override val toggled: Boolean
            get() = Skytils.config.showScoreCalculation

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    data class FloorRequirement(val secretPercentage: Double = 1.0, val speed: Int = 10 * 60)

    private fun Boolean.ifTrue(num: Int) = if (this) num else 0
}