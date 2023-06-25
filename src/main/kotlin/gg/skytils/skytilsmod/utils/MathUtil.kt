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

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
object MathUtil {
    /**
     * returns par0 cast as an int, and no greater than Integer.MAX_VALUE-1024
     */
    fun fastFloor(value: Double) = (value + 1024.0).toInt() - 1024

    fun ceil(value: Float): Int {
        val i = value.toInt()
        return if (value > i.toFloat()) i + 1 else i
    }

    fun ceil(value: Double): Int {
        val i = value.toInt()
        return if (value > i.toDouble()) i + 1 else i
    }

    /**
     * Returns the greatest integer less than or equal to the float argument
     */
    fun floor(value: Float): Int {
        val i = value.toInt()
        return if (value < i.toFloat()) i - 1 else i
    }
}