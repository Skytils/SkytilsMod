/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.boss

import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.boss.icons.IconRenderer
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.util.ResourceLocation

class LayeredBossMap(val floor: Int, val x: IntRange, val z: IntRange) {
    val layers = mutableSetOf<Layer>()

    inner class Layer(val y: IntRange, val texture: ResourceLocation) {
        val map get() = this@LayeredBossMap

        val iconRenderers = mutableSetOf<IconRenderer>()
        fun addIconRenderer(iconRenderer: IconRenderer): Layer {
            iconRenderers.add(iconRenderer)
            return this
        }
    }

    fun addLayer(y: IntRange, texture: ResourceLocation): Layer {
        return Layer(y, texture).also { layers.add(it) }
    }

    fun addIconRendererToAll(iconRenderer: IconRenderer) {
        layers.forEach { it.addIconRenderer(iconRenderer) }
    }

    fun getLayer(player: AbstractClientPlayer): Layer? {
        val playerX = player.posX.toInt()
        val playerY = player.posY.toInt()
        val playerZ = player.posZ.toInt()

        return if (playerX in x && playerZ in z) {
            layers.firstOrNull { playerY in it.y }
        } else null
    }
}