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

package gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.map

import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.CataclysmicMapConfig
import java.awt.Color

class Door(override val x: Int, override val z: Int, var type: DoorType) : Tile {
    var opened = false
    override var state: RoomState = RoomState.UNDISCOVERED
    override val color: Color
        get() = when (this.type) {
            DoorType.BLOOD -> CataclysmicMapConfig.colorBloodDoor
            DoorType.ENTRANCE -> CataclysmicMapConfig.colorEntranceDoor
            DoorType.WITHER -> if (opened) CataclysmicMapConfig.colorOpenWitherDoor else CataclysmicMapConfig.colorWitherDoor
            else -> CataclysmicMapConfig.colorRoomDoor
        }
}
