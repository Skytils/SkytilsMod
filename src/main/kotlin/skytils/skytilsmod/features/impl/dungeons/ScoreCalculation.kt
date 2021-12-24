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
package skytils.skytilsmod.features.impl.dungeons

import gg.essential.universal.UResolution
import net.minecraft.client.Minecraft
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.world.World
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.util.regex.Pattern
import kotlin.math.floor
import kotlin.math.roundToInt

object ScoreCalculation {

    private val partyAssistSecretsPattern: Pattern =
        Pattern.compile("^Party > .+: \\\$SKYTILS-DUNGEON-SCORE-ROOM\\$: \\[(?<name>.+)] \\((?<secrets>\\d+)\\)$")!!
    private val mc = Minecraft.getMinecraft()
    private var ticks = 0

    private val deathsTabPattern = Regex("§r§a§lDeaths: §r§f\\((?<deaths>\\d+)\\)§r")
    private val missingPuzzlePattern = Regex("§r (?<puzzle>.+): §r§7\\[§r§6§l✦§r§7] ?§r")
    private val failedPuzzlePattern =
        Regex("§r (?<puzzle>.+): §r§7\\[§r§c§l✖§r§7] §r§f(?:\\((?:§r(?<player>.+))?§r§f\\)|\\(§r§f})§r")
    private val secretsFoundPattern = Regex("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val secretsFoundPercentagePattern = Regex("§r Secrets Found: §r§[ae](?<percentage>[\\d.]+)%§r")
    private val cryptsPattern = Regex("§r Crypts: §r§6(?<crypts>\\d+)§r")

    val floorRequirements = hashMapOf(
        // idk what entrance is so i put it as the same as f1
        "E" to FloorRequirement(.3),
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

    var floorReq = floorRequirements["default"]!!

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (mc.thePlayer != null && mc.theWorld != null && Utils.inDungeons) {
            if (ticks % 2 == 0) {
                missingPuzzles = 0
                failedPuzzles = 0
                for (pi in TabListUtils.tabEntries) {
                    try {
                        val name = pi.text
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
                                        ?: 0.0) / foundSecrets
                                    totalSecrets =
                                        if (percentagePer > 0.0) (100 / percentagePer).roundToInt() else 0
                                } else {
                                    val matcher = secretsFoundPattern.find(name) ?: continue
                                    foundSecrets = matcher.groups["secrets"]?.value?.toIntOrNull() ?: 0
                                }
                            }
                            name.contains("Crypts:") -> {
                                val matcher = cryptsPattern.find(name) ?: continue
                                crypts = matcher.groups["crypts"]?.value?.toIntOrNull() ?: 0
                            }
                        }
                    } catch (ignored: NumberFormatException) {
                    }
                }
            }
        }
        ticks++
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChatReceived(event: ClientChatReceivedEvent) {
        if (!Utils.inDungeons || mc.thePlayer == null) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        try {
            if (Skytils.config.scoreCalculationReceiveAssist) {
                if (unformatted.startsWith("Party > ")) {
                    if (unformatted.contains("\$SKYTILS-DUNGEON-SCORE-MIMIC$")) {
                        mimicKilled = true
                        event.isCanceled = true
                        return
                    }
                    if (unformatted.contains("\$SKYTILS-DUNGEON-SCORE-ROOM$")) {
                        val matcher = partyAssistSecretsPattern.matcher(unformatted)
                        if (matcher.find()) {
                            event.isCanceled = true
                            return
                        }
                    }
                }
            }
        } catch (ignored: NumberFormatException) {
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (Skytils.config.removePartyChatNotifFromScoreCalc) {
            if (unformatted.startsWith("Party > ") && mc.thePlayer != null && !unformatted.contains(mc.thePlayer.name)) {
                mc.thePlayer.playSound("random.orb", 1f, 1f)
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
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inDungeons) return
        if (event.packet is S29PacketSoundEffect) {
            val packet = event.packet
            val sound = packet.soundName
            val pitch = packet.pitch
            val volume = packet.volume
            if (Skytils.config.removePartyChatNotifFromScoreCalc && sound == "random.orb" && pitch == 1f && volume == 1f) {
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        mimicKilled = false
        firstDeathHadSpirit = false
        floorReq = floorRequirements["default"]!!
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
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (toggled && Utils.inDungeons && player != null && world != null) {
                val sr = UResolution

                val leftAlign = actualX < sr.scaledWidth / 2f
                val text = ArrayList<String>()
                var clearedPercentage = 0
                var secondsElapsed = 0.0
                val isPaul =
                    (MayorInfo.currentMayor == "Paul" && MayorInfo.mayorPerks.contains("EZPZ")) || (MayorInfo.jerryMayor?.name
                        ?: "") == "Paul"
                for (l in ScoreboardUtil.sidebarLines) {
                    val line = ScoreboardUtil.cleanSB(l)
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
                val skillScore =
                    100 - (2 * deaths - if (firstDeathHadSpirit) 1 else 0) - 14 * (missingPuzzles + failedPuzzles)
                val percentageSecretsFound = foundSecrets / (totalSecrets * floorReq.secretPercentage)
                val discoveryScore: Double = floor(
                    (60 * (clearedPercentage / 100f)).toDouble().coerceIn(0.0, 60.0)
                ) + if (totalSecrets <= 0) 0.0 else floor(
                    (40f * percentageSecretsFound).coerceIn(0.0, 40.0)
                )
                val speedScore: Double
                val bonusScore = (if (mimicKilled) 2 else 0) + crypts.coerceAtMost(5) + if (isPaul) 10 else 0
                // no idea how speed score works soooo
                speedScore = 100 - ((secondsElapsed - floorReq.speed) / 3f).coerceIn(0.0, 100.0)
                if (deaths != 0) text.add("§6Deaths:§a $deaths")
                if (missingPuzzles != 0) text.add("§6Missing Puzzles:§a $missingPuzzles")
                if (failedPuzzles != 0) text.add("§6Failed Puzzles:§a $failedPuzzles")
                if (foundSecrets != 0) text.add("§6Secrets:§a${if (percentageSecretsFound >= 0.999999999) "§l" else ""} $foundSecrets")
                if (totalSecrets != 0) text.add("§6Total Secrets:§a $totalSecrets")
                text.add("§6Crypts:§a $crypts")
                if (Utils.equalsOneOf(DungeonFeatures.dungeonFloor, "F6", "F7", "M6", "M7")) {
                    text.add("§6Mimic:" + if (mimicKilled) "§a ✓" else " §c X")
                }
                if (isPaul) {
                    text.add("§6EZPZ: §a+10")
                }
                if (skillScore != 100) text.add("§6Skill:§a $skillScore")
                if (totalSecrets != 0) text.add("§6Discovery:§a " + discoveryScore.toInt())
                if (speedScore != 100.0) text.add("§6Speed:§a " + speedScore.toInt())
                if (bonusScore != 0) text.add("§6Bonus:§a $bonusScore")
                if (totalSecrets != 0) text.add("§6Total:§a " + (skillScore + discoveryScore + speedScore + bonusScore).toInt())
                for (i in text.indices) {
                    val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                    ScreenRenderer.fontRenderer.drawString(
                        text[i],
                        if (leftAlign) 0f else width.toFloat(),
                        (i * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                        CommonColors.WHITE,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                }
            }
        }

        override fun demoRender() {
            val sr = UResolution
            val leftAlign = actualX < sr.scaledWidth / 2f
            val text = ArrayList<String>()
            text.add("§6Secrets Found: 99")
            text.add("§6Estimated Secret Count: 99")
            text.add("§6Crypts: 99")
            text.add("§6Mimic Killed:§a ✓")
            for (i in text.indices) {
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    text[i],
                    if (leftAlign) 0f else width.toFloat(),
                    (i * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * 4
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§6Estimated Secret Count: 99")

        override val toggled: Boolean
            get() = Skytils.config.showScoreCalculation

        companion object {
            private val dungeonClearedPattern = Regex("Dungeon Cleared: (?<percentage>\\d+)%")
            private val timeElapsedPattern =
                Regex("Time Elapsed: (?:(?<hrs>\\d+)h )?(?:(?<min>\\d+)m )?(?:(?<sec>\\d+)s)?")
        }

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    data class FloorRequirement(val secretPercentage: Double = 1.0, val speed: Int = 10 * 60)
}