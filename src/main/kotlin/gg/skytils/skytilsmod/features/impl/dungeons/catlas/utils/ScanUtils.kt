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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils

import gg.skytils.skytilsmod.Skytils.Companion.json
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.Room
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.RoomData
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonInfo
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonScanner
import gg.skytils.skytilsmod.utils.Utils
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import kotlin.math.roundToInt

object ScanUtils {
    @OptIn(ExperimentalSerializationApi::class)
    val roomList by lazy {
        json.decodeFromStream<Set<RoomData>>(
            mc.resourceManager.getResource(
                ResourceLocation("skytils", "catlas/rooms.json")
            ).inputStream
        )
    }

    fun getRoomData(x: Int, z: Int): RoomData? {
        return getRoomData(getCore(x, z))
    }

    fun getRoomData(hash: Int): RoomData? {
        return roomList.find { hash in it.cores }
    }

    fun getRoomCenter(posX: Int, posZ: Int): Pair<Int, Int> {
        val roomX = ((posX - DungeonScanner.startX) / 32f).roundToInt()
        val roomZ = ((posZ - DungeonScanner.startZ) / 32f).roundToInt()
        return (roomX * 32 + DungeonScanner.startX) to (roomZ * 32 + DungeonScanner.startZ)
    }

    fun getRoomFromPos(pos: BlockPos): Room? {
        val x = ((pos.x - DungeonScanner.startX + 15) shr 5)
        val z = ((pos.z - DungeonScanner.startZ + 15) shr 5)
        val room = DungeonInfo.dungeonList.getOrNull(x * 2 + z * 22)
        return if (room is Room) room else null
    }

    fun getCore(x: Int, z: Int): Int {
        val sb = StringBuilder(150)
        val chunk = mc.theWorld.getChunkFromBlockCoords(BlockPos(x, 0, z))
        for (y in 140 downTo 12) {
            val id = Block.blockRegistry.getIDForObject(chunk.getBlock(BlockPos(x, y, z)))
            if (!Utils.equalsOneOf(id, 5, 54, 146)) {
                sb.append(id)
            }
        }
        return sb.toString().hashCode()
    }
}
