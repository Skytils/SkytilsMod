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
package gg.skytils.skytilsmod.utils

class EnumUtil {
    enum class AnchorPoint(val id: Int) {
        TOP_LEFT(0), TOP_RIGHT(1), BOTTOM_LEFT(2), BOTTOM_RIGHT(3), BOTTOM_MIDDLE(4);

        fun getX(maxX: Int): Int {
            return when (this) {
                TOP_RIGHT, BOTTOM_RIGHT -> maxX
                BOTTOM_MIDDLE -> maxX / 2
                else -> 0
            }
        }

        fun getY(maxY: Int): Int {
            return when (this) {
                BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_MIDDLE -> maxY
                else -> 0
            }
        }

        companion object {
            // Accessed by reflection...
            fun fromId(id: Int): AnchorPoint? = entries.find { it.id == id }
        }
    }
}