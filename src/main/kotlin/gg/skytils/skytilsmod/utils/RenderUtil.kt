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

import gg.skytils.skytilsmod.Skytils.mc
import net.minecraft.util.math.Box

object RenderUtil {
    fun getViewerPos(partialTicks: Float): Triple<Double, Double, Double> {
        val viewer = mc.cameraEntity!!
        val viewerX = viewer.lastRenderX + (viewer.x - viewer.lastRenderX) * partialTicks
        val viewerY = viewer.lastRenderY + (viewer.y - viewer.lastRenderY) * partialTicks
        val viewerZ = viewer.lastRenderZ + (viewer.z - viewer.lastRenderZ) * partialTicks
        return Triple(viewerX, viewerY, viewerZ)
    }

    fun interpolate(currentValue: Double, lastValue: Double, multiplier: Float): Double {
        return lastValue + (currentValue - lastValue) * multiplier
    }
}

fun Box.expandBlock(): Box =
    expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)