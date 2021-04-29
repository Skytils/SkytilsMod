/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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
package skytils.skytilsmod.utils

object StringUtils {
    @JvmStatic
    fun stripControlCodes(string: String?): String {
        return net.minecraft.util.StringUtils.stripControlCodes(string)
    }

    @JvmStatic
    fun startsWith(string: CharSequence?, sequence: CharSequence?): Boolean {
        return org.apache.commons.lang3.StringUtils.startsWith(string, sequence)
    }

    @JvmStatic
    fun startsWithAny(string: CharSequence?, vararg sequences: CharSequence?): Boolean {
        return org.apache.commons.lang3.StringUtils.startsWithAny(string, *sequences)
    }

    @JvmStatic
    fun containsAny(string: CharSequence?, vararg sequences: CharSequence?): Boolean {
        if (string == null) return false
        for(sequence in sequences) {
            if (sequence?.let { string.contains(it) } == true) {
                return true
            }
        }
        return false
    }
}