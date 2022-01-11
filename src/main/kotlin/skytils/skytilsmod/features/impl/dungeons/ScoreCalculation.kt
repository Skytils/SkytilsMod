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

import gg.essential.universal.UResolution
import net.minecraft.entity.monster.EntityZombie
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
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import kotlin.math.*

object ScoreCalculation {

    private var ticks = 0

    private val deathsTabPattern = Regex("§r§a§lDeaths: §r§f\\((?<deaths>\\d+)\\)§r")
    private val missingPuzzlePattern = Regex("§r (?<puzzle>.+): §r§7\\[§r§6§l✦§r§7] ?§r")
    private val failedPuzzlePattern =
        Regex("§r (?<puzzle>.+): §r§7\\[§r§c§l✖§r§7] §.+")
    private val secretsFoundPattern = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val secretsFoundPercentagePattern = Regex("§r Secrets Found: §r§[ae](?<percentage>[\\d.]+)%§r")
    private val cryptsPattern = Regex("§r Crypts: §r§6(?<crypts>\\d+)§r")
    private val dungeonClearedPattern = Regex("Dungeon Cleared: (?<percentage>\\d+)%")
    private val timeElapsedPattern =
        Regex("Time Elapsed: (?:(?<hrs>\\d+)h )?(?:(?<min>\\d+)m )?(?:(?<sec>\\d+)s)?")
    private val roomCompletedPattern = Regex("§r Completed Rooms: §r§d(?<count>\\d+)§r")

    val floorRequirements = hashMapOf(
        // idk what entrance is so i put it as the same as f1
        "E" to FloorRequirement(.3),
        "F1" to FloorRequirement(.3),
        "F2" to FloorRequirement(.4),
        "F3" to FloorRequirement(.5),
        "F4" to FloorRequirement(.6),
        "F5" to FloorRequirement(.7,  12 * 60),
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

    var deaths = 0
    var missingPuzzles = 0
    var failedPuzzles = 0
    var foundSecrets = 0
    var totalSecrets = 0
    var crypts = 0
    var mimicKilled = false
    var firstDeathHadSpirit = false
    var clearedPercentage = 0
    var secondsElapsed = 0.0
    var isPaul = false
    var skillScore = 0
    var percentageSecretsFound = 0.0
    var totalSecretsNeeded = 0
    var discoveryScore = 0
    var speedScore = 0
    var bonusScore = 0
    var completedRooms = 0
    var totalRooms = 0

    var sent270Message = false
    var sent300Message = false

    var floorReq = floorRequirements["default"]!!

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (mc.thePlayer != null && mc.theWorld != null && Utils.inDungeons) {
            if (Skytils.config.showScoreCalculation && ticks % 5 == 0) {
                missingPuzzles = 0
                failedPuzzles = 0
                for (line in ScoreboardUtil.sidebarLines) {
                    if (line.startsWith("Dungeon Cleared:")) {
                        val matcher = dungeonClearedPattern.find(line)
                        if (matcher != null) {
                            clearedPercentage = matcher.groups["percentage"]?.value?.toIntOrNull() ?: 0
                            continue
                        }
                    }
                    if (line.startsWith("Time Elapsed:")) {
                        val matcher = timeElapsedPattern.find(line)
                        if (matcher != null) {
                            val hours = matcher.groups["hrs"]?.value?.toIntOrNull() ?: 0
                            val minutes = matcher.groups["min"]?.value?.toIntOrNull() ?: 0
                            val seconds = matcher.groups["sec"]?.value?.toIntOrNull() ?: 0
                            secondsElapsed = (hours * 3600 + minutes * 60 + seconds).toDouble()
                            continue
                        }
                    }
                }
                for ((_, name) in TabListUtils.tabEntries) {
                    when {
                        name.contains("Deaths:") -> {
                            val matcher = deathsTabPattern.find(name) ?: continue
                            deaths = matcher.groups["deaths"]?.value?.toIntOrNull() ?: 0
                        }
                        name.contains("✦") -> {
                            if (missingPuzzlePattern.containsMatchIn(name)) {
                                missingPuzzles++
                            }
                        }
                        name.contains("✖") -> {
                            if (failedPuzzlePattern.containsMatchIn(name)) {
                                failedPuzzles++
                            }
                        }
                        name.contains("Secrets Found:") -> {
                            if (name.contains("%")) {
                                val matcher = secretsFoundPercentagePattern.find(name) ?: continue
                                val percentagePer = (matcher.groups["percentage"]?.value?.toDoubleOrNull()
                                    ?: 0.0)
                                totalSecrets =
                                    if (foundSecrets > 0 && percentagePer > 0) floor(100f / percentagePer * foundSecrets + 0.5).toInt() else 0
                            } else {
                                val matcher = secretsFoundPattern.find(name) ?: continue
                                foundSecrets = matcher.groups["secrets"]?.value?.toIntOrNull() ?: 0
                            }
                        }
                        name.contains("Crypts:") -> {
                            val matcher = cryptsPattern.find(name) ?: continue
                            crypts = matcher.groups["crypts"]?.value?.toIntOrNull() ?: 0
                        }
                        name.contains("Completed Rooms") -> {
                            val matcher = roomCompletedPattern.find(name) ?: continue
                            completedRooms = matcher.groups["count"]?.value?.toIntOrNull() ?: continue
                            totalRooms = if (completedRooms > 0 && clearedPercentage > 0) {
                                (100 * (completedRooms / clearedPercentage.toDouble())).roundToInt()
                            } else 0
                            printDevMessage(totalRooms.toString(), "scorecalc")
                        }
                    }
                }
                val calcingCompletedRooms =
                    completedRooms + (!DungeonFeatures.hasBossSpawned).ifTrue(1) + (DungeonTimer.bloodClearTime == -1L).ifTrue(
                        1
                    )
                val calcingClearedPercentage =
                    if (totalRooms > 0) (calcingCompletedRooms / totalRooms.toDouble()).coerceAtMost(1.0) else 0.0
                printDevMessage(calcingClearedPercentage.toString(), "scorecalc")
                isPaul =
                    (MayorInfo.currentMayor == "Paul" && MayorInfo.mayorPerks.contains("EZPZ")) || MayorInfo.jerryMayor?.name == "Paul"
                val deathPenalty = (2 * deaths) - firstDeathHadSpirit.ifTrue(1)
                val puzzlePenalty = 10 * (missingPuzzles + failedPuzzles)
                skillScore =
                    (20 + calcingClearedPercentage * 80 - deathPenalty - puzzlePenalty)
                        .coerceIn(20.0, 100.0).roundToInt()
                totalSecretsNeeded = ceil(totalSecrets * floorReq.secretPercentage).toInt()
                percentageSecretsFound = foundSecrets / totalSecretsNeeded.toDouble()
                val roomClearScore = (60 * calcingClearedPercentage).coerceIn(0.0, 60.0)
                val secretScore = if (totalSecrets <= 0) 0.0 else
                    (40f * percentageSecretsFound).coerceIn(0.0, 40.0)
                discoveryScore = (roomClearScore + secretScore).roundToInt()
                bonusScore = (if (mimicKilled) 2 else 0) + crypts.coerceAtMost(5) + if (isPaul) 10 else 0

                // formula works in F6, but calc lower in F7
                val overtime = secondsElapsed - floorReq.speed
                val t = if (Utils.equalsOneOf(DungeonFeatures.dungeonFloor, "F7", "M7")) 7 else 6 // value not correct in f7
                val x = ((-5.0 * t + sqrt((5.0 * t).pow(2) + 20.0 * t * overtime)) / (10.0 * t)).toInt()
                speedScore =
                    (100 - 10 * x - (overtime - (5 * t * x + 5 * t * x * x)) / ((x + 1) * t)).toInt().coerceIn(0, 100)

                val totalScore = (skillScore + discoveryScore + speedScore + bonusScore)

                val rank = when {
                    totalScore < 100 -> "§cD"
                    totalScore < 160 -> "§9C"
                    totalScore < 230 -> "§aB"
                    totalScore < 270 -> "§5A"
                    totalScore < 300 -> "§eS"
                    else -> "§6S+"
                }

                if (Skytils.config.sendMessageOn270Score && !sent270Message && totalScore >= 270) {
                    sent270Message = true
                    Skytils.sendMessageQueue.add("/pc Skytils > 270 score")
                }
                if (Skytils.config.sendMessageOn300Score && !sent300Message && totalScore >= 300) {
                    sent300Message = true
                    Skytils.sendMessageQueue.add("/pc Skytils > 300 score")
                }

                ScoreCalculationElement.text.clear()
                if (Skytils.config.minimizedScoreCalculation) {
                    val color = when {
                        totalScore < 270 -> 'c'
                        totalScore < 300 -> 'e'
                        else -> 'a'
                    }
                    ScoreCalculationElement.text.add("§6Score: §$color$totalScore §7(${rank}§7)")
                } else {
                    ScoreCalculationElement.text.add("§9Dungeon Status")
                    ScoreCalculationElement.text.add("§f• §eDeaths:§c $deaths ${if (firstDeathHadSpirit) "§7(§6Spirit§7)" else ""}")
                    ScoreCalculationElement.text.add("§f• §eMissing Puzzles:§c $missingPuzzles")
                    ScoreCalculationElement.text.add("§f• §eFailed Puzzles:§c $failedPuzzles")
                    if (discoveryScore > 0) ScoreCalculationElement.text.add("§f• §eSecrets: ${if (foundSecrets >= totalSecretsNeeded) "§a" else "§c"}$foundSecrets§7/§a${totalSecretsNeeded} §7(§6Total: ${totalSecrets}§7)")
                    ScoreCalculationElement.text.add("§f• §eCrypts:§a $crypts")
                    if (Utils.equalsOneOf(DungeonFeatures.dungeonFloor, "F6", "F7", "M6", "M7")) {
                        ScoreCalculationElement.text.add("§f• §eMimic:${if (mimicKilled) "§a ✓" else " §c X"}")
                    }
                    ScoreCalculationElement.text.add("")
                    ScoreCalculationElement.text.add("§6Score:")
                    ScoreCalculationElement.text.add("§f• §eSkill Score:§a $skillScore")
                    ScoreCalculationElement.text.add("§f• §eExplore Score:§a $discoveryScore §7(§e${roomClearScore.roundToInt()} §7+ §6${secretScore.roundToInt()}§7)")
                    ScoreCalculationElement.text.add("§f• §eSpeed Score:§a $speedScore")
                    ScoreCalculationElement.text.add("§f• §eBonus Score:§a $bonusScore")
                    ScoreCalculationElement.text.add("§f• §eTotal Score:§a $totalScore" + if (isPaul) " §7(§6+10§7)" else "")
                    ScoreCalculationElement.text.add("§f• §eRank: $rank")
                }
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
                    mimicKilled = true
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
                if (!mimicKilled) {
                    mimicKilled = true
                    if (Skytils.config.scoreCalculationAssist) {
                        Skytils.sendMessageQueue.add("/pc \$SKYTILS-DUNGEON-SCORE-MIMIC$")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        mimicKilled = false
        firstDeathHadSpirit = false
        floorReq = floorRequirements["default"]!!
        sent270Message = false
        sent300Message = false
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
                    "Crypts: $crypts",
                    if (leftAlign) 0f else width.toFloat(),
                    0f,
                    alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT,
                    customColor = if (crypts < 5) CommonColors.RED else CommonColors.LIGHT_GREEN
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
