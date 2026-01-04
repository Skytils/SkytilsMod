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

import gg.skytils.skytilsmod.utils.Utils.colorFromString
import java.awt.Color
import kotlin.math.min

class CyclingTwoColorGradient(var color1: Color, var color2: Color, var speed: Double, var offset: Double) : CustomColor() {

    fun getCurrentColor(): Color {
        var p = (System.currentTimeMillis() + offset) / speed

        if (p > 1) {
            val decimal = p % 1
            val whole = p.toInt()
            p = if (whole % 2 == 0) decimal else (1 - decimal)
        }

        return getGradient(p)
    }

    fun getGradient(p: Double): Color {
        val complement = 1 - p
        val r = (color1.red * p + color2.red * complement).toInt()
        val g = (color1.green * p + color2.green * complement).toInt()
        val b = (color1.blue * p + color2.blue * complement).toInt()
        val a = (color1.alpha * p + color2.alpha * complement).toInt()

        return Color(r, g, b, a)
    }

    // no-op as it's currently unused
    // FIXME
    override fun applyColor() {}//getCurrentColor().bindColor()

    override fun toHSV(): FloatArray {
        val color = getCurrentColor()
        val r = color.red.toFloat()
        val g = color.blue.toFloat()
        val b = color.blue.toFloat()
        val a = color.alpha.toFloat()
        var hue: Float
        val saturation: Float
        val value: Float
        val cmax = r.coerceAtLeast(g).coerceAtLeast(b)
        val cmin = min(r.coerceAtMost(g), b)
        value = cmax
        saturation = if (cmax == 0f) 0F else (cmax - cmin) / cmax
        if (saturation == 0f) {
            hue = 0f
        } else {
            val redc = (cmax - r) / (cmax - cmin)
            val greenc = (cmax - g) / (cmax - cmin)
            val bluec = (cmax - b) / (cmax - cmin)
            hue = when {
                r == cmax -> {
                    bluec - greenc
                }
                g == cmax -> {
                    2.0f + redc - bluec
                }
                else -> {
                    4.0f + greenc - redc
                }
            }
            hue /= 6.0f
            if (hue < 0) {
                hue += 1.0f
            }
        }
        return floatArrayOf(hue, saturation, value, a)
    }

    override fun toInt(): Int {
        return getCurrentColor().rgb
    }

    override fun toString(): String {
        return "cyclingtwocolorgradient(${color1.rgb},${color2.rgb},$speed,$offset)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is CyclingTwoColorGradient) {
            return speed == other.speed && color1 == other.color1 && color2 == other.color2 && offset == other.offset
        }
        return false
    }

    override fun hashCode(): Int {
        return doubleArrayOf(color1.rgb.toDouble(), color2.rgb.toDouble(), speed).contentHashCode()
    }

    companion object {
        @JvmStatic
        fun fromString(string: String?): CyclingTwoColorGradient {
            if (string == null) throw NullPointerException("Argument cannot be null!")
            require(string.startsWith("cyclingtwocolorgradient(") && string.endsWith(")")) { "Invalid cycling two color gradient format" }
            return try {
                val split = string.substringAfter("cyclingtwocolorgradient(").substringBeforeLast(')').split(",")
                val color1 = colorFromString(split[0])
                val color2 = colorFromString(split[1])
                val speed = split[2].toDouble()
                val offset = split[3].toDouble()
                CyclingTwoColorGradient(color1, color2, speed, offset)
            } catch (exception: NumberFormatException) {
                throw IllegalArgumentException("Invalid cycling two color gradient string")
            } catch (exception: IndexOutOfBoundsException) {
                throw IllegalArgumentException("Invalid cycling two color gradient string")
            }
        }
    }
}