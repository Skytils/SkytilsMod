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

import gg.essential.universal.UChat
import gg.essential.universal.UResolution
import net.minecraft.client.Minecraft
import net.minecraft.world.World
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.Utils.timeFormat
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import skytils.skytilsmod.utils.stripControlCodes
import kotlin.math.roundToInt

class DungeonTimer {
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inDungeons || event.type.toInt() == 2) return
        val message = event.message.formattedText
        val unformatted = event.message.unformattedText.stripControlCodes()
        when {
            scoreShownAt == -1L && message.contains("§r§fTeam Score: §r") -> {
                scoreShownAt = System.currentTimeMillis()
            }
            (message == "§r§aDungeon starts in 1 second.§r" || message == "§r§aDungeon starts in 1 second. Get ready!§r") && dungeonStartTime == -1L -> {
                dungeonStartTime = System.currentTimeMillis() + 1000
            }
            message.endsWith(" §r§ehas obtained §r§a§r§6§r§8Wither Key§r§e!§r") || unformatted == "A Wither Key was picked up!" || message.endsWith(
                "§r§ehas obtained §r§8Wither Key§r§e!§r"
            ) -> {
                witherDoors++
            }
            bloodOpenTime == -1L && (unformatted == "The BLOOD DOOR has been opened!" || message.startsWith(
                "§r§c[BOSS] The Watcher§r§f"
            )) -> {
                bloodOpenTime = System.currentTimeMillis()
                if (Skytils.config.dungeonTimer) UChat.chat(
                    "§bBlood took " + ((bloodOpenTime - dungeonStartTime) / 1000.0).roundToInt() + " seconds to open."
                )
            }
            message == "§r§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass.§r" -> {
                bloodClearTime = System.currentTimeMillis()
                if (Skytils.config.dungeonTimer) UChat.chat(
                    "§bWatcher took " + ((bloodClearTime - bloodOpenTime) / 1000.0).roundToInt() + " seconds to clear."
                )
            }
            bloodClearTime != -1L && bossEntryTime == -1L && unformatted.startsWith("[BOSS] ") && unformatted.contains(":") -> {
                val bossName = unformatted.substringAfter("[BOSS] ").substringBefore(":").trim()
                if (bossName != "The Watcher" && DungeonFeatures.dungeonFloor != null && Utils.checkBossName(
                        DungeonFeatures.dungeonFloor!!,
                        bossName
                    )
                ) {
                    bossEntryTime = System.currentTimeMillis()
                }
            }
            bossEntryTime != -1L && bossClearTime == -1L && message.contains("§r§c☠ §r§eDefeated §r") -> {
                bossClearTime = System.currentTimeMillis()
                if (Skytils.config.dungeonTimer) {
                    TickTask(5) {
                        UChat.chat(
                            """§7Wither Doors: $witherDoors
§cBlood took ${((bloodOpenTime - dungeonStartTime) / 1000.0).roundToInt()} seconds to open.
§bWatcher took ${((bloodClearTime - bloodOpenTime) / 1000.0).roundToInt()} seconds to clear.
§9Boss entry was ${timeFormat((bossEntryTime - dungeonStartTime) / 1000.0)}."""
                        )
                    }
                }
            }
            DungeonFeatures.dungeonFloor == "F7" && message.startsWith("§r§4[BOSS] Necron") -> {
                when {
                    message.endsWith("§r§cFINE! LET'S MOVE TO SOMEWHERE ELSE!!§r") && phase1ClearTime == -1L -> {
                        phase1ClearTime = System.currentTimeMillis()
                        if (Skytils.config.necronPhaseTimer) UChat.chat(
                            "§bPhase 1 took " + ((phase1ClearTime - bossEntryTime) / 1000.0).roundToInt() + " seconds."
                        )
                    }
                    message.endsWith("§r§cCRAP!! IT BROKE THE FLOOR!§r") && phase2ClearTime == -1L -> {
                        phase2ClearTime = System.currentTimeMillis()
                        if (Skytils.config.necronPhaseTimer) UChat.chat(
                            "§bPhase 2 took " + ((phase2ClearTime - phase1ClearTime) / 1000.0).roundToInt() + " seconds."
                        )
                    }
                    (message.endsWith("§r§cTHAT'S IT YOU HAVE DONE IT!§r") && phase3ClearTime == -1L) -> {
                        phase3ClearTime = System.currentTimeMillis()
                        if (Skytils.config.necronPhaseTimer) UChat.chat(
                            "§bPhase 3 took " + ((phase3ClearTime - phase2ClearTime) / 1000.0).roundToInt() + " seconds."
                        )
                    }
                }
            }
            (DungeonFeatures.dungeonFloor == "F6" || DungeonFeatures.dungeonFloor == "M6") && message.startsWith("§r§c[BOSS] Sadan") -> {
                when {
                    (message.endsWith("§r§f: ENOUGH!§r") && terraClearTime == -1L) -> {
                        terraClearTime = System.currentTimeMillis()
                        if (Skytils.config.sadanPhaseTimer) UChat.chat(
                            "§bTerracotta took " + ((terraClearTime - bossEntryTime) / 1000.0).roundToInt() + " seconds."
                        )
                    }
                    (message.endsWith("§r§f: You did it. I understand now, you have earned my respect.§r") && giantsClearTime == -1L) -> {
                        giantsClearTime = System.currentTimeMillis()
                        if (Skytils.config.sadanPhaseTimer) UChat.chat(
                            "§bGiants took " + ((giantsClearTime - terraClearTime) / 1000.0).roundToInt() + " seconds."
                        )
                    }

                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        dungeonStartTime = -1
        bloodOpenTime = -1
        bloodClearTime = -1
        bossEntryTime = -1
        bossClearTime = -1
        phase1ClearTime = -1
        phase2ClearTime = -1
        phase3ClearTime = -1
        terraClearTime = -1
        giantsClearTime = -1
        sadanTime = -1
        witherDoors = 0
        scoreShownAt = -1
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        var dungeonStartTime = -1L
        var bloodOpenTime = -1L
        var bloodClearTime = -1L
        var bossEntryTime = -1L
        var bossClearTime = -1L
        var phase1ClearTime = -1L
        var phase2ClearTime = -1L
        var phase3ClearTime = -1L
        var terraClearTime = -1L
        var giantsClearTime = -1L
        var sadanTime = -1L
        var witherDoors = 0
        var scoreShownAt = -1L

        init {
            DungeonTimerElement()
            NecronPhaseTimerElement()
            SadanPhaseTimerElement()
        }
    }

    class DungeonTimerElement : GuiElement("Dungeon Timer", FloatPair(200, 80)) {
        override fun render() {
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (toggled && Utils.inDungeons && player != null && world != null) {
                val sr = UResolution
                val leftAlign = actualX < sr.scaledWidth / 2f
                val time =
                    (((if (bossClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else bossClearTime) - dungeonStartTime) / 1000.0)
                val lines = arrayListOf(
                    "§aReal Time: ${if (dungeonStartTime == -1L) "0s" else "${time}s"}",
                    "§aTime Elapsed: ${if (dungeonStartTime == -1L) "0s" else timeFormat(time)}",
                    "§7Wither Doors: $witherDoors",
                    "§4Blood Open: ${if (bloodOpenTime == -1L) "${time}s" else timeFormat((bloodOpenTime - dungeonStartTime) / 1000.0)}",
                ).apply {
                    if (bloodOpenTime != -1L)
                        add("§cWatcher Clear: ${timeFormat(((if (bloodClearTime == -1L) System.currentTimeMillis() else bloodClearTime) - bloodOpenTime) / 1000.0)}")
                    add("§9Boss Entry: ${if (bossEntryTime == -1L) "${time}s" else timeFormat((bossEntryTime - dungeonStartTime) / 1000.0)}")
                    if (bossEntryTime != -1L)
                        add("§bBoss Clear: ${timeFormat(((if (bossClearTime == -1L) System.currentTimeMillis() else bossClearTime) - bossEntryTime) / 1000.0)}")
                }
                for (i in lines.indices) {
                    val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                    ScreenRenderer.fontRenderer.drawString(
                        lines[i],
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
            val displayText = """
                §aReal Time: 0s
                §aTime Elapsed: 0s
                §7Wither Doors: 0
                §4Blood Open: 0s
                §cWatcher Clear: 0s
                §9Boss Entry: 0s
                §bBoss Clear: 0s
                """.trimIndent()
            val lines = displayText.split("\n".toRegex()).toTypedArray()
            for (i in lines.indices) {
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    lines[i],
                    if (leftAlign) 0f else actualWidth,
                    (i * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * 7
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§cWatcher Clear: 0s")

        override val toggled: Boolean
            get() = Skytils.config.dungeonTimer

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class NecronPhaseTimerElement : GuiElement("Necron Phase Timer", FloatPair(200, 120)) {
        override fun render() {
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (toggled && Utils.inDungeons && player != null && world != null && bossEntryTime != -1L && DungeonFeatures.dungeonFloor == "F7") {
                val sr = UResolution
                val leftAlign = actualX < sr.scaledWidth / 2f
                val lines = arrayListOf(
                    "§bPhase 1: ${timeFormat(((if (phase1ClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else phase1ClearTime) - bossEntryTime) / 1000.0)}"
                ).apply {
                    if (phase1ClearTime != -1L)
                        add("§cPhase 2: ${timeFormat(((if (phase2ClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else phase2ClearTime) - phase1ClearTime) / 1000.0)}")
                    if (phase2ClearTime != -1L)
                        add("§6Phase 3: ${timeFormat(((if (phase3ClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else phase3ClearTime) - phase2ClearTime) / 1000.0)}")
                    if (phase3ClearTime != -1L)
                        add("§4Phase 4: ${timeFormat(((if (bossClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else bossClearTime) - phase3ClearTime) / 1000.0)}")
                }
                for (i in lines.indices) {
                    val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                    ScreenRenderer.fontRenderer.drawString(
                        lines[i],
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
            val displayText = """
                §bPhase 1: 0s
                §cPhase 2: 0s
                §6Phase 3: 0s
                §4Phase 4: 0s
                """.trimIndent()
            val lines = displayText.split("\n".toRegex()).toTypedArray()
            for (i in lines.indices) {
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    lines[i],
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
            get() = ScreenRenderer.fontRenderer.getStringWidth("§cPhase 1: 0s")

        override val toggled: Boolean
            get() = Skytils.config.necronPhaseTimer

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class SadanPhaseTimerElement : GuiElement("Sadan Phase Timer", FloatPair(200, 120)) {
        override fun render() {
            val player = mc.thePlayer
            val world: World? = mc.theWorld
            if (toggled && Utils.inDungeons && player != null && world != null && bossEntryTime != -1L && Utils.equalsOneOf(
                    DungeonFeatures.dungeonFloor,
                    "F6",
                    "M6"
                )
            ) {
                val sr = UResolution
                val leftAlign = actualX < sr.scaledWidth / 2f
                val lines = arrayListOf(
                    "§dTerracotta: ${timeFormat(((if (terraClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else terraClearTime) - bossEntryTime) / 1000.0)}"
                ).apply {
                    if (terraClearTime != -1L)
                        add("§aGiants: ${timeFormat(((if (giantsClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else giantsClearTime) - terraClearTime) / 1000.0)}")
                    if (giantsClearTime != -1L)
                        add("§cSadan: ${timeFormat(((if (bossClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else bossClearTime) - giantsClearTime) / 1000.0)}")
                }
                for (i in lines.indices) {
                    val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                    ScreenRenderer.fontRenderer.drawString(
                        lines[i],
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
            val displayText = """
                §dTerracotta: 0s
                §aGiants: 0s
                §cSadan: 0s
                """.trimIndent()
            val lines = displayText.split("\n".toRegex()).toTypedArray()
            for (i in lines.indices) {
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    lines[i],
                    if (leftAlign) 0f else width.toFloat(),
                    (i * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * 3

        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§dTerracotta: 0s")

        override val toggled: Boolean
            get() = Skytils.config.sadanPhaseTimer

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}