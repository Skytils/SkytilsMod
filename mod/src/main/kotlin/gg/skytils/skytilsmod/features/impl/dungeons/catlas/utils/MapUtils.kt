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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils

import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonInfo
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonMapColorParser
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonScanner
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.item.ItemMap
import net.minecraft.util.Vec4b
import net.minecraft.world.storage.MapData

object MapUtils {

    val Vec4b.mapX
        get() = (this.func_176112_b() + 128) shr 1

    val Vec4b.mapZ
        get() = (this.func_176113_c() + 128) shr 1

    val Vec4b.yaw
        get() = this.func_176111_d() * 22.5f

    var startCorner = Pair(5, 5)
    var mapRoomSize = 16
    var coordMultiplier = 0.625
    var calibrated = false

    fun getMapData(): MapData? {
        val map = mc.thePlayer?.inventory?.getStackInSlot(8) ?: return null
        if (map.item !is ItemMap || !map.displayName.contains("Magical Map")) return null
        return (map.item as ItemMap).getMapData(map, mc.theWorld)
    }

    /**
     * Calibrates map metrics based on the size and location of the entrance room.
     */
    fun calibrateMap(): Boolean {
        val (start, size) = findEntranceCorner()
        if (Utils.equalsOneOf(size, 16, 18)) {
            mapRoomSize = size
            startCorner = when (DungeonFeatures.dungeonFloorNumber) {
                0 -> Pair(22, 22)
                1 -> Pair(22, 11)
                2, 3 -> Pair(11, 11)
                else -> {
                    val startX = start and 127
                    val startZ = start shr 7
                    Pair(startX % (mapRoomSize + 4), startZ % (mapRoomSize + 4))
                }
            }
            coordMultiplier = (mapRoomSize + 4.0) / DungeonScanner.roomSize

            DungeonMapColorParser.calibrate()
            return true
        }
        return false
    }

    /**
     * Finds the starting index of the entrance room as well as the size of the room.
     */
    private fun findEntranceCorner(): Pair<Int, Int> {
        var start = 0
        var currLength = 0
        (DungeonInfo.dungeonMap ?: DungeonInfo.guessMapData)?.colors?.forEachIndexed { index, byte ->
            if (byte == 30.toByte()) {
                if (currLength == 0) start = index
                currLength++
            } else {
                if (currLength >= 16) {
                    return Pair(start, currLength)
                }
                currLength = 0
            }
        }
        return Pair(start, currLength)
    }
}
