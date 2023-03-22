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
// This file was taken from beryx's awt-color-factory due to ForgeGradle not playing nicely, the original copyright header is below.
/*
 * Copyright (c) 2018 the original author or authors
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation. The authors designate this
 * particular file as subject to the "Classpath" exception as provided
 * by the authors in the LICENSE file that accompanied this code.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
// This file contains code adapted from the OpenJFX project, which is part of OpenJDK.
// Specifically, code portions from the following classes appear (in changed or unchanged form) in this file:
// javafx.scene.paint.Color.java (http://hg.openjdk.java.net/openjfx/jfx-dev/rt/raw-file/2c80e5ef751e/modules/javafx.graphics/src/main/java/javafx/scene/paint/Color.java)
// com.sun.javafx.util.Utils.java (http://hg.openjdk.java.net/openjfx/jfx-dev/rt/raw-file/2c80e5ef751e/modules/javafx.graphics/src/main/java/com/sun/javafx/util/Utils.java)
//
// The original copyright header attached to the above mentioned sources is included below:
/*
 * Copyright (c) 2010, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package gg.skytils.skytilsmod.utils.graphics.colors

import java.awt.Color
import java.util.*
import kotlin.math.floor

object ColorFactory {
    /**
     * Creates an opaque `Color` based on the specified values in the HSB color model.
     *
     * @param hue the hue, in degrees
     * @param saturation the saturation, `0.0 to 1.0`
     * @param brightness the brightness, `0.0 to 1.0`
     * @return the `Color`
     *
     * out of range
     */
    @JvmOverloads
    fun hsb(
        hue: Double,
        saturation: Double,
        brightness: Double,
        opacity: Double = 1.0
    ): Color {
        checkSB(saturation, brightness)
        val rgb = HSBtoRGB(hue, saturation, brightness)
        return Color(rgb[0].toFloat(), rgb[1].toFloat(), rgb[2].toFloat(), opacity.toFloat())
    }

    private fun checkSB(saturation: Double, brightness: Double) {
        require(!(saturation < 0.0 || saturation > 1.0)) { "Color.hsb's saturation parameter ($saturation) expects values 0.0-1.0" }
        require(!(brightness < 0.0 || brightness > 1.0)) { "Color.hsb's brightness parameter ($brightness) expects values 0.0-1.0" }
    }
    /**
     * Creates an RGB color specified with an HTML or CSS attribute string.
     *
     *
     *
     * This method supports the following formats:
     *
     *  * Any standard HTML color name
     *  * An HTML long or short format hex string with an optional hex alpha
     * channel.
     * Hexadecimal values may be preceded by either `"0x"` or `"#"`
     * and can either be 2 digits in the range `00` to `0xFF` or a
     * single digit in the range `0` to `F`.
     *  * An `rgb(r,g,b)` or `rgba(r,g,b,a)` format string.
     * Each of the `r`, `g`, or `b` values can be an integer
     * from 0 to 255 or a floating point percentage value from 0.0 to 100.0
     * followed by the percent (`%`) character.
     * The alpha component, if present, is a
     * floating point value from 0.0 to 1.0.  Spaces are allowed before or
     * after the numbers and between the percentage number and its percent
     * sign (`%`).
     *  * An `hsl(h,s,l)` or `hsla(h,s,l,a)` format string.
     * The `h` value is a floating point number from 0.0 to 360.0
     * representing the hue angle on a color wheel in degrees with
     * `0.0` or `360.0` representing red, `120.0`
     * representing green, and `240.0` representing blue.  The
     * `s` value is the saturation of the desired color represented
     * as a floating point percentage from gray (`0.0`) to
     * the fully saturated color (`100.0`) and the `l` value
     * is the desired lightness or brightness of the desired color represented
     * as a floating point percentage from black (`0.0`) to the full
     * brightness of the color (`100.0`).
     * The alpha component, if present, is a floating
     * point value from 0.0 to 1.0.  Spaces are allowed before or
     * after the numbers and between the percentage number and its percent
     * sign (`%`).
     *
     *
     *
     * For formats without an alpha component and for named colors, opacity
     * is set according to the `opacity` argument. For colors specified
     * with an alpha component, the resulting opacity is a combination of the
     * parsed alpha component and the `opacity` argument, so a
     * transparent color becomes more transparent by specifying opacity.
     *
     *
     * Examples:
     * <div class="classUseContainer">
     * <table class="overviewSummary">
     * <caption>Web Color Format Table</caption>
     * <tr>
     * <th scope="col" class="colFirst">Web Format String</th>
     * <th scope="col" class="colLast">Equivalent constructor or factory call</th>
    </tr> *
     * <tr class="rowColor">
     * <th scope="row" class="colFirst">`Color.web("orange", 0.5);`</th>
     * <td class="colLast">`new Color(1.0, 0xA5/255.0, 0.0, 0.5)`</td>
    </tr> *
     * <tr class="altColor">
     * <th scope="row" class="colFirst">`Color.web("0xff66cc33", 0.5);`</th>
     * <td class="colLast">`new Color(1.0, 0.4, 0.8, 0.1)`</td>
    </tr> *
     * <tr class="rowColor">
     * <th scope="row" class="colFirst">`Color.web("0xff66cc", 0.5);`</th>
     * <td class="colLast">`new Color(1.0, 0.4, 0.8, 0.5)`</td>
    </tr> *
     * <tr class="altColor">
     * <th scope="row" class="colFirst">`Color.web("#ff66cc", 0.5);`</th>
     * <td class="colLast">`new Color(1.0, 0.4, 0.8, 0.5)`</td>
    </tr> *
     * <tr class="rowColor">
     * <th scope="row" class="colFirst">`Color.web("#f68", 0.5);`</th>
     * <td class="colLast">`new Color(1.0, 0.4, 0.8, 0.5)`</td>
    </tr> *
     * <tr class="altColor">
     * <th scope="row" class="colFirst">`Color.web("rgb(255,102,204)", 0.5);`</th>
     * <td class="colLast">`new Color(1.0, 0.4, 0.8, 0.5)`</td>
    </tr> *
     * <tr class="rowColor">
     * <th scope="row" class="colFirst">`Color.web("rgb(100%,50%,50%)", 0.5);`</th>
     * <td class="colLast">`new Color(1.0, 0.5, 0.5, 0.5)`</td>
    </tr> *
     * <tr class="altColor">
     * <th scope="row" class="colFirst">`Color.web("rgb(255,50%,50%,0.25)", 0.5);`</th>
     * <td class="colLast">`new Color(1.0, 0.5, 0.5, 0.125)`</td>
    </tr> *
     * <tr class="rowColor">
     * <th scope="row" class="colFirst">`Color.web("hsl(240,100%,100%)", 0.5);`</th>
     * <td class="colLast">`Color.hsb(240.0, 1.0, 1.0, 0.5)`</td>
    </tr> *
     * <tr class="altColor">
     * <th scope="row" style="border-bottom:1px solid" class="colFirst">
     * `Color.web("hsla(120,0%,0%,0.25)", 0.5);`
    </th> *
     * <td style="border-bottom:1px solid" class="colLast">
     * `Color.hsb(120.0, 0.0, 0.0, 0.125)`
    </td> *
    </tr> *
    </table> *
    </div> *
     *
     * @param colorString the name or numeric representation of the color
     * in one of the supported formats
     * @param opacity the opacity component in range from 0.0 (transparent)
     * to 1.0 (opaque)
     * @return the RGB color specified with the colorString
     *
     *
     * an unsupported color name or contains an illegal numeric value
     */
    /**
     * Creates an RGB color specified with an HTML or CSS attribute string.
     *
     *
     *
     * This method supports the following formats:
     *
     *  * Any standard HTML color name
     *  * An HTML long or short format hex string with an optional hex alpha
     * channel.
     * Hexadecimal values may be preceded by either `"0x"` or `"#"`
     * and can either be 2 digits in the range `00` to `0xFF` or a
     * single digit in the range `0` to `F`.
     *  * An `rgb(r,g,b)` or `rgba(r,g,b,a)` format string.
     * Each of the `r`, `g`, or `b` values can be an integer
     * from 0 to 255 or a floating point percentage value from 0.0 to 100.0
     * followed by the percent (`%`) character.
     * The alpha component, if present, is a
     * floating point value from 0.0 to 1.0.  Spaces are allowed before or
     * after the numbers and between the percentage number and its percent
     * sign (`%`).
     *  * An `hsl(h,s,l)` or `hsla(h,s,l,a)` format string.
     * The `h` value is a floating point number from 0.0 to 360.0
     * representing the hue angle on a color wheel in degrees with
     * `0.0` or `360.0` representing red, `120.0`
     * representing green, and `240.0` representing blue.  The
     * `s` value is the saturation of the desired color represented
     * as a floating point percentage from gray (`0.0`) to
     * the fully saturated color (`100.0`) and the `l` value
     * is the desired lightness or brightness of the desired color represented
     * as a floating point percentage from black (`0.0`) to the full
     * brightness of the color (`100.0`).
     * The alpha component, if present, is a floating
     * point value from 0.0 to 1.0.  Spaces are allowed before or
     * after the numbers and between the percentage number and its percent
     * sign (`%`).
     *
     *
     *
     * Examples:
     * <div class="classUseContainer">
     * <table class="overviewSummary">
     * <caption>Web Color Format Table</caption>
     * <tr>
     * <th scope="col" class="colFirst">Web Format String</th>
     * <th scope="col" class="colLast">Equivalent constant or factory call</th>
    </tr> *
     * <tr class="rowColor">
     * <th scope="row" class="colFirst">`Color.web("orange");`</th>
     * <td class="colLast">`Color.ORANGE`</td>
    </tr> *
     * <tr class="altColor">
     * <th scope="row" class="colFirst">`Color.web("0xff668840");`</th>
     * <td class="colLast">`Color.rgb(255, 102, 136, 0.25)`</td>
    </tr> *
     * <tr class="rowColor">
     * <th scope="row" class="colFirst">`Color.web("0xff6688");`</th>
     * <td class="colLast">`Color.rgb(255, 102, 136, 1.0)`</td>
    </tr> *
     * <tr class="altColor">
     * <th scope="row" class="colFirst">`Color.web("#ff6688");`</th>
     * <td class="colLast">`Color.rgb(255, 102, 136, 1.0)`</td>
    </tr> *
     * <tr class="rowColor">
     * <th scope="row" class="colFirst">`Color.web("#f68");`</th>
     * <td class="colLast">`Color.rgb(255, 102, 136, 1.0)`</td>
    </tr> *
     * <tr class="altColor">
     * <th scope="row" class="colFirst">`Color.web("rgb(255,102,136)");`</th>
     * <td class="colLast">`Color.rgb(255, 102, 136, 1.0)`</td>
    </tr> *
     * <tr class="rowColor">
     * <th scope="row" class="colFirst">`Color.web("rgb(100%,50%,50%)");`</th>
     * <td class="colLast">`Color.rgb(255, 128, 128, 1.0)`</td>
    </tr> *
     * <tr class="altColor">
     * <th scope="row" class="colFirst">`Color.web("rgb(255,50%,50%,0.25)");`</th>
     * <td class="colLast">`Color.rgb(255, 128, 128, 0.25)`</td>
    </tr> *
     * <tr class="rowColor">
     * <th scope="row" class="colFirst">`Color.web("hsl(240,100%,100%)");`</th>
     * <td class="colLast">`Color.hsb(240.0, 1.0, 1.0, 1.0)`</td>
    </tr> *
     * <tr class="altColor">
     * <th scope="row" style="border-bottom:1px solid" class="colFirst">
     * `Color.web("hsla(120,0%,0%,0.25)");`
    </th> *
     * <td style="border-bottom:1px solid" class="colLast">
     * `Color.hsb(120.0, 0.0, 0.0, 0.25)`
    </td> *
    </tr> *
    </table> *
    </div> *
     *
     * @param colorString the name or numeric representation of the color
     * in one of the supported formats
     * @return an RGB color
     *
     *
     * an unsupported color name or contains an illegal numeric value
     */
    @JvmOverloads
    fun web(colorString: String?, opacity: Double = 1.0): Color {
        if (colorString == null) {
            throw NullPointerException(
                "The color components or name must be specified"
            )
        }
        require(colorString.isNotEmpty()) { "Invalid color specification" }
        var color = colorString.lowercase()
        if (color.startsWith("#")) {
            color = color.substring(1)
        } else if (color.startsWith("0x")) {
            color = color.substring(2)
        } else if (color.startsWith("rgb")) {
            if (color.startsWith("(", 3)) {
                return parseRGBColor(color, 4, false, opacity)
            } else if (color.startsWith("a(", 3)) {
                return parseRGBColor(color, 5, true, opacity)
            }
        } else if (color.startsWith("hsl")) {
            if (color.startsWith("(", 3)) {
                return parseHSLColor(color, 4, false, opacity)
            } else if (color.startsWith("a(", 3)) {
                return parseHSLColor(color, 5, true, opacity)
            }
        } else {
            val col = NamedColors[color]
            if (col != null) {
                return if (opacity == 1.0) {
                    col
                } else {
                    Color(col.red, col.green, col.blue, (255 * opacity + 0.5).toInt())
                }
            }
        }
        val len = color.length
        try {
            val r: Int
            val g: Int
            val b: Int
            val a: Int
            when (len) {
                3 -> {
                    r = color.substring(0, 1).toInt(16)
                    g = color.substring(1, 2).toInt(16)
                    b = color.substring(2, 3).toInt(16)
                    return Color(r / 15.0f, g / 15.0f, b / 15.0f, opacity.toFloat())
                }

                4 -> {
                    r = color.substring(0, 1).toInt(16)
                    g = color.substring(1, 2).toInt(16)
                    b = color.substring(2, 3).toInt(16)
                    a = color.substring(3, 4).toInt(16)
                    return Color(r / 15.0f, g / 15.0f, b / 15.0f, (opacity * a / 15.0).toFloat())
                }

                6 -> {
                    r = color.substring(0, 2).toInt(16)
                    g = color.substring(2, 4).toInt(16)
                    b = color.substring(4, 6).toInt(16)
                    return Color(r, g, b, (opacity * 255 + 0.5).toInt())
                }

                8 -> {
                    r = color.substring(0, 2).toInt(16)
                    g = color.substring(2, 4).toInt(16)
                    b = color.substring(4, 6).toInt(16)
                    a = color.substring(6, 8).toInt(16)
                    return Color(r, g, b, (opacity * a + 0.5).toInt())
                }
            }
        } catch (_: NumberFormatException) {
        }
        throw IllegalArgumentException("Invalid color specification")
    }

    private fun parseRGBColor(
        color: String, roff: Int,
        hasAlpha: Boolean, a: Double
    ): Color {
        var a1 = a
        try {
            val rend = color.indexOf(',', roff)
            val gend = if (rend < 0) -1 else color.indexOf(',', rend + 1)
            val bend = if (gend < 0) -1 else color.indexOf(if (hasAlpha) ',' else ')', gend + 1)
            val aend = if (hasAlpha) if (bend < 0) -1 else color.indexOf(')', bend + 1) else bend
            if (aend >= 0) {
                val r = parseComponent(color, roff, rend, PARSE_COMPONENT)
                val g = parseComponent(color, rend + 1, gend, PARSE_COMPONENT)
                val b = parseComponent(color, gend + 1, bend, PARSE_COMPONENT)
                if (hasAlpha) {
                    a1 *= parseComponent(color, bend + 1, aend, PARSE_ALPHA)
                }
                return Color(r.toFloat(), g.toFloat(), b.toFloat(), a1.toFloat())
            }
        } catch (_: NumberFormatException) {
        }
        throw IllegalArgumentException("Invalid color specification")
    }

    private fun parseHSLColor(
        color: String, hoff: Int,
        hasAlpha: Boolean, a: Double
    ): Color {
        var alpha = a
        try {
            val hend = color.indexOf(',', hoff)
            val send = if (hend < 0) -1 else color.indexOf(',', hend + 1)
            val lend = if (send < 0) -1 else color.indexOf(if (hasAlpha) ',' else ')', send + 1)
            val aend = if (hasAlpha) if (lend < 0) -1 else color.indexOf(')', lend + 1) else lend
            if (aend >= 0) {
                val h = parseComponent(color, hoff, hend, PARSE_ANGLE)
                val s = parseComponent(color, hend + 1, send, PARSE_PERCENT)
                val l = parseComponent(color, send + 1, lend, PARSE_PERCENT)
                if (hasAlpha) {
                    alpha *= parseComponent(color, lend + 1, aend, PARSE_ALPHA)
                }
                return hsb(h, s, l, alpha)
            }
        } catch (_: NumberFormatException) {
        }
        throw IllegalArgumentException("Invalid color specification")
    }

    private const val PARSE_COMPONENT = 0 // percent, or clamped to [0,255] => [0,1]
    private const val PARSE_PERCENT = 1 // clamped to [0,100]% => [0,1]
    private const val PARSE_ANGLE = 2 // clamped to [0,360]
    private const val PARSE_ALPHA = 3 // clamped to [0.0,1.0]
    private fun parseComponent(color: String, off: Int, end: Int, type: Int): Double {
        var color1 = color
        var type1 = type
        color1 = color1.substring(off, end).trim()
        if (color1.endsWith("%")) {
            require(type1 <= PARSE_PERCENT) { "Invalid color specification" }
            type1 = PARSE_PERCENT
            color1 = color1.substring(0, color1.length - 1).trim()
        } else require(type1 != PARSE_PERCENT) { "Invalid color specification" }
        val c = if (type1 == PARSE_COMPONENT) color1.toInt().toDouble() else color1.toDouble()
        when (type1) {
            PARSE_ALPHA -> return if (c < 0.0) 0.0 else if (c > 1.0) 1.0 else c
            PARSE_PERCENT -> return if (c <= 0.0) 0.0 else if (c >= 100.0) 1.0 else c / 100.0
            PARSE_COMPONENT -> return if (c <= 0.0) 0.0 else if (c >= 255.0) 1.0 else c / 255.0
            PARSE_ANGLE -> return if (c < 0.0) c % 360.0 + 360.0 else if (c > 360.0) c % 360.0 else c
        }
        throw IllegalArgumentException("Invalid color specification")
    }

    /**
     * Creates a color value from a string representation. The format
     * of the string representation is the same as in [.web].
     *
     * @param value the string to convert
     *
     *
     * an unsupported color name or illegal hexadecimal value
     * @return a `Color` object holding the value represented
     * by the string argument
     * @see .web
     */
    fun valueOf(value: String?): Color {
        if (value == null) {
            throw NullPointerException("color must be specified")
        }
        return web(value)
    }

    private fun to32BitInteger(red: Int, green: Int, blue: Int, alpha: Int): Int {
        var i = red
        i = i shl 8
        i = i or green
        i = i shl 8
        i = i or blue
        i = i shl 8
        i = i or alpha
        return i
    }

    /**
     * A fully transparent color with an ARGB value of #00000000.
     */
    val TRANSPARENT = Color(0f, 0f, 0f, 0f)

    /**
     * The color alice blue with an RGB value of #F0F8FF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F0F8FF;float:right;margin: 0 10px 0 0"></div>
     */
    val ALICEBLUE = Color(0.9411765f, 0.972549f, 1.0f)

    /**
     * The color antique white with an RGB value of #FAEBD7
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FAEBD7;float:right;margin: 0 10px 0 0"></div>
     */
    val ANTIQUEWHITE = Color(0.98039216f, 0.92156863f, 0.84313726f)

    /**
     * The color aqua with an RGB value of #00FFFF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#00FFFF;float:right;margin: 0 10px 0 0"></div>
     */
    val AQUA = Color(0.0f, 1.0f, 1.0f)

    /**
     * The color aquamarine with an RGB value of #7FFFD4
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#7FFFD4;float:right;margin: 0 10px 0 0"></div>
     */
    val AQUAMARINE = Color(0.49803922f, 1.0f, 0.83137256f)

    /**
     * The color azure with an RGB value of #F0FFFF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F0FFFF;float:right;margin: 0 10px 0 0"></div>
     */
    val AZURE = Color(0.9411765f, 1.0f, 1.0f)

    /**
     * The color beige with an RGB value of #F5F5DC
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F5F5DC;float:right;margin: 0 10px 0 0"></div>
     */
    val BEIGE = Color(0.9607843f, 0.9607843f, 0.8627451f)

    /**
     * The color bisque with an RGB value of #FFE4C4
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFE4C4;float:right;margin: 0 10px 0 0"></div>
     */
    val BISQUE = Color(1.0f, 0.89411765f, 0.76862746f)

    /**
     * The color black with an RGB value of #000000
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#000000;float:right;margin: 0 10px 0 0"></div>
     */
    val BLACK = Color(0.0f, 0.0f, 0.0f)

    /**
     * The color blanched almond with an RGB value of #FFEBCD
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFEBCD;float:right;margin: 0 10px 0 0"></div>
     */
    val BLANCHEDALMOND = Color(1.0f, 0.92156863f, 0.8039216f)

    /**
     * The color blue with an RGB value of #0000FF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#0000FF;float:right;margin: 0 10px 0 0"></div>
     */
    val BLUE = Color(0.0f, 0.0f, 1.0f)

    /**
     * The color blue violet with an RGB value of #8A2BE2
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#8A2BE2;float:right;margin: 0 10px 0 0"></div>
     */
    val BLUEVIOLET = Color(0.5411765f, 0.16862746f, 0.8862745f)

    /**
     * The color brown with an RGB value of #A52A2A
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#A52A2A;float:right;margin: 0 10px 0 0"></div>
     */
    val BROWN = Color(0.64705884f, 0.16470589f, 0.16470589f)

    /**
     * The color burly wood with an RGB value of #DEB887
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#DEB887;float:right;margin: 0 10px 0 0"></div>
     */
    val BURLYWOOD = Color(0.87058824f, 0.72156864f, 0.5294118f)

    /**
     * The color cadet blue with an RGB value of #5F9EA0
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#5F9EA0;float:right;margin: 0 10px 0 0"></div>
     */
    val CADETBLUE = Color(0.37254903f, 0.61960787f, 0.627451f)

    /**
     * The color chartreuse with an RGB value of #7FFF00
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#7FFF00;float:right;margin: 0 10px 0 0"></div>
     */
    val CHARTREUSE = Color(0.49803922f, 1.0f, 0.0f)

    /**
     * The color chocolate with an RGB value of #D2691E
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#D2691E;float:right;margin: 0 10px 0 0"></div>
     */
    val CHOCOLATE = Color(0.8235294f, 0.4117647f, 0.11764706f)

    /**
     * The color coral with an RGB value of #FF7F50
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF7F50;float:right;margin: 0 10px 0 0"></div>
     */
    val CORAL = Color(1.0f, 0.49803922f, 0.3137255f)

    /**
     * The color cornflower blue with an RGB value of #6495ED
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#6495ED;float:right;margin: 0 10px 0 0"></div>
     */
    val CORNFLOWERBLUE = Color(0.39215687f, 0.58431375f, 0.92941177f)

    /**
     * The color cornsilk with an RGB value of #FFF8DC
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFF8DC;float:right;margin: 0 10px 0 0"></div>
     */
    val CORNSILK = Color(1.0f, 0.972549f, 0.8627451f)

    /**
     * The color crimson with an RGB value of #DC143C
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#DC143C;float:right;margin: 0 10px 0 0"></div>
     */
    val CRIMSON = Color(0.8627451f, 0.078431375f, 0.23529412f)

    /**
     * The color cyan with an RGB value of #00FFFF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#00FFFF;float:right;margin: 0 10px 0 0"></div>
     */
    val CYAN = Color(0.0f, 1.0f, 1.0f)

    /**
     * The color dark blue with an RGB value of #00008B
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#00008B;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKBLUE = Color(0.0f, 0.0f, 0.54509807f)

    /**
     * The color dark cyan with an RGB value of #008B8B
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#008B8B;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKCYAN = Color(0.0f, 0.54509807f, 0.54509807f)

    /**
     * The color dark goldenrod with an RGB value of #B8860B
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#B8860B;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKGOLDENROD = Color(0.72156864f, 0.5254902f, 0.043137256f)

    /**
     * The color dark gray with an RGB value of #A9A9A9
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#A9A9A9;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKGRAY = Color(0.6627451f, 0.6627451f, 0.6627451f)

    /**
     * The color dark green with an RGB value of #006400
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#006400;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKGREEN = Color(0.0f, 0.39215687f, 0.0f)

    /**
     * The color dark grey with an RGB value of #A9A9A9
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#A9A9A9;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKGREY = DARKGRAY

    /**
     * The color dark khaki with an RGB value of #BDB76B
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#BDB76B;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKKHAKI = Color(0.7411765f, 0.7176471f, 0.41960785f)

    /**
     * The color dark magenta with an RGB value of #8B008B
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#8B008B;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKMAGENTA = Color(0.54509807f, 0.0f, 0.54509807f)

    /**
     * The color dark olive green with an RGB value of #556B2F
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#556B2F;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKOLIVEGREEN = Color(0.33333334f, 0.41960785f, 0.18431373f)

    /**
     * The color dark orange with an RGB value of #FF8C00
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF8C00;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKORANGE = Color(1.0f, 0.54901963f, 0.0f)

    /**
     * The color dark orchid with an RGB value of #9932CC
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#9932CC;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKORCHID = Color(0.6f, 0.19607843f, 0.8f)

    /**
     * The color dark red with an RGB value of #8B0000
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#8B0000;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKRED = Color(0.54509807f, 0.0f, 0.0f)

    /**
     * The color dark salmon with an RGB value of #E9967A
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#E9967A;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKSALMON = Color(0.9137255f, 0.5882353f, 0.47843137f)

    /**
     * The color dark sea green with an RGB value of #8FBC8F
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#8FBC8F;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKSEAGREEN = Color(0.56078434f, 0.7372549f, 0.56078434f)

    /**
     * The color dark slate blue with an RGB value of #483D8B
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#483D8B;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKSLATEBLUE = Color(0.28235295f, 0.23921569f, 0.54509807f)

    /**
     * The color dark slate gray with an RGB value of #2F4F4F
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#2F4F4F;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKSLATEGRAY = Color(0.18431373f, 0.30980393f, 0.30980393f)

    /**
     * The color dark slate grey with an RGB value of #2F4F4F
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#2F4F4F;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKSLATEGREY = DARKSLATEGRAY

    /**
     * The color dark turquoise with an RGB value of #00CED1
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#00CED1;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKTURQUOISE = Color(0.0f, 0.80784315f, 0.81960785f)

    /**
     * The color dark violet with an RGB value of #9400D3
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#9400D3;float:right;margin: 0 10px 0 0"></div>
     */
    val DARKVIOLET = Color(0.5803922f, 0.0f, 0.827451f)

    /**
     * The color deep pink with an RGB value of #FF1493
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF1493;float:right;margin: 0 10px 0 0"></div>
     */
    val DEEPPINK = Color(1.0f, 0.078431375f, 0.5764706f)

    /**
     * The color deep sky blue with an RGB value of #00BFFF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#00BFFF;float:right;margin: 0 10px 0 0"></div>
     */
    val DEEPSKYBLUE = Color(0.0f, 0.7490196f, 1.0f)

    /**
     * The color dim gray with an RGB value of #696969
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#696969;float:right;margin: 0 10px 0 0"></div>
     */
    val DIMGRAY = Color(0.4117647f, 0.4117647f, 0.4117647f)

    /**
     * The color dim grey with an RGB value of #696969
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#696969;float:right;margin: 0 10px 0 0"></div>
     */
    val DIMGREY = DIMGRAY

    /**
     * The color dodger blue with an RGB value of #1E90FF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#1E90FF;float:right;margin: 0 10px 0 0"></div>
     */
    val DODGERBLUE = Color(0.11764706f, 0.5647059f, 1.0f)

    /**
     * The color firebrick with an RGB value of #B22222
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#B22222;float:right;margin: 0 10px 0 0"></div>
     */
    val FIREBRICK = Color(0.69803923f, 0.13333334f, 0.13333334f)

    /**
     * The color floral white with an RGB value of #FFFAF0
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFAF0;float:right;margin: 0 10px 0 0"></div>
     */
    val FLORALWHITE = Color(1.0f, 0.98039216f, 0.9411765f)

    /**
     * The color forest green with an RGB value of #228B22
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#228B22;float:right;margin: 0 10px 0 0"></div>
     */
    val FORESTGREEN = Color(0.13333334f, 0.54509807f, 0.13333334f)

    /**
     * The color fuchsia with an RGB value of #FF00FF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF00FF;float:right;margin: 0 10px 0 0"></div>
     */
    val FUCHSIA = Color(1.0f, 0.0f, 1.0f)

    /**
     * The color gainsboro with an RGB value of #DCDCDC
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#DCDCDC;float:right;margin: 0 10px 0 0"></div>
     */
    val GAINSBORO = Color(0.8627451f, 0.8627451f, 0.8627451f)

    /**
     * The color ghost white with an RGB value of #F8F8FF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F8F8FF;float:right;margin: 0 10px 0 0"></div>
     */
    val GHOSTWHITE = Color(0.972549f, 0.972549f, 1.0f)

    /**
     * The color gold with an RGB value of #FFD700
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFD700;float:right;margin: 0 10px 0 0"></div>
     */
    val GOLD = Color(1.0f, 0.84313726f, 0.0f)

    /**
     * The color goldenrod with an RGB value of #DAA520
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#DAA520;float:right;margin: 0 10px 0 0"></div>
     */
    val GOLDENROD = Color(0.85490197f, 0.64705884f, 0.1254902f)

    /**
     * The color gray with an RGB value of #808080
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#808080;float:right;margin: 0 10px 0 0"></div>
     */
    val GRAY = Color(0.5019608f, 0.5019608f, 0.5019608f)

    /**
     * The color green with an RGB value of #008000
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#008000;float:right;margin: 0 10px 0 0"></div>
     */
    val GREEN = Color(0.0f, 0.5019608f, 0.0f)

    /**
     * The color green yellow with an RGB value of #ADFF2F
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#ADFF2F;float:right;margin: 0 10px 0 0"></div>
     */
    val GREENYELLOW = Color(0.6784314f, 1.0f, 0.18431373f)

    /**
     * The color grey with an RGB value of #808080
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#808080;float:right;margin: 0 10px 0 0"></div>
     */
    val GREY = GRAY

    /**
     * The color honeydew with an RGB value of #F0FFF0
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F0FFF0;float:right;margin: 0 10px 0 0"></div>
     */
    val HONEYDEW = Color(0.9411765f, 1.0f, 0.9411765f)

    /**
     * The color hot pink with an RGB value of #FF69B4
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF69B4;float:right;margin: 0 10px 0 0"></div>
     */
    val HOTPINK = Color(1.0f, 0.4117647f, 0.7058824f)

    /**
     * The color indian red with an RGB value of #CD5C5C
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#CD5C5C;float:right;margin: 0 10px 0 0"></div>
     */
    val INDIANRED = Color(0.8039216f, 0.36078432f, 0.36078432f)

    /**
     * The color indigo with an RGB value of #4B0082
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#4B0082;float:right;margin: 0 10px 0 0"></div>
     */
    val INDIGO = Color(0.29411766f, 0.0f, 0.50980395f)

    /**
     * The color ivory with an RGB value of #FFFFF0
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFFF0;float:right;margin: 0 10px 0 0"></div>
     */
    val IVORY = Color(1.0f, 1.0f, 0.9411765f)

    /**
     * The color khaki with an RGB value of #F0E68C
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F0E68C;float:right;margin: 0 10px 0 0"></div>
     */
    val KHAKI = Color(0.9411765f, 0.9019608f, 0.54901963f)

    /**
     * The color lavender with an RGB value of #E6E6FA
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#E6E6FA;float:right;margin: 0 10px 0 0"></div>
     */
    val LAVENDER = Color(0.9019608f, 0.9019608f, 0.98039216f)

    /**
     * The color lavender blush with an RGB value of #FFF0F5
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFF0F5;float:right;margin: 0 10px 0 0"></div>
     */
    val LAVENDERBLUSH = Color(1.0f, 0.9411765f, 0.9607843f)

    /**
     * The color lawn green with an RGB value of #7CFC00
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#7CFC00;float:right;margin: 0 10px 0 0"></div>
     */
    val LAWNGREEN = Color(0.4862745f, 0.9882353f, 0.0f)

    /**
     * The color lemon chiffon with an RGB value of #FFFACD
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFACD;float:right;margin: 0 10px 0 0"></div>
     */
    val LEMONCHIFFON = Color(1.0f, 0.98039216f, 0.8039216f)

    /**
     * The color light blue with an RGB value of #ADD8E6
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#ADD8E6;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTBLUE = Color(0.6784314f, 0.84705883f, 0.9019608f)

    /**
     * The color light coral with an RGB value of #F08080
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F08080;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTCORAL = Color(0.9411765f, 0.5019608f, 0.5019608f)

    /**
     * The color light cyan with an RGB value of #E0FFFF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#E0FFFF;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTCYAN = Color(0.8784314f, 1.0f, 1.0f)

    /**
     * The color light goldenrod yellow with an RGB value of #FAFAD2
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FAFAD2;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTGOLDENRODYELLOW = Color(0.98039216f, 0.98039216f, 0.8235294f)

    /**
     * The color light gray with an RGB value of #D3D3D3
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#D3D3D3;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTGRAY = Color(0.827451f, 0.827451f, 0.827451f)

    /**
     * The color light green with an RGB value of #90EE90
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#90EE90;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTGREEN = Color(0.5647059f, 0.93333334f, 0.5647059f)

    /**
     * The color light grey with an RGB value of #D3D3D3
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#D3D3D3;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTGREY = LIGHTGRAY

    /**
     * The color light pink with an RGB value of #FFB6C1
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFB6C1;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTPINK = Color(1.0f, 0.7137255f, 0.75686276f)

    /**
     * The color light salmon with an RGB value of #FFA07A
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFA07A;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTSALMON = Color(1.0f, 0.627451f, 0.47843137f)

    /**
     * The color light sea green with an RGB value of #20B2AA
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#20B2AA;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTSEAGREEN = Color(0.1254902f, 0.69803923f, 0.6666667f)

    /**
     * The color light sky blue with an RGB value of #87CEFA
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#87CEFA;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTSKYBLUE = Color(0.5294118f, 0.80784315f, 0.98039216f)

    /**
     * The color light slate gray with an RGB value of #778899
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#778899;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTSLATEGRAY = Color(0.46666667f, 0.53333336f, 0.6f)

    /**
     * The color light slate grey with an RGB value of #778899
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#778899;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTSLATEGREY = LIGHTSLATEGRAY

    /**
     * The color light steel blue with an RGB value of #B0C4DE
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#B0C4DE;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTSTEELBLUE = Color(0.6901961f, 0.76862746f, 0.87058824f)

    /**
     * The color light yellow with an RGB value of #FFFFE0
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFFE0;float:right;margin: 0 10px 0 0"></div>
     */
    val LIGHTYELLOW = Color(1.0f, 1.0f, 0.8784314f)

    /**
     * The color lime with an RGB value of #00FF00
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#00FF00;float:right;margin: 0 10px 0 0"></div>
     */
    val LIME = Color(0.0f, 1.0f, 0.0f)

    /**
     * The color lime green with an RGB value of #32CD32
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#32CD32;float:right;margin: 0 10px 0 0"></div>
     */
    val LIMEGREEN = Color(0.19607843f, 0.8039216f, 0.19607843f)

    /**
     * The color linen with an RGB value of #FAF0E6
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FAF0E6;float:right;margin: 0 10px 0 0"></div>
     */
    val LINEN = Color(0.98039216f, 0.9411765f, 0.9019608f)

    /**
     * The color magenta with an RGB value of #FF00FF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF00FF;float:right;margin: 0 10px 0 0"></div>
     */
    val MAGENTA = Color(1.0f, 0.0f, 1.0f)

    /**
     * The color maroon with an RGB value of #800000
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#800000;float:right;margin: 0 10px 0 0"></div>
     */
    val MAROON = Color(0.5019608f, 0.0f, 0.0f)

    /**
     * The color medium aquamarine with an RGB value of #66CDAA
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#66CDAA;float:right;margin: 0 10px 0 0"></div>
     */
    val MEDIUMAQUAMARINE = Color(0.4f, 0.8039216f, 0.6666667f)

    /**
     * The color medium blue with an RGB value of #0000CD
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#0000CD;float:right;margin: 0 10px 0 0"></div>
     */
    val MEDIUMBLUE = Color(0.0f, 0.0f, 0.8039216f)

    /**
     * The color medium orchid with an RGB value of #BA55D3
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#BA55D3;float:right;margin: 0 10px 0 0"></div>
     */
    val MEDIUMORCHID = Color(0.7294118f, 0.33333334f, 0.827451f)

    /**
     * The color medium purple with an RGB value of #9370DB
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#9370DB;float:right;margin: 0 10px 0 0"></div>
     */
    val MEDIUMPURPLE = Color(0.5764706f, 0.4392157f, 0.85882354f)

    /**
     * The color medium sea green with an RGB value of #3CB371
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#3CB371;float:right;margin: 0 10px 0 0"></div>
     */
    val MEDIUMSEAGREEN = Color(0.23529412f, 0.7019608f, 0.44313726f)

    /**
     * The color medium slate blue with an RGB value of #7B68EE
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#7B68EE;float:right;margin: 0 10px 0 0"></div>
     */
    val MEDIUMSLATEBLUE = Color(0.48235294f, 0.40784314f, 0.93333334f)

    /**
     * The color medium spring green with an RGB value of #00FA9A
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#00FA9A;float:right;margin: 0 10px 0 0"></div>
     */
    val MEDIUMSPRINGGREEN = Color(0.0f, 0.98039216f, 0.6039216f)

    /**
     * The color medium turquoise with an RGB value of #48D1CC
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#48D1CC;float:right;margin: 0 10px 0 0"></div>
     */
    val MEDIUMTURQUOISE = Color(0.28235295f, 0.81960785f, 0.8f)

    /**
     * The color medium violet red with an RGB value of #C71585
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#C71585;float:right;margin: 0 10px 0 0"></div>
     */
    val MEDIUMVIOLETRED = Color(0.78039217f, 0.08235294f, 0.52156866f)

    /**
     * The color midnight blue with an RGB value of #191970
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#191970;float:right;margin: 0 10px 0 0"></div>
     */
    val MIDNIGHTBLUE = Color(0.09803922f, 0.09803922f, 0.4392157f)

    /**
     * The color mint cream with an RGB value of #F5FFFA
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F5FFFA;float:right;margin: 0 10px 0 0"></div>
     */
    val MINTCREAM = Color(0.9607843f, 1.0f, 0.98039216f)

    /**
     * The color misty rose with an RGB value of #FFE4E1
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFE4E1;float:right;margin: 0 10px 0 0"></div>
     */
    val MISTYROSE = Color(1.0f, 0.89411765f, 0.88235295f)

    /**
     * The color moccasin with an RGB value of #FFE4B5
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFE4B5;float:right;margin: 0 10px 0 0"></div>
     */
    val MOCCASIN = Color(1.0f, 0.89411765f, 0.70980394f)

    /**
     * The color navajo white with an RGB value of #FFDEAD
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFDEAD;float:right;margin: 0 10px 0 0"></div>
     */
    val NAVAJOWHITE = Color(1.0f, 0.87058824f, 0.6784314f)

    /**
     * The color navy with an RGB value of #000080
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#000080;float:right;margin: 0 10px 0 0"></div>
     */
    val NAVY = Color(0.0f, 0.0f, 0.5019608f)

    /**
     * The color old lace with an RGB value of #FDF5E6
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FDF5E6;float:right;margin: 0 10px 0 0"></div>
     */
    val OLDLACE = Color(0.99215686f, 0.9607843f, 0.9019608f)

    /**
     * The color olive with an RGB value of #808000
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#808000;float:right;margin: 0 10px 0 0"></div>
     */
    val OLIVE = Color(0.5019608f, 0.5019608f, 0.0f)

    /**
     * The color olive drab with an RGB value of #6B8E23
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#6B8E23;float:right;margin: 0 10px 0 0"></div>
     */
    val OLIVEDRAB = Color(0.41960785f, 0.5568628f, 0.13725491f)

    /**
     * The color orange with an RGB value of #FFA500
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFA500;float:right;margin: 0 10px 0 0"></div>
     */
    val ORANGE = Color(1.0f, 0.64705884f, 0.0f)

    /**
     * The color orange red with an RGB value of #FF4500
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF4500;float:right;margin: 0 10px 0 0"></div>
     */
    val ORANGERED = Color(1.0f, 0.27058825f, 0.0f)

    /**
     * The color orchid with an RGB value of #DA70D6
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#DA70D6;float:right;margin: 0 10px 0 0"></div>
     */
    val ORCHID = Color(0.85490197f, 0.4392157f, 0.8392157f)

    /**
     * The color pale goldenrod with an RGB value of #EEE8AA
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#EEE8AA;float:right;margin: 0 10px 0 0"></div>
     */
    val PALEGOLDENROD = Color(0.93333334f, 0.9098039f, 0.6666667f)

    /**
     * The color pale green with an RGB value of #98FB98
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#98FB98;float:right;margin: 0 10px 0 0"></div>
     */
    val PALEGREEN = Color(0.59607846f, 0.9843137f, 0.59607846f)

    /**
     * The color pale turquoise with an RGB value of #AFEEEE
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#AFEEEE;float:right;margin: 0 10px 0 0"></div>
     */
    val PALETURQUOISE = Color(0.6862745f, 0.93333334f, 0.93333334f)

    /**
     * The color pale violet red with an RGB value of #DB7093
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#DB7093;float:right;margin: 0 10px 0 0"></div>
     */
    val PALEVIOLETRED = Color(0.85882354f, 0.4392157f, 0.5764706f)

    /**
     * The color papaya whip with an RGB value of #FFEFD5
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFEFD5;float:right;margin: 0 10px 0 0"></div>
     */
    val PAPAYAWHIP = Color(1.0f, 0.9372549f, 0.8352941f)

    /**
     * The color peach puff with an RGB value of #FFDAB9
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFDAB9;float:right;margin: 0 10px 0 0"></div>
     */
    val PEACHPUFF = Color(1.0f, 0.85490197f, 0.7254902f)

    /**
     * The color peru with an RGB value of #CD853F
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#CD853F;float:right;margin: 0 10px 0 0"></div>
     */
    val PERU = Color(0.8039216f, 0.52156866f, 0.24705882f)

    /**
     * The color pink with an RGB value of #FFC0CB
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFC0CB;float:right;margin: 0 10px 0 0"></div>
     */
    val PINK = Color(1.0f, 0.7529412f, 0.79607844f)

    /**
     * The color plum with an RGB value of #DDA0DD
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#DDA0DD;float:right;margin: 0 10px 0 0"></div>
     */
    val PLUM = Color(0.8666667f, 0.627451f, 0.8666667f)

    /**
     * The color powder blue with an RGB value of #B0E0E6
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#B0E0E6;float:right;margin: 0 10px 0 0"></div>
     */
    val POWDERBLUE = Color(0.6901961f, 0.8784314f, 0.9019608f)

    /**
     * The color purple with an RGB value of #800080
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#800080;float:right;margin: 0 10px 0 0"></div>
     */
    val PURPLE = Color(0.5019608f, 0.0f, 0.5019608f)

    /**
     * The color red with an RGB value of #FF0000
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF0000;float:right;margin: 0 10px 0 0"></div>
     */
    val RED = Color(1.0f, 0.0f, 0.0f)

    /**
     * The color rosy brown with an RGB value of #BC8F8F
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#BC8F8F;float:right;margin: 0 10px 0 0"></div>
     */
    val ROSYBROWN = Color(0.7372549f, 0.56078434f, 0.56078434f)

    /**
     * The color royal blue with an RGB value of #4169E1
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#4169E1;float:right;margin: 0 10px 0 0"></div>
     */
    val ROYALBLUE = Color(0.25490198f, 0.4117647f, 0.88235295f)

    /**
     * The color saddle brown with an RGB value of #8B4513
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#8B4513;float:right;margin: 0 10px 0 0"></div>
     */
    val SADDLEBROWN = Color(0.54509807f, 0.27058825f, 0.07450981f)

    /**
     * The color salmon with an RGB value of #FA8072
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FA8072;float:right;margin: 0 10px 0 0"></div>
     */
    val SALMON = Color(0.98039216f, 0.5019608f, 0.44705883f)

    /**
     * The color sandy brown with an RGB value of #F4A460
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F4A460;float:right;margin: 0 10px 0 0"></div>
     */
    val SANDYBROWN = Color(0.95686275f, 0.6431373f, 0.3764706f)

    /**
     * The color sea green with an RGB value of #2E8B57
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#2E8B57;float:right;margin: 0 10px 0 0"></div>
     */
    val SEAGREEN = Color(0.18039216f, 0.54509807f, 0.34117648f)

    /**
     * The color sea shell with an RGB value of #FFF5EE
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFF5EE;float:right;margin: 0 10px 0 0"></div>
     */
    val SEASHELL = Color(1.0f, 0.9607843f, 0.93333334f)

    /**
     * The color sienna with an RGB value of #A0522D
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#A0522D;float:right;margin: 0 10px 0 0"></div>
     */
    val SIENNA = Color(0.627451f, 0.32156864f, 0.1764706f)

    /**
     * The color silver with an RGB value of #C0C0C0
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#C0C0C0;float:right;margin: 0 10px 0 0"></div>
     */
    val SILVER = Color(0.7529412f, 0.7529412f, 0.7529412f)

    /**
     * The color sky blue with an RGB value of #87CEEB
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#87CEEB;float:right;margin: 0 10px 0 0"></div>
     */
    val SKYBLUE = Color(0.5294118f, 0.80784315f, 0.92156863f)

    /**
     * The color slate blue with an RGB value of #6A5ACD
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#6A5ACD;float:right;margin: 0 10px 0 0"></div>
     */
    val SLATEBLUE = Color(0.41568628f, 0.3529412f, 0.8039216f)

    /**
     * The color slate gray with an RGB value of #708090
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#708090;float:right;margin: 0 10px 0 0"></div>
     */
    val SLATEGRAY = Color(0.4392157f, 0.5019608f, 0.5647059f)

    /**
     * The color slate grey with an RGB value of #708090
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#708090;float:right;margin: 0 10px 0 0"></div>
     */
    val SLATEGREY = SLATEGRAY

    /**
     * The color snow with an RGB value of #FFFAFA
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFAFA;float:right;margin: 0 10px 0 0"></div>
     */
    val SNOW = Color(1.0f, 0.98039216f, 0.98039216f)

    /**
     * The color spring green with an RGB value of #00FF7F
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#00FF7F;float:right;margin: 0 10px 0 0"></div>
     */
    val SPRINGGREEN = Color(0.0f, 1.0f, 0.49803922f)

    /**
     * The color steel blue with an RGB value of #4682B4
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#4682B4;float:right;margin: 0 10px 0 0"></div>
     */
    val STEELBLUE = Color(0.27450982f, 0.50980395f, 0.7058824f)

    /**
     * The color tan with an RGB value of #D2B48C
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#D2B48C;float:right;margin: 0 10px 0 0"></div>
     */
    val TAN = Color(0.8235294f, 0.7058824f, 0.54901963f)

    /**
     * The color teal with an RGB value of #008080
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#008080;float:right;margin: 0 10px 0 0"></div>
     */
    val TEAL = Color(0.0f, 0.5019608f, 0.5019608f)

    /**
     * The color thistle with an RGB value of #D8BFD8
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#D8BFD8;float:right;margin: 0 10px 0 0"></div>
     */
    val THISTLE = Color(0.84705883f, 0.7490196f, 0.84705883f)

    /**
     * The color tomato with an RGB value of #FF6347
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FF6347;float:right;margin: 0 10px 0 0"></div>
     */
    val TOMATO = Color(1.0f, 0.3882353f, 0.2784314f)

    /**
     * The color turquoise with an RGB value of #40E0D0
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#40E0D0;float:right;margin: 0 10px 0 0"></div>
     */
    val TURQUOISE = Color(0.2509804f, 0.8784314f, 0.8156863f)

    /**
     * The color violet with an RGB value of #EE82EE
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#EE82EE;float:right;margin: 0 10px 0 0"></div>
     */
    val VIOLET = Color(0.93333334f, 0.50980395f, 0.93333334f)

    /**
     * The color wheat with an RGB value of #F5DEB3
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F5DEB3;float:right;margin: 0 10px 0 0"></div>
     */
    val WHEAT = Color(0.9607843f, 0.87058824f, 0.7019608f)

    /**
     * The color white with an RGB value of #FFFFFF
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFFFF;float:right;margin: 0 10px 0 0"></div>
     */
    val WHITE = Color(1.0f, 1.0f, 1.0f)

    /**
     * The color white smoke with an RGB value of #F5F5F5
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#F5F5F5;float:right;margin: 0 10px 0 0"></div>
     */
    val WHITESMOKE = Color(0.9607843f, 0.9607843f, 0.9607843f)

    /**
     * The color yellow with an RGB value of #FFFF00
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#FFFF00;float:right;margin: 0 10px 0 0"></div>
     */
    val YELLOW = Color(1.0f, 1.0f, 0.0f)

    /**
     * The color yellow green with an RGB value of #9ACD32
     * <div style="border:1px solid black;width:40px;height:20px;background-color:#9ACD32;float:right;margin: 0 10px 0 0"></div>
     */
    val YELLOWGREEN = Color(0.6039216f, 0.8039216f, 0.19607843f)
    fun HSBtoRGB(hue: Double, saturation: Double, brightness: Double): DoubleArray {
        // normalize the hue
        var hue1 = hue
        val normalizedHue = (hue1 % 360 + 360) % 360
        hue1 = normalizedHue / 360
        var r = 0.0
        var g = 0.0
        var b = 0.0
        if (saturation == 0.0) {
            b = brightness
            g = b
            r = g
        } else {
            val h = (hue1 - floor(hue1)) * 6.0
            val f = h - floor(h)
            val p = brightness * (1.0 - saturation)
            val q = brightness * (1.0 - saturation * f)
            val t = brightness * (1.0 - saturation * (1.0 - f))
            when (h.toInt()) {
                0 -> {
                    r = brightness
                    g = t
                    b = p
                }

                1 -> {
                    r = q
                    g = brightness
                    b = p
                }

                2 -> {
                    r = p
                    g = brightness
                    b = t
                }

                3 -> {
                    r = p
                    g = q
                    b = brightness
                }

                4 -> {
                    r = t
                    g = p
                    b = brightness
                }

                5 -> {
                    r = brightness
                    g = p
                    b = q
                }
            }
        }
        val f = DoubleArray(3)
        f[0] = r
        f[1] = g
        f[2] = b
        return f
    }

    fun RGBtoHSB(r: Double, g: Double, b: Double): DoubleArray {
        var hue: Double
        val saturation: Double
        val brightness: Double
        val hsbvals = DoubleArray(3)
        var cmax = if (r > g) r else g
        if (b > cmax) cmax = b
        var cmin = if (r < g) r else g
        if (b < cmin) cmin = b
        brightness = cmax
        saturation = if (cmax != 0.0) (cmax - cmin) / cmax else 0.0
        if (saturation == 0.0) {
            hue = 0.0
        } else {
            val redc = (cmax - r) / (cmax - cmin)
            val greenc = (cmax - g) / (cmax - cmin)
            val bluec = (cmax - b) / (cmax - cmin)
            hue = if (r == cmax) bluec - greenc else if (g == cmax) 2.0 + redc - bluec else 4.0 + greenc - redc
            hue /= 6.0
            if (hue < 0) hue += 1.0
        }
        hsbvals[0] = hue * 360
        hsbvals[1] = saturation
        hsbvals[2] = brightness
        return hsbvals
    }

    /*
     * Named colors moved to nested class to initialize them only when they
     * are needed.
     */
    private object NamedColors {
        private val namedColors = createNamedColors()
        operator fun get(name: String): Color? {
            return namedColors[name]
        }

        private fun createNamedColors(): Map<String, Color> {
            val colors: MutableMap<String, Color> = HashMap(256)
            colors["aliceblue"] = ALICEBLUE
            colors["antiquewhite"] = ANTIQUEWHITE
            colors["aqua"] = AQUA
            colors["aquamarine"] = AQUAMARINE
            colors["azure"] = AZURE
            colors["beige"] = BEIGE
            colors["bisque"] = BISQUE
            colors["black"] = BLACK
            colors["blanchedalmond"] = BLANCHEDALMOND
            colors["blue"] = BLUE
            colors["blueviolet"] = BLUEVIOLET
            colors["brown"] = BROWN
            colors["burlywood"] = BURLYWOOD
            colors["cadetblue"] = CADETBLUE
            colors["chartreuse"] = CHARTREUSE
            colors["chocolate"] = CHOCOLATE
            colors["coral"] = CORAL
            colors["cornflowerblue"] = CORNFLOWERBLUE
            colors["cornsilk"] = CORNSILK
            colors["crimson"] = CRIMSON
            colors["cyan"] = CYAN
            colors["darkblue"] = DARKBLUE
            colors["darkcyan"] = DARKCYAN
            colors["darkgoldenrod"] = DARKGOLDENROD
            colors["darkgray"] = DARKGRAY
            colors["darkgreen"] = DARKGREEN
            colors["darkgrey"] = DARKGREY
            colors["darkkhaki"] = DARKKHAKI
            colors["darkmagenta"] = DARKMAGENTA
            colors["darkolivegreen"] = DARKOLIVEGREEN
            colors["darkorange"] = DARKORANGE
            colors["darkorchid"] = DARKORCHID
            colors["darkred"] = DARKRED
            colors["darksalmon"] = DARKSALMON
            colors["darkseagreen"] = DARKSEAGREEN
            colors["darkslateblue"] = DARKSLATEBLUE
            colors["darkslategray"] = DARKSLATEGRAY
            colors["darkslategrey"] = DARKSLATEGREY
            colors["darkturquoise"] = DARKTURQUOISE
            colors["darkviolet"] = DARKVIOLET
            colors["deeppink"] = DEEPPINK
            colors["deepskyblue"] = DEEPSKYBLUE
            colors["dimgray"] = DIMGRAY
            colors["dimgrey"] = DIMGREY
            colors["dodgerblue"] = DODGERBLUE
            colors["firebrick"] = FIREBRICK
            colors["floralwhite"] = FLORALWHITE
            colors["forestgreen"] = FORESTGREEN
            colors["fuchsia"] = FUCHSIA
            colors["gainsboro"] = GAINSBORO
            colors["ghostwhite"] = GHOSTWHITE
            colors["gold"] = GOLD
            colors["goldenrod"] = GOLDENROD
            colors["gray"] = GRAY
            colors["green"] = GREEN
            colors["greenyellow"] = GREENYELLOW
            colors["grey"] = GREY
            colors["honeydew"] = HONEYDEW
            colors["hotpink"] = HOTPINK
            colors["indianred"] = INDIANRED
            colors["indigo"] = INDIGO
            colors["ivory"] = IVORY
            colors["khaki"] = KHAKI
            colors["lavender"] = LAVENDER
            colors["lavenderblush"] = LAVENDERBLUSH
            colors["lawngreen"] = LAWNGREEN
            colors["lemonchiffon"] = LEMONCHIFFON
            colors["lightblue"] = LIGHTBLUE
            colors["lightcoral"] = LIGHTCORAL
            colors["lightcyan"] = LIGHTCYAN
            colors["lightgoldenrodyellow"] = LIGHTGOLDENRODYELLOW
            colors["lightgray"] = LIGHTGRAY
            colors["lightgreen"] = LIGHTGREEN
            colors["lightgrey"] = LIGHTGREY
            colors["lightpink"] = LIGHTPINK
            colors["lightsalmon"] = LIGHTSALMON
            colors["lightseagreen"] = LIGHTSEAGREEN
            colors["lightskyblue"] = LIGHTSKYBLUE
            colors["lightslategray"] = LIGHTSLATEGRAY
            colors["lightslategrey"] = LIGHTSLATEGREY
            colors["lightsteelblue"] = LIGHTSTEELBLUE
            colors["lightyellow"] = LIGHTYELLOW
            colors["lime"] = LIME
            colors["limegreen"] = LIMEGREEN
            colors["linen"] = LINEN
            colors["magenta"] = MAGENTA
            colors["maroon"] = MAROON
            colors["mediumaquamarine"] = MEDIUMAQUAMARINE
            colors["mediumblue"] = MEDIUMBLUE
            colors["mediumorchid"] = MEDIUMORCHID
            colors["mediumpurple"] = MEDIUMPURPLE
            colors["mediumseagreen"] = MEDIUMSEAGREEN
            colors["mediumslateblue"] = MEDIUMSLATEBLUE
            colors["mediumspringgreen"] = MEDIUMSPRINGGREEN
            colors["mediumturquoise"] = MEDIUMTURQUOISE
            colors["mediumvioletred"] = MEDIUMVIOLETRED
            colors["midnightblue"] = MIDNIGHTBLUE
            colors["mintcream"] = MINTCREAM
            colors["mistyrose"] = MISTYROSE
            colors["moccasin"] = MOCCASIN
            colors["navajowhite"] = NAVAJOWHITE
            colors["navy"] = NAVY
            colors["oldlace"] = OLDLACE
            colors["olive"] = OLIVE
            colors["olivedrab"] = OLIVEDRAB
            colors["orange"] = ORANGE
            colors["orangered"] = ORANGERED
            colors["orchid"] = ORCHID
            colors["palegoldenrod"] = PALEGOLDENROD
            colors["palegreen"] = PALEGREEN
            colors["paleturquoise"] = PALETURQUOISE
            colors["palevioletred"] = PALEVIOLETRED
            colors["papayawhip"] = PAPAYAWHIP
            colors["peachpuff"] = PEACHPUFF
            colors["peru"] = PERU
            colors["pink"] = PINK
            colors["plum"] = PLUM
            colors["powderblue"] = POWDERBLUE
            colors["purple"] = PURPLE
            colors["red"] = RED
            colors["rosybrown"] = ROSYBROWN
            colors["royalblue"] = ROYALBLUE
            colors["saddlebrown"] = SADDLEBROWN
            colors["salmon"] = SALMON
            colors["sandybrown"] = SANDYBROWN
            colors["seagreen"] = SEAGREEN
            colors["seashell"] = SEASHELL
            colors["sienna"] = SIENNA
            colors["silver"] = SILVER
            colors["skyblue"] = SKYBLUE
            colors["slateblue"] = SLATEBLUE
            colors["slategray"] = SLATEGRAY
            colors["slategrey"] = SLATEGREY
            colors["snow"] = SNOW
            colors["springgreen"] = SPRINGGREEN
            colors["steelblue"] = STEELBLUE
            colors["tan"] = TAN
            colors["teal"] = TEAL
            colors["thistle"] = THISTLE
            colors["tomato"] = TOMATO
            colors["transparent"] = TRANSPARENT
            colors["turquoise"] = TURQUOISE
            colors["violet"] = VIOLET
            colors["wheat"] = WHEAT
            colors["white"] = WHITE
            colors["whitesmoke"] = WHITESMOKE
            colors["yellow"] = YELLOW
            colors["yellowgreen"] = YELLOWGREEN
            return colors
        }
    }
}