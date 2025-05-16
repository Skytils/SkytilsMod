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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.boss.icons

import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.RenderUtils
import gg.skytils.skytilsmod.listeners.DungeonListener
import net.minecraft.client.renderer.GlStateManager
import kotlin.collections.component1
import kotlin.collections.component2

object PlayerIconRenderer : IconRenderer() {
    override fun draw(iconCtx: IconContext) {
        if (!CatlasConfig.bossMapPlayerIcons) return
        DungeonListener.team.forEach { (name, teammate) ->
            if (!teammate.dead || teammate.mapPlayer.isOurMarker && teammate.player != null) {
                GlStateManager.pushMatrix()
                GlStateManager.translate(
                    iconCtx.worldToIconX(teammate.player!!.posX.toInt()),
                    iconCtx.worldToIconY(teammate.player!!.posZ.toInt()),
                    0.0
                )
                RenderUtils.drawPlayerHead(name, teammate.mapPlayer)
                GlStateManager.popMatrix()
            }
        }
    }
}