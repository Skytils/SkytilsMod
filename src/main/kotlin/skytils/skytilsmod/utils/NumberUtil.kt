/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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
    private val romanSymbols = TreeMap(
        mapOf(
            1000 to "M",
            900 to "CM",
            500 to "D",
            400 to "CD",
            100 to "C",
            90 to "XC",
            50 to "L",
            40 to "XL",
            10 to "X",
            9 to "IX",
            5 to "V",
            4 to "IV",
            1 to "I",
        )
    )

    /**
     * This code was unmodified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/30661479
     * @author assylias
     */
    @JvmStatic
    fun format(value: Number): String {
        @Suppress("NAME_SHADOWING")
        val value = value.toLong()
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1)
        if (value < 0) return "-" + format(-value)
        if (value < 1000) return value.toString() //deal with easy case
        val (divideBy, suffix) = suffixes.floorEntry(value)
        val truncated = value / (divideBy / 10) //the number part of the output times 10
        val hasDecimal = truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
        return if (hasDecimal) (truncated / 10.0).toString() + suffix else (truncated / 10).toString() + suffix
    }

    @JvmStatic
    fun unformat(value: String): Long {
        val suffix = value.filter { !it.isDigit() }.lowercase()
        val num = value.filter { it.isDigit() }.toLong()
        return num * (suffixes.entries.find { it.value.lowercase() == suffix }?.key ?: 1)
    }

    /**
     * This code was unmodified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/22186845
     * @author jpdymond
     */
    fun Double.roundToPrecision(precision: Int): Double {
        val scale = 10.0.pow(precision).toInt()
        return (this * scale).roundToInt().toDouble() / scale
    }

    /**
     * This code was unmodified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/22186845
     * @author jpdymond
     */
    fun Float.roundToPrecision(precision: Int): Float {
        val scale = 10.0.pow(precision).toInt()
        return (this * scale).roundToInt().toFloat() / scale
    }

    fun Number.addSuffix(): String {
        val long = this.toLong()
        if (long in 11..13) return "${this}th"
        return when (long % 10) {
            1L -> "${this}st"
            2L -> "${this}nd"
            3L -> "${this}rd"
            else -> "${this}th"
        }
    }

    /**
     * This code was converted to Kotlin and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/9073310
     */
    fun String.romanToDecimal(): Int {
        var decimal = 0
        var lastNumber = 0
        val romanNumeral = this.uppercase()
        for (x in romanNumeral.length - 1 downTo 0) {
            when (romanNumeral[x]) {
                'M' -> {
                    decimal = processDecimal(1000, lastNumber, decimal)
                    lastNumber = 1000
                }
                'D' -> {
                    decimal = processDecimal(500, lastNumber, decimal)
                    lastNumber = 500
                }
                'C' -> {
                    decimal = processDecimal(100, lastNumber, decimal)
                    lastNumber = 100
                }
                'L' -> {
                    decimal = processDecimal(50, lastNumber, decimal)
                    lastNumber = 50
                }
                'X' -> {
                    decimal = processDecimal(10, lastNumber, decimal)
                    lastNumber = 10
                }
                'V' -> {
                    decimal = processDecimal(5, lastNumber, decimal)
                    lastNumber = 5
                }
                'I' -> {
                    decimal = processDecimal(1, lastNumber, decimal)
                    lastNumber = 1
                }
            }
        }
        return decimal
    }

    fun Int.toRoman(): String {
        val l: Int = romanSymbols.floorKey(this)
        return if (this == l) {
            romanSymbols[this]!!
        } else romanSymbols[l] + (this - l).toRoman()
    }

    private fun processDecimal(decimal: Int, lastNumber: Int, lastDecimal: Int): Int {
        return if (lastNumber > decimal) {
            lastDecimal - decimal
        } else {
            lastDecimal + decimal
        }
    }
}