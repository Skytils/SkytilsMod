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
package gg.skytils.skytilsmod.utils.graphics.colors

/**
 * Represents an ordered set of colors
 *
 * Taken from Wynntils under GNU AGPL v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
class ColorSet<T : CustomColor?>(private val colors: Array<T>, names: Array<String>) {
    private val names: Array<String?> = arrayOfNulls(names.size)
    private val nameMap: MutableMap<String, T?>

    /**
     * Map text formatting colour codes to the respective CustomColor in the set
     *
     * Returns null if invalid.
     */
    fun fromCode(code: Int): T? {
        return if (code < 0 || colors.size <= code) {
            null
        } else colors[code]
    }

    /**
     * Returns the colour code for a CustomColor if it is in the set, -1 if it isn't.
     */
    fun getCode(c: CustomColor?): Int {
        if (c == null) return -1
        for (i in colors.indices) {
            if (colors[i] == c) {
                return i
            }
        }
        return -1
    }

    /**
     * Shorthand for `getCode(fromName(name))`
     */
    fun getCode(name: String?): Int {
        return getCode(fromName(name))
    }

    /**
     * Return the name colour in the set, or `null` if `c` isn't in the set.
     */
    fun getName(c: CustomColor?): String? {
        val code = getCode(c)
        return if (code == -1) null else names[code]
    }

    /**
     * Shorthand for `getName(fromCode(code))`
     */
    fun getName(code: Int): String? {
        return if (code < 0 || names.size <= code) null else names[code]
    }

    /**
     * Return the colour in the set corresponding to the name given
     */
    private fun fromName(name: String?) = name.takeIf { it != null }?.let {
        nameMap.getOrDefault(
            name?.trim()?.replace(' ', '_')?.replace("_", "")?.uppercase(), null
        )
    }


    /**
     * Returns the canonical name for a common colour (All caps, space -> underscore, will be a field name).
     * Null if this isn't the name of a common colour.
     */
    fun canonicalize(name: String?): String? {
        return getName(fromName(name))
    }

    fun has(name: String?): Boolean {
        return fromName(name) != null
    }

    fun has(code: Int): Boolean {
        return 0 <= code && code < colors.size
    }

    fun has(c: CustomColor?): Boolean {
        return getCode(c) != -1
    }

    fun valueOf(name: String?): T? {
        return fromName(name)
    }

    fun valueOf(code: Int): T? {
        return fromCode(code)
    }

    fun valueOf(c: CustomColor?): T? {
        return fromCode(getCode(c)) // Becomes null if not in the set, and also returns reference to color in set
    }

    /**
     * `size() - 1` is the maximum value for `fromCode`.
     *
     * @return The number of colours in the set
     */
    fun size(): Int {
        return colors.size
    }
    /**
     * @return the aliases for the colour with a given code
     */
    //    public Set<String> getAliases(int code) {
    //        return Collections.unmodifiableSet(aliases[code]);
    //    }
    /**
     * @return a copy of the colours in the set (that can be modified)
     */
    fun copySet(): Array<CustomColor> {
        val colors = arrayOfNulls<CustomColor>(colors.size)
        for (i in colors.indices) {
            colors[i] = CustomColor(this.colors[i]!!)
        }
        return colors.requireNoNulls()
    }

    /**
     * @return the colours as integers
     */
    fun asInts(): IntArray {
        val colors = IntArray(colors.size)
        for (i in colors.indices) {
            colors[i] = this.colors[i]!!.toInt()
        }
        return colors
    }

    init {
        assert(colors.size == this.names.size)
        nameMap = HashMap(colors.size)
        for (i in colors.indices) {
            val name = names[i].trim().replace(' ', '_').uppercase()
            nameMap[name.replace("_", "")] = colors[i]
            this.names[i] = name
        }
    }
}