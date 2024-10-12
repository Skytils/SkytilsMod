/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.core

import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonScanner
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.MapUtils
import gg.skytils.skytilsmod.listeners.DungeonListener
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation

data class DungeonMapPlayer(val teammate: DungeonListener.DungeonTeammate, val skin: ResourceLocation) {
    var mapX = 0
    var mapZ = 0
    var yaw = 0f

    /** Has information from player entity been loaded */
    var playerLoaded = false
    var icon = ""
    var renderHat = false
    var uuid = ""
    var isOurMarker = false

    /** Set player data that requires entity to be loaded */
    fun setData(player: EntityPlayer) {
        renderHat = player.isWearing(EnumPlayerModelParts.HAT)
        uuid = player.uniqueID.toString()
        playerLoaded = true
    }

    fun getBlockPos(): BlockPos {
        val playerPos = this.teammate.player?.playerLocation
        if (playerPos != null) return playerPos

        val x = (this.mapX.toFloat() - MapUtils.startCorner.first) / MapUtils.coordMultiplier + DungeonScanner.startX - 15
        val y = 0.0
        val z = (this.mapZ.toFloat() - MapUtils.startCorner.second) / MapUtils.coordMultiplier + DungeonScanner.startZ - 15

        return BlockPos(x, y, z)
    }
}
