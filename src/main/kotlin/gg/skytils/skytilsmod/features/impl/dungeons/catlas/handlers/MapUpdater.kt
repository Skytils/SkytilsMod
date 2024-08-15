/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.*
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.MapUtils
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.MapUtils.mapX
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.MapUtils.mapZ
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.MapUtils.yaw
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.world.storage.MapData
import kotlin.math.roundToInt

object MapUpdater {
    fun updatePlayers(mapData: MapData) {
        if (DungeonListener.team.isEmpty()) return

        val decor = mapData.mapDecorations
        DungeonListener.team.forEach { (name, team) ->
            val player = team.mapPlayer
            decor.entries.find { (icon, _) -> icon == player.icon }?.let { (_, vec4b) ->
                player.isOurMarker = vec4b.func_176110_a().toInt() == 1
                player.mapX = vec4b.mapX
                player.mapZ = vec4b.mapZ
                player.yaw = vec4b.yaw
            }
            if (player.isOurMarker || name == mc.thePlayer.name) {
                player.yaw = mc.thePlayer.rotationYaw
                player.mapX =
                    ((mc.thePlayer.posX - DungeonScanner.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).roundToInt()
                player.mapZ =
                    ((mc.thePlayer.posZ - DungeonScanner.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).roundToInt()
            }
        }
    }

    fun updateRooms(mapData: MapData) {
        DungeonMapColorParser.updateMap(mapData)

        for (x in 0..10) {
            for (z in 0..10) {
                val room = DungeonInfo.dungeonList[z * 11 + x]
                val mapTile = DungeonMapColorParser.getTile(x, z)

                if (room is Unknown) {
                    DungeonInfo.dungeonList[z * 11 + x] = mapTile
                    if (mapTile is Room) {
                        val connected = DungeonMapColorParser.getConnected(x, z)
                        connected.firstOrNull { it.data.name != "Unknown" }?.let {
                            mapTile.addToUnique(z, x, it.data.name)
                        }
                    }
                    continue
                }

                if (mapTile.state.ordinal < room.state.ordinal) {
                    room.state = mapTile.state
                    if (room is Room && room.state == RoomState.GREEN) {
                        room.uniqueRoom?.foundSecrets = room.uniqueRoom?.foundSecrets?.coerceAtLeast(room.data.secrets)
                    }
                }

                if (mapTile is Door && room is Door) {
                    if (mapTile.type == DoorType.WITHER && room.type != DoorType.WITHER) {
                        room.type = mapTile.type
                    }
                }

                if (room is Door && Utils.equalsOneOf(room.type, DoorType.ENTRANCE, DoorType.WITHER, DoorType.BLOOD)) {
                    if (mapTile is Door && mapTile.type == DoorType.WITHER) {
                        room.opened = false
                    } else if (!room.opened) {
                        val chunk = mc.theWorld.getChunkFromChunkCoords(
                            room.x shr 4,
                            room.z shr 4
                        )
                        if (chunk.isLoaded) {
                            if (chunk.getBlockState(BlockPos(room.x, 69, room.z)).block == Blocks.air)
                            room.opened = true
                        } else if (mapTile is Door && mapTile.state == RoomState.DISCOVERED) {
                            if (room.type == DoorType.BLOOD) {
                                val bloodRoom = DungeonInfo.uniqueRooms.find { r ->
                                    r.mainRoom.data.type == RoomType.BLOOD
                                }

                                if (bloodRoom != null && bloodRoom.mainRoom.state != RoomState.UNOPENED) {
                                    room.opened = true
                                }
                            } else {
                                room.opened = true
                            }
                        }
                    }
                }
            }
        }
    }
}
