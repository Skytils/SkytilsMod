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

package gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.handlers

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.DungeonMapParser
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.map.Door
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.map.DoorType
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.map.Unknown
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.utils.MapUtils
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.utils.MapUtils.mapX
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.utils.MapUtils.mapZ
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.utils.MapUtils.yaw
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import kotlin.math.roundToInt

object MapUpdater {
    fun updatePlayers() {
        if (DungeonListener.team.isEmpty()) return

        val decor = MapUtils.getMapData()?.mapDecorations ?: return
        DungeonListener.team.forEach { (name, team) ->
            val player = team.mapPlayer
            decor.entries.find { (icon, _) -> icon == player.icon }?.let { (_, vec4b) ->
                player.isOurMarker = vec4b.func_176110_a().toInt() == 1
                player.mapX = vec4b.mapX
                player.mapZ = vec4b.mapZ
                player.yaw = vec4b.yaw
            }
            if (player.isOurMarker || name == mc.thePlayer.name) {
                player.yaw = mc.thePlayer.rotationYaw
                player.mapX =
                    ((mc.thePlayer.posX - DungeonScanner.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first).roundToInt()
                player.mapZ =
                    ((mc.thePlayer.posZ - DungeonScanner.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second).roundToInt()
            }
        }
    }

    fun updateRooms() {
        val map = DungeonMapParser(MapUtils.getMapData()?.colors ?: return)

        for (x in 0..10) {
            for (z in 0..10) {
                val room = DungeonInfo.dungeonList[z * 11 + x]
                val mapTile = map.getTile(x, z)

                if (room is Unknown) {
                    DungeonInfo.dungeonList[z * 11 + x] = mapTile
                    continue
                }

                if (mapTile.state.ordinal < room.state.ordinal) {
                    room.state = mapTile.state
                }

                if (room is Door && Utils.equalsOneOf(room.type, DoorType.ENTRANCE, DoorType.WITHER, DoorType.BLOOD)) {
                    if (mapTile is Door && mapTile.type == DoorType.WITHER) {
                        room.opened = false
                    } else if (!room.opened && mc.theWorld.getChunkFromChunkCoords(
                            room.x shr 4,
                            room.z shr 4
                        ).isLoaded
                    ) {
                        if (mc.theWorld.getBlockState(BlockPos(room.x, 69, room.z)).block == Blocks.air) {
                            room.opened = true
                        }
                    }
                }
            }
        }
    }
}
