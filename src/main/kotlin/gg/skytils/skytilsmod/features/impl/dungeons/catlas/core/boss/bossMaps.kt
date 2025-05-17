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

import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.boss.icons.*
import net.minecraft.util.ResourceLocation

val BossMaps = hashMapOf(
    7 to LayeredBossMap(
        floor = 7,
        x = -8..134,
        z = -8..147
    ).apply {
        addLayer(209..253, ResourceLocation("catlas:boss/7/1.png"))
        addLayer(160..209, ResourceLocation("catlas:boss/7/2.png"))
        addLayer(103..160, ResourceLocation("catlas:boss/7/3.png")).apply {
            addIconRenderer(TerminalIconRenderer)
        }
        addLayer(51..103, ResourceLocation("catlas:boss/7/4.png"))
        addLayer(0..51, ResourceLocation("catlas:boss/7/5.png"))
        addIconRendererToAll(PlayerIconRenderer)
    }
)