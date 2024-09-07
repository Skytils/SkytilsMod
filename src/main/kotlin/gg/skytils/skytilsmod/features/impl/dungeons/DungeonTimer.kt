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

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures.dungeonFloorNumber
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.Room
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.RoomState
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.RoomType
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonInfo
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.NumberUtil
import gg.skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor
import kotlin.math.roundToInt

object DungeonTimer {
    var dungeonStartTime = -1L
    var bloodOpenTime = -1L
    var bloodClearTime = -1L
    var bossEntryTime = -1L
    var bossClearTime = -1L
    var phase1ClearTime = -1L
    var phase2ClearTime = -1L
    var phase3ClearTime = -1L
    var terminalClearTime = -1L
    var phase4ClearTime = -1L
    var terraClearTime = -1L
    var giantsClearTime = -1L
    var witherDoors = 0
    var scoreShownAt = -1L

    init {
        DungeonTimerElement()
        NecronPhaseTimerElement()
        SadanPhaseTimerElement()
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inDungeons || event.type == 2.toByte()) return
        val message = event.message.formattedText
        val unformatted = event.message.unformattedText.stripControlCodes()
        when {
            scoreShownAt == -1L && message.contains("§r§fTeam Score: §r") -> {
                scoreShownAt = System.currentTimeMillis()
            }

/*            (message == "§r§aStarting in 1 second.§r") && dungeonStartTime == -1L -> {
                dungeonStartTime = System.currentTimeMillis() + 1000
            }*/

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
                    "§4Blood §btook ${diff(bloodOpenTime, dungeonStartTime)} seconds to open."
                )
            }

            bloodOpenTime != -1L && bloodClearTime == -1L && message == "§r§c[BOSS] The Watcher§r§f: That will be enough for now.§r" -> {
                DungeonInfo.uniqueRooms.find { it.mainRoom.data.type == RoomType.BLOOD }?.let {
                    if (it.mainRoom.state > RoomState.CLEARED) {
                        it.mainRoom.state = RoomState.CLEARED
                    }
                }
            }

            message == "§r§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass.§r" -> {
                bloodClearTime = System.currentTimeMillis()
                if (Skytils.config.dungeonTimer) UChat.chat(
                    "§cWatcher §btook ${diff(bloodClearTime, bloodOpenTime)} seconds to clear."
                )
            }

            bossEntryTime == -1L && unformatted.startsWith("[BOSS] ") && unformatted.contains(":") -> {
                val bossName = unformatted.substringAfter("[BOSS] ").substringBefore(":").trim()
                if (bossName != "The Watcher" && DungeonFeatures.dungeonFloor != null && Utils.checkBossName(
                        DungeonFeatures.dungeonFloor!!,
                        bossName
                    )
                ) {
                    bossEntryTime = System.currentTimeMillis()
                    DungeonListener.markAllRevived()
                    if (Skytils.config.dungeonTimer && bloodClearTime != -1L) UChat.chat(
                        "§dPortal §btook ${diff(bossEntryTime, bloodClearTime)} seconds to enter."
                    )
                }
            }

            bossEntryTime != -1L && bossClearTime == -1L && message.contains("§r§c☠ §r§eDefeated §r") -> {
                bossClearTime = System.currentTimeMillis()
                tickTimer(5) {
                    arrayListOf<String>().apply {
                        if (Skytils.config.dungeonTimer) {
                            add("§7Wither Doors: $witherDoors")
                            add("§4Blood §btook ${diff(bloodOpenTime, dungeonStartTime)} seconds to open.")
                            if (bloodClearTime == -1L) {
                                add("§c§lGG! §cWatcher §bWAS SKIPPED!")
                                add("§d§lGG! §dPortal §bWAS SKIPPED!")
                            } else {
                                add("§cWatcher §btook ${diff(bloodClearTime, bloodOpenTime)} seconds to clear.")
                                add("§dPortal §btook ${diff(bossEntryTime, bloodClearTime)} seconds to enter.")
                            }
                            add("§9Boss entry §bwas ${dungeonTimeFormat((bossEntryTime - dungeonStartTime) / 1000.0)}.")
                        }
                        if (Skytils.config.sadanPhaseTimer && dungeonFloorNumber == 6) {
                            add("§dTerracotta §btook ${diff(terraClearTime, bossEntryTime)} seconds.")
                            add("§aGiants §btook ${diff(giantsClearTime, terraClearTime)} seconds.")
                            add("§cSadan §btook ${diff(bossClearTime, giantsClearTime)} seconds.")
                        } else if (Skytils.config.necronPhaseTimer && dungeonFloorNumber == 7) {
                            add("§bMaxor took ${diff(phase1ClearTime, bossEntryTime)} seconds.")
                            add("§cStorm §btook ${diff(phase2ClearTime, phase1ClearTime)} seconds.")
                            add("§eTerminals §btook ${diff(terminalClearTime, phase2ClearTime)} seconds.")
                            add("§6Goldor §btook ${diff(phase3ClearTime, terminalClearTime)} seconds.")
                            add("§4Necron §btook ${diff(phase4ClearTime, phase3ClearTime)} seconds.")
                            if (DungeonFeatures.dungeonFloor == "M7") {
                                add("§7Wither King §btook ${diff(bossClearTime, phase4ClearTime)} seconds.")
                            }
                        }
                        if (Skytils.config.dungeonTimer) {
                            add("§bDungeon finished in ${diff(bossClearTime, dungeonStartTime)} seconds.")
                        }
                        if (isNotEmpty()) UChat.chat(joinToString("\n"))
                    }
                }
            }

            dungeonFloorNumber == 7 && (message.startsWith("§r§4[BOSS] ") || message.startsWith("§r§aThe Core entrance ")) -> {
                when {
                    message.endsWith("§r§cPathetic Maxor, just like expected.§r") && phase1ClearTime == -1L -> {
                        phase1ClearTime = System.currentTimeMillis()
                        if (Skytils.config.necronPhaseTimer) UChat.chat(
                            "§bMaxor took ${diff(phase1ClearTime, bossEntryTime)} seconds."
                        )
                    }

                    message.endsWith("§r§cWho dares trespass into my domain?§r") && phase2ClearTime == -1L -> {
                        phase2ClearTime = System.currentTimeMillis()
                        if (Skytils.config.necronPhaseTimer) UChat.chat(
                            "§cStorm §btook ${diff(phase2ClearTime, phase1ClearTime)} seconds."
                        )
                    }

                    message.endsWith(" is opening!§r") && terminalClearTime == -1L -> {
                        terminalClearTime = System.currentTimeMillis()
                        if (Skytils.config.necronPhaseTimer) UChat.chat(
                            "§eTerminals §btook ${diff(terminalClearTime, phase2ClearTime)} seconds."
                        )
                    }

                    message.endsWith("§r§cYou went further than any human before, congratulations.§r") && phase3ClearTime == -1L -> {
                        phase3ClearTime = System.currentTimeMillis()
                        if (Skytils.config.necronPhaseTimer) UChat.chat(
                            "§6Goldor §btook ${diff(phase3ClearTime, terminalClearTime)} seconds."
                        )
                    }

                    message.endsWith("§r§cAll this, for nothing...§r") -> {
                        phase4ClearTime = System.currentTimeMillis()
                        if (Skytils.config.necronPhaseTimer) UChat.chat(
                            "§4Necron §btook ${diff(phase4ClearTime, phase3ClearTime)} seconds."
                        )
                    }
                }
            }

            dungeonFloorNumber == 6 && message.startsWith("§r§c[BOSS] Sadan") -> {
                when {
                    (message.endsWith("§r§f: ENOUGH!§r") && terraClearTime == -1L) -> {
                        terraClearTime = System.currentTimeMillis()
                        if (Skytils.config.sadanPhaseTimer) UChat.chat(
                            "§dTerracotta §btook ${diff(terraClearTime, bossEntryTime)} seconds."
                        )
                    }

                    (message.endsWith("§r§f: You did it. I understand now, you have earned my respect.§r") && giantsClearTime == -1L) -> {
                        giantsClearTime = System.currentTimeMillis()
                        if (Skytils.config.sadanPhaseTimer) UChat.chat(
                            "§aGiants §btook ${diff(giantsClearTime, terraClearTime)} seconds."
                        )
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        dungeonStartTime = -1
        bloodOpenTime = -1
        bloodClearTime = -1
        bossEntryTime = -1
        bossClearTime = -1
        phase1ClearTime = -1
        phase2ClearTime = -1
        terminalClearTime = -1
        phase3ClearTime = -1
        phase4ClearTime = -1
        terraClearTime = -1
        giantsClearTime = -1
        witherDoors = 0
        scoreShownAt = -1
    }

    class DungeonTimerElement : GuiElement("Dungeon Timer", x = 200, y = 80) {
        override fun render() {
            if (toggled && Utils.inDungeons && dungeonStartTime != -1L) {
                val time =
                    (((if (bossClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else bossClearTime) - dungeonStartTime) / 1000.0)
                val lines = arrayListOf(
                    "§aTime Elapsed: ${if (dungeonStartTime == -1L) "0s" else dungeonTimeFormat(time)}",
                    "§7Wither Doors: $witherDoors",
                    "§4Blood Open: ${
                        if (bloodOpenTime == -1L) dungeonTimeFormat(time) else dungeonTimeFormat(
                            (bloodOpenTime - dungeonStartTime) / 1000.0
                        )
                    }",
                ).apply {
                    if (bloodOpenTime != -1L)
                        add(
                            "§cWatcher Clear: ${
                                diff(
                                    if (bloodClearTime == -1L) System.currentTimeMillis() else bloodClearTime,
                                    bloodOpenTime
                                )
                            }s"
                        )
                    if (bloodClearTime != -1L)
                        if (!DungeonFeatures.dungeonFloor.equals("E"))
                            add("§dPortal: ${dungeonTimeFormat(((if (bossEntryTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else bossEntryTime) - bloodClearTime) / 1000.0)}")
                    add("§9Boss Entry: ${if (bossEntryTime == -1L) dungeonTimeFormat(time) else dungeonTimeFormat((bossEntryTime - dungeonStartTime) / 1000.0)}")
                    if (bossEntryTime != -1L)
                        add("§bBoss Clear: ${dungeonTimeFormat(((if (bossClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else bossClearTime) - bossEntryTime) / 1000.0)}")
                }
                RenderUtil.drawAllInList(this, lines)
            }
        }

        override fun demoRender() {
            val displayText = """
                §aTime Elapsed: 0s
                §7Wither Doors: 0
                §4Blood Open: 0s
                §cWatcher Clear: 0s
                §dPortal: 0s
                §9Boss Entry: 0s
                §bBoss Clear: 0s
                """.trimIndent()
            val lines = displayText.split('\n')
            RenderUtil.drawAllInList(this, lines)
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

    class NecronPhaseTimerElement : GuiElement("Necron Phase Timer", x = 200, y = 120) {
        override fun render() {
            if (toggled && Utils.inDungeons && bossEntryTime != -1L && dungeonFloorNumber == 7) {
                val lines = arrayListOf(
                    "§bMaxor: ${dungeonTimeFormat(((if (phase1ClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else phase1ClearTime) - bossEntryTime) / 1000.0)}"
                ).apply {
                    if (phase1ClearTime != -1L)
                        add("§cStorm: ${dungeonTimeFormat(((if (phase2ClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else phase2ClearTime) - phase1ClearTime) / 1000.0)}")
                    if (phase2ClearTime != -1L)
                        add("§eTerminals: ${dungeonTimeFormat(((if (terminalClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else terminalClearTime) - phase2ClearTime) / 1000.0)}")
                    if (terminalClearTime != -1L)
                        add("§6Goldor: ${dungeonTimeFormat(((if (phase3ClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else phase3ClearTime) - terminalClearTime) / 1000.0)}")
                    if (phase3ClearTime != -1L)
                        add("§4Necron: ${dungeonTimeFormat(((if (phase4ClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else phase4ClearTime) - phase3ClearTime) / 1000.0)}")
                    if (phase4ClearTime != -1L && DungeonFeatures.dungeonFloor == "M7")
                        add("§7Wither King: ${dungeonTimeFormat(((if (bossClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else bossClearTime) - phase4ClearTime) / 1000.0)}")
                }
                RenderUtil.drawAllInList(this, lines)
            }
        }

        override fun demoRender() {
            val displayText = """
                §bMaxor: 0s
                §cStorm: 0s
                §eTerminals: 0s
                §6Goldor: 0s
                §4Necron: 0s
                §7Wither King: 0s
                """.trimIndent()
            val lines = displayText.split('\n')
            RenderUtil.drawAllInList(this, lines)
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * 6
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§7Wither King: 0s")

        override val toggled: Boolean
            get() = Skytils.config.necronPhaseTimer

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    class SadanPhaseTimerElement : GuiElement("Sadan Phase Timer", x = 200, y = 120) {
        override fun render() {
            if (toggled && Utils.inDungeons && bossEntryTime != -1L && dungeonFloorNumber == 6) {
                val lines = arrayListOf(
                    "§dTerracotta: ${dungeonTimeFormat(((if (terraClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else terraClearTime) - bossEntryTime) / 1000.0)}"
                ).apply {
                    if (terraClearTime != -1L)
                        add("§aGiants: ${dungeonTimeFormat(((if (giantsClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else giantsClearTime) - terraClearTime) / 1000.0)}")
                    if (giantsClearTime != -1L)
                        add("§cSadan: ${dungeonTimeFormat(((if (bossClearTime == -1L) if (scoreShownAt == -1L) System.currentTimeMillis() else scoreShownAt else bossClearTime) - giantsClearTime) / 1000.0)}")
                }
                RenderUtil.drawAllInList(this, lines)
            }
        }

        override fun demoRender() {
            val displayText = """
                §dTerracotta: 0s
                §aGiants: 0s
                §cSadan: 0s
                """.trimIndent()
            val lines = displayText.split('\n')
            RenderUtil.drawAllInList(this, lines)
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

private fun dungeonTimeFormat(seconds: Double): String {
    return if (seconds >= 60) {
        "${floor(seconds / 60).toInt()}m ${
            (seconds % 60).run {
                if (!Skytils.config.showMillisOnDungeonTimer) roundToInt() else NumberUtil.nf.format(
                    this.roundToPrecision(
                        2
                    )
                )
            }
        }s"
    } else {
        "${
            seconds.run {
                if (!Skytils.config.showMillisOnDungeonTimer) roundToInt() else NumberUtil.nf.format(
                    this.roundToPrecision(
                        2
                    )
                )
            }
        }s"
    }
}

private fun diff(end: Long, start: Long): Any {
    val sec = ((end - start) / 1000.0)
    return if (!Skytils.config.showMillisOnDungeonTimer) sec.roundToInt() else NumberUtil.nf.format(
        sec.roundToPrecision(
            2
        )
    )
}
