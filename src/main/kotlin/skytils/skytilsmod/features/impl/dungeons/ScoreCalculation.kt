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

import com.google.gson.Gson
import com.google.gson.JsonObject
import gg.essential.api.utils.Multithreading
import gg.essential.universal.UResolution
import net.minecraft.client.Minecraft
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.world.World
import net.minecraftforge.client.ClientCommandHandler
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
import skytils.skytilsmod.events.impl.AddChatMessageEvent
import skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import skytils.skytilsmod.events.impl.SendChatMessageEvent
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.lang.invoke.MethodHandle
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import kotlin.math.floor
import kotlin.math.pow

object ScoreCalculation {

    private val partyAssistSecretsPattern: Pattern =
        Pattern.compile("^Party > .+: \\\$SKYTILS-DUNGEON-SCORE-ROOM\\$: \\[(?<name>.+)] \\((?<secrets>\\d+)\\)$")!!
    private var rooms = ConcurrentHashMap<String, Int>()
    private val mc = Minecraft.getMinecraft()
    private var ticks = 0
    private val JSON_BRACKET_PATTERN = Pattern.compile("\\{.+}")
    private var lastRoomScanPos: BlockPos? = null

    private val deathsTabPattern = Pattern.compile("§r§a§lDeaths: §r§f\\((?<deaths>\\d+)\\)§r")
    private val missingPuzzlePattern = Pattern.compile("§r (?<puzzle>.+): §r§7\\[§r§6§l✦§r§7]§r")
    private val failedPuzzlePattern =
        Pattern.compile("§r (?<puzzle>.+): §r§7\\[§r§c§l✖§r§7] §r§f\\((?:§r(?<player>.+))?§r§f\\)§r")
    private val secretsFoundPattern = Pattern.compile("§r Secrets Found: §r§b(?<secrets>\\d+)§r")
    private val cryptsPattern = Pattern.compile("§r Crypts: §r§6(?<crypts>\\d+)§r")

    var deaths = 0
    var missingPuzzles = 0
    var failedPuzzles = 0
    var foundSecrets = 0
    val totalSecrets: Int
        get() = rooms.values.sum()
    var crypts = 0
    var mimicKilled = false

    var drmRoomScanMethod: MethodHandle? = null

    private fun roomScanCallback(list: List<String>) {
        if (Skytils.config.scoreCalculationMethod != 1) return
        Utils.checkThreadAndQueue {
            for (room in list) {
                if (!rooms.containsKey(room)) {
                    val secrets = room.substringAfterLast("-").toIntOrNull() ?: 0
                    rooms[room] = secrets
                    if (Skytils.config.scoreCalculationAssist && false) {
                        Skytils.sendMessageQueue.add("/pc \$SKYTILS-DUNGEON-SCORE-ROOM$: [$room] ($secrets)")
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onAddChatMessage(event: AddChatMessageEvent) {
        if (!Utils.inDungeons || Skytils.config.scoreCalculationMethod != 0) return
        try {
            val unformatted = event.message.unformattedText.stripControlCodes()
            if (unformatted == "null" || unformatted.startsWith("Dungeon Rooms: Use this command in dungeons")) {
                event.isCanceled = true
            }
            if (event.message.unformattedText.contains("{") && event.message.unformattedText.contains("}")) {
                val matcher = JSON_BRACKET_PATTERN.matcher(event.message.unformattedText)
                if (matcher.find()) {
                    val obj = Gson().fromJson(unformatted, JsonObject::class.java)
                    if (obj.has("name") && obj.has("category") && obj.has("secrets")) {
                        val name = obj["name"].asString
                        val secrets = obj["secrets"].asInt
                        if (!rooms.containsKey(name)) {
                            rooms[name] = secrets
                            if (Skytils.config.scoreCalculationAssist && Skytils.config.scoreCalculationMethod == 0) {
                                Skytils.sendMessageQueue.add("/pc \$SKYTILS-DUNGEON-SCORE-ROOM$: [$name] ($secrets)")
                            }
                        }
                        event.isCanceled = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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
                                val matcher = deathsTabPattern.matcher(name)
                                if (matcher.find()) {
                                    deaths = matcher.group("deaths").toInt()
                                }
                            }
                            name.contains("✦") -> {
                                val matcher = missingPuzzlePattern.matcher(name)
                                if (matcher.find()) {
                                    missingPuzzles++
                                }
                            }
                            name.contains("✖") -> {
                                val matcher = failedPuzzlePattern.matcher(name)
                                if (matcher.find()) {
                                    failedPuzzles++
                                }
                            }
                            name.contains("Secrets Found:") -> {
                                val matcher = secretsFoundPattern.matcher(name)
                                if (matcher.find()) {
                                    foundSecrets = matcher.group("secrets").toInt()
                                }
                            }
                            name.contains("Crypts:") -> {
                                val matcher = cryptsPattern.matcher(name)
                                if (matcher.find()) {
                                    crypts = matcher.group("crypts").toInt()
                                }
                            }
                        }
                    } catch (ignored: NumberFormatException) {
                    }
                }
            }
            if (Skytils.usingDungeonRooms && (Skytils.config.showScoreCalculation || Skytils.config.scoreCalculationAssist)) {
                when (Skytils.config.scoreCalculationMethod) {
                    0 -> {
                        if (ticks % 30 == 0) {
                            ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/room json")
                            ticks = 0
                        }
                    }
                    1 -> {
                        if (drmRoomScanMethod != null && (lastRoomScanPos == null || ticks % 900 == 0 || mc.thePlayer.getDistanceSqToCenter(
                                lastRoomScanPos
                            ) >= (mc.gameSettings.renderDistanceChunks.coerceAtMost(
                                10
                            ) * 16.0).pow(2)
                                    )
                        ) {
                            lastRoomScanPos = mc.thePlayer.position
                            Multithreading.runAsync {
                                @Suppress("UNCHECKED_CAST")
                                roomScanCallback(
                                    drmRoomScanMethod!!.invokeExact() as List<String>
                                )
                            }
                            ticks = 0
                        }
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
                            val name = matcher.group("name")
                            val secrets = matcher.group("secrets").toIntOrNull() ?: return
                            if (!rooms.containsKey(name)) {
                                rooms[name] = secrets
                            }
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
    fun onSendChat(event: SendChatMessageEvent) {
        if (DevTools.getToggle("scorecalc") && event.message == "/debugscorecalcrooms") {
            mc.thePlayer.addChatMessage(ChatComponentText(rooms.toString()))
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        mimicKilled = false
        lastRoomScanPos = null
        rooms.clear()
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
                        val matcher = dungeonClearedPattern.matcher(line)
                        if (matcher.find()) {
                            clearedPercentage = matcher.group("percentage").toInt()
                            continue
                        }
                    }
                    if (line.startsWith("Time Elapsed:")) {
                        val matcher = timeElapsedPattern.matcher(line)
                        if (matcher.find()) {
                            val hours: Int = runCatching { matcher.group("hrs").toInt() }.getOrDefault(0)
                            val minutes: Int = runCatching { matcher.group("min").toInt() }.getOrDefault(0)
                            val seconds: Int = runCatching { matcher.group("sec").toInt() }.getOrDefault(0)
                            secondsElapsed = (hours * 3600 + minutes * 60 + seconds).toDouble()
                            continue
                        }
                    }
                }
                val skillScore = 100 - 2 * deaths - 14 * (missingPuzzles + failedPuzzles)
                val discoveryScore: Double = floor(
                    (60 * (clearedPercentage / 100f)).toDouble().coerceIn(0.0, 60.0)
                ) + if (totalSecrets <= 0) 0.0 else floor(
                    (40f * foundSecrets / totalSecrets).toDouble().coerceIn(0.0, 40.0)
                )
                val speedScore: Double
                val bonusScore = (if (mimicKilled) 2 else 0) + crypts.coerceAtMost(5) + if (isPaul) 10 else 0
                val countedSeconds =
                    if (DungeonFeatures.dungeonFloor == "F2") 0.0.coerceAtLeast(secondsElapsed - 120) else secondsElapsed
                speedScore = if (countedSeconds <= 1320) {
                    100.0
                } else if (1320 < countedSeconds && countedSeconds <= 1420) {
                    232 - 0.1 * countedSeconds
                } else if (1420 < countedSeconds && countedSeconds <= 1820) {
                    161 - 0.05 * countedSeconds
                } else if (1820 < countedSeconds && countedSeconds <= 3920) {
                    392 / 3f - 1 / 30f * countedSeconds
                } else 0.0
                text.add("§6Deaths:§a $deaths")
                text.add("§6Missing Puzzles:§a $missingPuzzles")
                text.add("§6Failed Puzzles:§a $failedPuzzles")
                text.add("§6Secrets Found:§a $foundSecrets")
                if (totalSecrets != 0) {
                    if (Skytils.config.scoreCalculationMethod == 0) text.add("§6Estimated Secret Count:§a $totalSecrets")
                    else {
                        if (clearedPercentage >= 80) {
                            text.add("§6Estimated Secret Count:§a $totalSecrets")
                        } else {
                            text.add("§6Estimated Secret Count:§c HIDDEN")
                        }
                    }
                }
                text.add("§6Crypts:§a $crypts")
                if (Utils.equalsOneOf(DungeonFeatures.dungeonFloor, "F6", "F7", "M6", "M7")) {
                    text.add("§6Mimic Killed:" + if (mimicKilled) "§a ✓" else " §c X")
                }
                if (isPaul) {
                    text.add("§6EZPZ: §a+10")
                }
                text.add("§6Skill Score:§a $skillScore")
                if (totalSecrets != 0) text.add("§6Estimated Discovery Score:§a " + discoveryScore.toInt())
                if (speedScore != 100.0) text.add("§6Speed Score:§a " + speedScore.toInt())
                text.add("§6Estimated Bonus Score:§a $bonusScore")
                if (totalSecrets != 0) text.add("§6Estimated Total Score:§a " + (skillScore + discoveryScore + speedScore + bonusScore).toInt())
                if (!Skytils.usingDungeonRooms) text.add("§cDownload the Dungeon Rooms Mod for discovery estimate.")
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
            private val dungeonClearedPattern = Pattern.compile("Dungeon Cleared: (?<percentage>\\d+)%")
            private val timeElapsedPattern =
                Pattern.compile("Time Elapsed: (?:(?<hrs>\\d+)h )?(?:(?<min>\\d+)m )?(?:(?<sec>\\d+)s)?")
        }

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}