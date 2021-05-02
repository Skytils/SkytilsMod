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

import java.text.NumberFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

object NumberUtil {
    @JvmField
    val nf: NumberFormat = NumberFormat.getInstance(Locale.US)
    private val suffixes = TreeMap<Long, String>().apply {
        this[1000L] = "k"
        this[1000000L] = "M"
        this[1000000000L] = "B"
        this[1000000000000L] = "T"
        this[1000000000000000L] = "P"
        this[1000000000000000000L] = "E"
    }

    /**
     * This code was unmodified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/30661479
     * @author assylias
     */
    @JvmStatic
    fun format(value: Long): String {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1)
        if (value < 0) return "-" + format(-value)
        if (value < 1000) return value.toString() //deal with easy case
        val (divideBy, suffix) = suffixes.floorEntry(value)
        val truncated = value / (divideBy / 10) //the number part of the output times 10
        val hasDecimal = truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
        return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
    }

    /**
     * This code was unmodified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/22186845
     * @author jpdymond
     */
    @JvmStatic
    fun round(value: Double, precision: Int): Double {
        val scale = 10.0.pow(precision.toDouble()).toInt()
        return (value * scale).roundToInt().toDouble() / scale
    }
}