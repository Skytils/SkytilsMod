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

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures.dungeonFloorNumber
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.CataclysmicMapElement
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.map.*
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.handlers.DungeonScanner.scan
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.utils.ScanUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

/**
 * Handles everything related to scanning the dungeon. Running [scan] will update the instance of [DungeonInfo].
 */
object DungeonScanner {

    /**
     * The size of each dungeon room in blocks.
     */
    const val roomSize = 32

    /**
     * The starting coordinates to start scanning (the north-west corner).
     */
    const val startX = -185
    const val startZ = -185

    private var lastScanTime = 0L
    var isScanning = false
    var hasScanned = false

    val shouldScan: Boolean
        get() = !isScanning && !hasScanned && System.currentTimeMillis() - lastScanTime >= 250 && dungeonFloorNumber != null

    fun scan() {
        isScanning = true
        var allChunksLoaded = true

        // Scans the dungeon in a 11x11 grid.
        for (x in 0..10) {
            for (z in 0..10) {
                // Translates the grid index into world position.
                val xPos = startX + x * (roomSize shr 1)
                val zPos = startZ + z * (roomSize shr 1)

                if (!mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4).isLoaded) {
                    // The room being scanned has not been loaded in.
                    allChunksLoaded = false
                    continue
                }

                // This room has already been added in a previous scan.
                if (DungeonInfo.dungeonList[x + z * 11].run {
                        this !is Unknown && (this as? Room)?.data?.name != "Unknown"
                    }) continue

                scanRoom(xPos, zPos, z, x)?.let {
                    DungeonInfo.dungeonList[z * 11 + x] = it
                }
            }
        }

        if (allChunksLoaded) {
            DungeonInfo.roomCount = DungeonInfo.dungeonList.filter { it is Room && !it.isSeparator }.size
            hasScanned = true
        }

        lastScanTime = System.currentTimeMillis()
        isScanning = false
    }

    private fun scanRoom(x: Int, z: Int, row: Int, column: Int): Tile? {
        val height = mc.theWorld.getChunkFromChunkCoords(x shr 4, z shr 4).getHeightValue(x and 15, z and 15)
        if (height == 0) return null

        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            // Scanning a room
            rowEven && columnEven -> {
                val roomCore = ScanUtils.getCore(x, z)
                Room(x, z, ScanUtils.getRoomData(roomCore) ?: return null).apply {
                    core = roomCore
                    // Checks if a room with the same name has already been scanned.
                    val duplicateRoom = DungeonInfo.uniqueRooms.firstOrNull { it.first.data.name == data.name }

                    if (duplicateRoom == null) {
                        DungeonInfo.uniqueRooms.add(this to (column to row))
                        DungeonInfo.cryptCount += data.crypts
                        DungeonInfo.secretCount += data.secrets
                        when (data.type) {
                            RoomType.ENTRANCE -> CataclysmicMapElement.dynamicRotation = when {
                                row == 0 -> 180f
                                column == 0 -> -90f
                                column > row -> 90f
                                else -> 0f
                            }

                            RoomType.TRAP -> DungeonInfo.trapType = data.name.split(" ")[0]
                            RoomType.PUZZLE -> {}//Puzzle.fromName(data.name)?.let { DungeonInfo.puzzles.putIfAbsent(it, false) }

                            else -> {}
                        }
                    } else if (x < duplicateRoom.first.x || (x == duplicateRoom.first.x && z < duplicateRoom.first.z)) {
                        DungeonInfo.uniqueRooms.remove(duplicateRoom)
                        DungeonInfo.uniqueRooms.add(this to (column to row))
                    }
                }
            }

            // Can only be the center "block" of a 2x2 room.
            !rowEven && !columnEven -> {
                DungeonInfo.dungeonList[column - 1 + (row - 1) * 11].let {
                    if (it is Room) Room(x, z, it.data).apply { isSeparator = true } else null
                }
            }

            // Doorway between rooms
            // Old trap has a single block at 82
            height == 74 || height == 82 -> {
                Door(
                    x, z,
                    // Finds door type from door block
                    type = when (mc.theWorld.getBlockState(BlockPos(x, 69, z)).block) {
                        Blocks.coal_block -> {
                            DungeonInfo.witherDoors++
                            DoorType.WITHER
                        }

                        Blocks.monster_egg -> DoorType.ENTRANCE
                        Blocks.stained_hardened_clay -> DoorType.BLOOD
                        else -> DoorType.NORMAL
                    }
                )
            }

            // Connection between large rooms
            else -> {
                DungeonInfo.dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column].let {
                    if (it !is Room) {
                        null
                    } else if (it.data.type == RoomType.ENTRANCE) {
                        Door(x, z, DoorType.ENTRANCE)
                    } else {
                        Room(x, z, it.data).apply { isSeparator = true }
                    }
                }
            }
        }
    }
}
