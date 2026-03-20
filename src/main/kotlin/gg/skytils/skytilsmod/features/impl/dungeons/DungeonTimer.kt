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

import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.ChatMessageReceivedEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures.dungeonFloorNumber
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.RoomState
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.RoomType
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonInfo
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.formattedText
import gg.skytils.skytilsmod.utils.stripControlCodes

object DungeonTimer : EventSubscriber {
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

    override fun setup() {
        register(::onChat, EventPriority.Highest)
        register(::onWorldChange)
    }

    fun onChat(event: ChatMessageReceivedEvent) {
        if (!Utils.inDungeons) return
        val message = event.message.formattedText
        val unformatted = message.stripControlCodes()
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
            }

            bloodOpenTime != -1L && bloodClearTime == -1L && message == "§r§c[BOSS] The Watcher§r§f: That will be enough for now.§r" -> {
                DungeonInfo.uniqueRooms["Blood"]?.let {
                    assert(it.mainRoom.data.type == RoomType.BLOOD)
                    if (it.mainRoom.state > RoomState.CLEARED) {
                        it.mainRoom.state = RoomState.CLEARED
                    }
                }
            }

            message == "§r§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass.§r" -> {
                bloodClearTime = System.currentTimeMillis()
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
                }
            }

            bossEntryTime != -1L && bossClearTime == -1L && message.contains("§r§c☠ §r§eDefeated §r") -> {
                bossClearTime = System.currentTimeMillis()
            }

            dungeonFloorNumber == 7 && (message.startsWith("§r§4[BOSS] ") || message.startsWith("§r§aThe Core entrance ")) -> {
                when {
                    message.endsWith("§r§cPathetic Maxor, just like expected.§r") && phase1ClearTime == -1L -> {
                        phase1ClearTime = System.currentTimeMillis()
                    }

                    message.endsWith("§r§cWho dares trespass into my domain?§r") && phase2ClearTime == -1L -> {
                        phase2ClearTime = System.currentTimeMillis()
                    }

                    message.endsWith(" is opening!§r") && terminalClearTime == -1L -> {
                        terminalClearTime = System.currentTimeMillis()
                    }

                    message.endsWith("§r§cYou went further than any human before, congratulations.§r") && phase3ClearTime == -1L -> {
                        phase3ClearTime = System.currentTimeMillis()
                    }

                    message.endsWith("§r§cAll this, for nothing...§r") -> {
                        phase4ClearTime = System.currentTimeMillis()
                    }
                }
            }

            dungeonFloorNumber == 6 && message.startsWith("§r§c[BOSS] Sadan") -> {
                when {
                    (message.endsWith("§r§f: ENOUGH!§r") && terraClearTime == -1L) -> {
                        terraClearTime = System.currentTimeMillis()
                    }

                    (message.endsWith("§r§f: You did it. I understand now, you have earned my respect.§r") && giantsClearTime == -1L) -> {
                        giantsClearTime = System.currentTimeMillis()
                    }
                }
            }
        }
    }

    fun onWorldChange(event: WorldUnloadEvent) {
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
}