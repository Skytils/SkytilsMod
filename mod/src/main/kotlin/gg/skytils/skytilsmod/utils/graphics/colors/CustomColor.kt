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

import gg.skytils.skytilsmod.utils.MathUtil
import gg.skytils.skytilsmod.utils.Utils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import com.mojang.blaze3d.systems.RenderSystem
import org.apache.commons.codec.digest.DigestUtils

/** CustomColor
 * will represent color or complex colors
 * in a more efficient way than awt's Color or minecraft's color ints.
 *
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
open class CustomColor {
    // The RED   value of the color(0.0f -> 1.0f)
    var r = 0f

    // The GREEN value of the color(0.0f -> 1.0f)
    var g = 0f

    // The BLUE  value of the color(0.0f -> 1.0f)
    var b = 0f

    // The ALPHA value of the color(0.0f -> 1.0f)
    var a = 0f


    @JvmOverloads
    constructor(r: Float, g: Float, b: Float, a: Float = 1.0f) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
    }

    constructor(r: Int, g: Int, b: Int) {
        this.r = r / 255f
        this.g = g / 255f
        this.b = b / 255f
        a = 1.0f
    }

    constructor()
    constructor(c: CustomColor) : this(c.r, c.g, c.b, c.a)

    /** applyColor
     * Will set the color to OpenGL's active color
     */
    // no-op as it's no longer used
    // FIXME
    open fun applyColor() {
//        RenderSystem.setShaderColor(r, g, b, a)
    }

    open fun setA(a: Float): CustomColor {
        this.a = a
        return this
    }

    /**
     * @return float[4]{ h, s, v, a }
     */
    open fun toHSV(): FloatArray {
        var hue: Float
        val saturation: Float
        val value: Float
        val cmax = r.coerceAtLeast(g).coerceAtLeast(b)
        val cmin = r.coerceAtMost(g).coerceAtMost(b)
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

    /**
     * `c.toInt() & 0xFFFFFF` to get `0xRRGGBB` (without alpha)
     *
     * @return 0xAARRGGBB
     */
    open fun toInt(): Int {
        val r = (r.coerceAtMost(1f) * 255).toInt()
        val g = (g.coerceAtMost(1f) * 255).toInt()
        val b = (b.coerceAtMost(1f) * 255).toInt()
        val a = (a.coerceAtMost(1f) * 255).toInt()
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    fun toInt(alpha: Int): Int {
        val r = (r.coerceAtMost(1f) * 255).toInt()
        val g = (g.coerceAtMost(1f) * 255).toInt()
        val b = (b.coerceAtMost(1f) * 255).toInt()
        val a = alpha.coerceAtMost(255)
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    /** this is = rgba(255,255,255,255)  */
    override fun toString(): String {
        return "rgba(" + (r * 255).toInt() + "," + (g * 255).toInt() + "," + (b * 255).toInt() + "," + (a * 255).toInt() + ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is CustomColor) {
            return r == other.r && g == other.g && b == other.b && a == other.a
        }
        return false
    }

    override fun hashCode(): Int {
        return floatArrayOf(r, g, b, a).contentHashCode()
    }

    /* package-private */
    open class SetBase : CustomColor {
        constructor(rgb: Int) : super((rgb shr 16) / 255f, (rgb shr 8 and 0xFF) / 255f, (rgb and 0xFF) / 255f, 1f)
        constructor(r: Float, g: Float, b: Float, a: Float) : super(r, g, b, a)

        // Prevent setA on global references. Create a copy with `new CustomColor(c)` first.
        override fun setA(a: Float): CustomColor {
            UnsupportedOperationException("Cannot set alpha of common color").printStackTrace()
            return this
        }
    }

    companion object {
        fun fromString(string: String?, a: Float): CustomColor {
            if (string == null) return CustomColor(0F, 0F, 0F, a)
            var withoutHash = string.trim()
            if (withoutHash.startsWith("#")) withoutHash = withoutHash.substring(1).trim()
            if (withoutHash == "") return CustomColor(0F, 0F, 0F, a)
            when (withoutHash.length) {
                6 -> {
                    try {
                        return fromInt(withoutHash.toInt(16), a)
                    } catch (ignored: Exception) {
                    }
                }
                3 -> {
                    // "rgb" -> "rrggbb"
                    try {
                        val rgb = withoutHash.toInt(16)
                        val r = (rgb shr 8 and 0xF) * 0x11
                        val g = (rgb shr 4 and 0xF) * 0x11
                        val b = (rgb and 0xF) * 0x11
                        return fromBytes(r.toByte(), g.toByte(), b.toByte(), a)
                    } catch (ignored: Exception) {
                    }
                }
                2 -> {
                    // "vv" -> "vvvvvv"
                    try {
                        val v = withoutHash.toInt(16).toByte()
                        return fromBytes(v, v, v, a)
                    } catch (ignored: Exception) {
                    }
                }
            }
            val hash = DigestUtils.sha1(string)
            return fromBytes(hash[0], hash[1], hash[2], a)
        }

        fun fromHSV(h: Float, s: Float, v: Float, a: Float): CustomColor {
            var s1 = s
            var v4 = v
            var a1 = a
            a1 = a1.coerceIn(0f, 1f)
            if (v4 <= 0) return CustomColor(0F, 0F, 0F, a1)
            if (v4 > 1) v4 = 1f
            if (s1 <= 0) return CustomColor(v4, v4, v4, a1)
            if (s1 > 1) s1 = 1f
            val vh = (h % 1 + 1) * 6 % 6
            val vi = MathUtil.fastFloor(vh.toDouble())
            val v1 = v4 * (1 - s1)
            val v2 = v4 * (1 - s1 * (vh - vi))
            val v3 = v4 * (1 - s1 * (1 - (vh - vi)))
            return when (vi) {
                0 -> CustomColor(v4, v3, v1, a1)
                1 -> CustomColor(v2, v4, v1, a1)
                2 -> CustomColor(v1, v4, v3, a1)
                3 -> CustomColor(v1, v2, v4, a1)
                4 -> CustomColor(v3, v1, v4, a1)
                else -> CustomColor(v4, v1, v2, a1)
            }
        }

        /**
         * Construct a CustomColor from an int 0xAARRGGBB
         *
         * @param argb 32 bits with the most significant 8 being the value of the alpha channel, followed by rgb values
         * @return A custom colour such that fromInt(x).toInt() == x
         */
        fun fromInt(argb: Int): CustomColor {
            return fromInt(argb and 0xFFFFFF, (argb ushr 24 and 0xFF) / 255f)
        }

        /**
         * Construct a CustomColor from an int 0xRRGGBB and an alpha value
         *
         * @param rgb 24 bits (Most significant 8 are ignored)
         * @param a Alpha value of colour
         * @return A custom colour such that fromInt(rgb, a).toInt() & 0xFFFFFF == rgb && fromInt(rgb, a).a == a
         */
        fun fromInt(rgb: Int, a: Float): CustomColor {
            return fromBytes((rgb ushr 16).toByte(), (rgb ushr 8).toByte(), rgb.toByte(), a)
        }

        fun fromBytes(r: Byte, g: Byte, b: Byte, a: Byte): CustomColor {
            return fromBytes(r, g, b, java.lang.Byte.toUnsignedInt(a) / 255f)
        }

        @JvmOverloads
        fun fromBytes(r: Byte, g: Byte, b: Byte, a: Float = 0f): CustomColor {
            return CustomColor(
                java.lang.Byte.toUnsignedInt(r) / 255f,
                java.lang.Byte.toUnsignedInt(g) / 255f,
                java.lang.Byte.toUnsignedInt(b) / 255f,
                a
            )
        }
    }

    object Serializer : KSerializer<CustomColor> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CustomColor", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): CustomColor = Utils.customColorFromString(decoder.decodeString())
        override fun serialize(encoder: Encoder, value: CustomColor) = encoder.encodeString(value.toString())
    }
}