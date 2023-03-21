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
package gg.skytils.skytilsmod.core.structure

import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer

abstract class GuiElement(var name: String, var scale: Float = 1f, var pos: FloatPair = 0f to 0f) {
    constructor(name: String, pos: IntPair = 0 to 0, scale: Float = 1f) : this(
        name,
        scale,
        (pos.first / UResolution.scaledWidth).toFloat() to (pos.second / UResolution.scaledHeight).toFloat()
    )

    abstract fun render()
    abstract fun demoRender()
    abstract val toggled: Boolean

    fun setPos(x: Int, y: Int) {
        val fX = x / sr.scaledWidth.toFloat()
        val fY = y / sr.scaledHeight.toFloat()
        setPos(fX, fY)
    }

    fun setPos(x: Float, y: Float) {
        pos = x to y
    }

    val scaleX: Float
        get() {
            val maxX = UResolution.scaledWidth
            return maxX * pos.first
        }
    val scaleY: Float
        get() {
            val maxY = UResolution.scaledHeight
            return maxY * pos.second
        }
    abstract val height: Int
    abstract val width: Int
    val actualHeight: Float
        get() = height * scale
    val actualWidth: Float
        get() = width * scale

    companion object {
        val sr = UResolution
        val fr by lazy {
            ScreenRenderer.fontRenderer
        }
    }

    init {
        pos = GuiManager.GUIPOSITIONS.getOrDefault(name, pos)
        scale = GuiManager.GUISCALES.getOrDefault(name, scale)
    }
}

typealias FloatPair = Pair<Float, Float>
typealias IntPair = Pair<Int, Int>