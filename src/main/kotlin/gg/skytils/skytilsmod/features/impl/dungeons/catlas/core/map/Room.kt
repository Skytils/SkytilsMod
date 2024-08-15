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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map

import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonInfo
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonScanner
import java.awt.Color

class Room(override val x: Int, override val z: Int, var data: RoomData) : Tile {
    var core = 0
    
    var isSeparator = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() {
            return if (state == RoomState.UNOPENED) CatlasConfig.colorUnopened
            else when (data.type) {
                RoomType.BLOOD -> CatlasConfig.colorBlood
                RoomType.CHAMPION -> CatlasConfig.colorMiniboss
                RoomType.ENTRANCE -> CatlasConfig.colorEntrance
                RoomType.FAIRY -> CatlasConfig.colorFairy
                RoomType.PUZZLE -> CatlasConfig.colorPuzzle
                RoomType.RARE -> CatlasConfig.colorRare
                RoomType.TRAP -> CatlasConfig.colorTrap
                else -> CatlasConfig.colorRoom
            }
        }
    var uniqueRoom: UniqueRoom? = null

    fun getArrayPosition(): Pair<Int, Int> {
        return Pair((x - DungeonScanner.startX) / 16, (z - DungeonScanner.startZ) / 16)
    }

    fun addToUnique(row: Int, column: Int, roomName: String = data.name) {
        val unique = DungeonInfo.uniqueRooms.find { it.name == roomName }

        if (unique == null) {
            UniqueRoom(column, row, this).let {
                DungeonInfo.uniqueRooms.add(it)
                uniqueRoom = it
            }
        } else {
            unique.addTile(column, row, this)
            uniqueRoom = unique
        }
    }
}
