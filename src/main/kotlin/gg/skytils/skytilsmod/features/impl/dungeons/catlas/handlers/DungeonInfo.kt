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

import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.*
import net.minecraft.item.map.MapState

object DungeonInfo {
    // 6 x 6 room grid, 11 x 11 with connections
    val dungeonList = Array<Tile>(121) { Unknown(0, 0) }
    val uniqueRooms = mutableMapOf<String, UniqueRoom>()
    var roomCount = 0
    val puzzles = mutableMapOf<Puzzle, Boolean>()

    var trapType = ""
    var witherDoors = 0
    var cryptCount = 0
    var secretCount = 0

    var keys = 0

    var dungeonMap: MapState? = null
    var guessMapData: MapState? = null

    fun reset() {
        dungeonList.fill(Unknown(0, 0))
        roomCount = 0
        uniqueRooms.clear()
        puzzles.clear()

        trapType = ""
        witherDoors = 0
        cryptCount = 0
        secretCount = 0

        keys = 0

        dungeonMap = null
        guessMapData = null
    }
}