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

import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.math.min

class RainbowColor(var speed: Int, var offset: Int, var saturation: Float, var brightness: Float) : CustomColor() {
    val rainbowColor: Color
        get() {
            var hue = ((System.currentTimeMillis() + offset) % if (speed == 0) 1 else speed).toFloat()
            hue /= (if (speed == 0) 1 else speed).toFloat()
            return Color.getHSBColor(hue, saturation, brightness)
        }

    override fun applyColor() {
        val color = rainbowColor
        GlStateManager.color(color.red / 255f, color.blue / 255f, color.green / 255f)
    }

    override fun toHSV(): FloatArray {
        val color = rainbowColor
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
        return rainbowColor.rgb
    }

    override fun toString(): String {
        return "rainbow($speed,$offset,$saturation,$brightness)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is RainbowColor) {
            return speed == other.speed && offset == other.offset && saturation == other.saturation && brightness == other.brightness
        }
        return false
    }

    override fun hashCode(): Int {
        return floatArrayOf(speed.toFloat(), offset.toFloat(), saturation, brightness).contentHashCode()
    }

    companion object {
        @JvmStatic
        fun fromString(string: String?): RainbowColor {
            if (string == null) throw NullPointerException("Argument cannot be null!")
            require(!(!string.startsWith("rainbow(") && !string.endsWith(")"))) { "Invalid rainbow color format" }
            return try {
                val first = string.indexOf(",")
                val second = string.indexOf(",", first + 1)
                val third = string.lastIndexOf(",")
                val speed = string.substring(string.indexOf("(") + 1, first).toInt()
                val offset = string.substring(first + 1, second).toInt()
                val saturation = string.substring(second + 1, third).toFloat()
                val brightness = string.substring(third + 1, string.indexOf(")")).toFloat()
                RainbowColor(speed, offset, saturation, brightness)
            } catch (exception: NumberFormatException) {
                throw IllegalArgumentException("Invalid rainbow string")
            }
        }
    }
}