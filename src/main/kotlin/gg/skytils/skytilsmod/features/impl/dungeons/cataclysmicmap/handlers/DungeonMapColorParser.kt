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

package gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.handlers

import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.map.*
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.utils.MapUtils
import net.minecraft.util.EnumFacing
import net.minecraft.world.storage.MapData

object DungeonMapColorParser {
    private var centerColors: ByteArray = ByteArray(121)
    private var sideColors: ByteArray = ByteArray(121)
    private var cachedTiles: Array<Tile?> = Array(121) { null }

    var halfRoom = -1
    var halfTile = -1
    // connectorSize
    var quarterRoom = -1
    var startX = -1
    var startY = -1

    fun calibrate() {
        halfRoom = MapUtils.mapRoomSize / 2
        halfTile = halfRoom + 2
        quarterRoom = halfRoom / 2
        startX = MapUtils.startCorner.first + halfRoom
        startY = MapUtils.startCorner.second + halfRoom

        centerColors = ByteArray(121)
        sideColors = ByteArray(121)
        cachedTiles = Array(121) { null }
    }

    fun updateMap(mapData: MapData) {
        cachedTiles = Array(121) { null }

        for (y in 0..10) {
            for (x in 0..10) {
                val mapX = startX + x * halfTile
                val mapY = startY + y * halfTile

                if (mapX >= 128 || mapY >= 128) continue

                centerColors[y * 11 + x] = mapData.colors[mapY * 128 + mapX]

                val sideIndex = if (x % 2 == 0 && y % 2 == 0) {
                    val topX = mapX - halfRoom
                    val topY = mapY - halfRoom
                    topY * 128 + topX
                } else {
                    val horizontal = y % 2 == 1
                    if (horizontal) {
                        mapY * 128 + mapX - 4
                    } else {
                        (mapY - 4) * 128 + mapX
                    }
                }

                sideColors[y * 11 + x] = mapData.colors[sideIndex]
            }
        }
    }

    fun getTile(arrayX: Int, arrayY: Int): Tile {
        val index = arrayY * 11 + arrayX
        val cached = cachedTiles.getOrElse(index) { return Unknown(0, 0) }
        if (cached == null) {
            val xPos = DungeonScanner.startX + arrayX * (DungeonScanner.roomSize shr 1)
            val zPos = DungeonScanner.startZ + arrayY * (DungeonScanner.roomSize shr 1)
            cachedTiles[index] = scanTile(arrayX, arrayY, xPos, zPos)
        }
        return cachedTiles[index] ?: Unknown(0, 0)
    }

    fun getConnected(arrayX: Int, arrayY: Int): List<Room> {
        val tile = getTile(arrayX, arrayY) as? Room ?: return emptyList()
        val connected = mutableListOf<Room>()
        val queue = ArrayDeque<Room>()
        queue.add(tile)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            connected.add(current)
            queue.addAll(EnumFacing.HORIZONTALS.mapNotNull {
                getTile(current.x + it.directionVec.x, current.z + it.directionVec.z) as? Room
            })
        }
        return connected
    }

    private fun scanTile(arrayX: Int, arrayY: Int, worldX: Int, worldZ: Int): Tile {
        val centerColor = centerColors[arrayY * 11 + arrayX].toInt()
        val sideColor = sideColors[arrayY * 11 + arrayX].toInt()

        if (centerColor == 0) return Unknown(worldX, worldZ)

        return if (arrayX % 2 == 0 && arrayY % 2 == 0) {
            val type = RoomType.fromMapColor(sideColor) ?: return Unknown(worldX, worldZ)
            Room(worldX, worldZ, RoomData.createUnknown(type)).apply {
                state = when (centerColor) {
                    18 -> when (type) {
                        RoomType.BLOOD -> RoomState.DISCOVERED
                        RoomType.PUZZLE -> RoomState.FAILED
                        else -> state
                    }

                    30 -> when (type) {
                        RoomType.ENTRANCE -> RoomState.DISCOVERED
                        else -> RoomState.GREEN
                    }

                    34 -> RoomState.CLEARED

                    85, 119 -> RoomState.UNOPENED

                    else -> RoomState.DISCOVERED
                }
            }
        } else {
            if (sideColor == 0) {
                val type = DoorType.fromMapColor(centerColor) ?: return Unknown(worldX, worldZ)
                Door(worldX, worldZ, type).apply {
                    state = if (centerColor == 85) RoomState.UNOPENED else RoomState.DISCOVERED
                }
            } else {
                val type = RoomType.fromMapColor(sideColor) ?: return Unknown(worldX, worldZ)
                Room(worldX, worldZ, RoomData.createUnknown(type)).apply {
                    state = RoomState.DISCOVERED
                    isSeparator = true
                }
            }
        }
    }
}
