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

package gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap

import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.CataclysmicMapConfig
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.CataclysmicMapElement
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.map.Door
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.map.DoorType
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.map.RoomState
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.handlers.DungeonInfo
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.handlers.DungeonScanner
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.handlers.MapUpdater
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.utils.MapUtils
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.TabListUtils
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object CataclysmicMap {

    fun reset() {
        DungeonInfo.reset()
        MapUtils.calibrated = false
        DungeonScanner.hasScanned = false
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null) return

        if (!MapUtils.calibrated) {
            if (DungeonInfo.dungeonMap == null) {
                DungeonInfo.dungeonMap = MapUtils.getMapData()
            }

            MapUtils.calibrated = MapUtils.calibrateMap()
        } else if (DungeonTimer.scoreShownAt == -1L && DungeonTimer.bossEntryTime == -1L) {
            DungeonInfo.dungeonMap?.let {
                MapUpdater.updateRooms(it)
                MapUpdater.updatePlayers(it)
            }
        }

        if (DungeonScanner.shouldScan) {
            DungeonScanner.scan()
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        reset()
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Utils.inDungeons || DungeonTimer.bossEntryTime != -1L || !CataclysmicMapConfig.boxWitherDoors) return

        DungeonInfo.dungeonList.filterIsInstance<Door>().filter {
            it.type != DoorType.NORMAL && it.state == RoomState.DISCOVERED && !it.opened
        }.forEach {
            val matrixStack = UMatrixStack()
            val aabb = AxisAlignedBB(it.x - 1.0, 69.0, it.z - 1.0, it.x + 2.0, 73.0, it.z + 2.0)
            val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)

            val color =
                if (DungeonInfo.keys > 0) CataclysmicMapConfig.witherDoorKeyColor else CataclysmicMapConfig.witherDoorNoKeyColor

            UGraphics.disableDepth()
            RenderUtil.drawOutlinedBoundingBox(
                aabb,
                color.withAlpha(CataclysmicMapConfig.witherDoorOutline),
                CataclysmicMapConfig.witherDoorOutlineWidth,
                event.partialTicks
            )
            RenderUtil.drawFilledBoundingBox(
                matrixStack,
                aabb.offset(-viewerX, -viewerY, -viewerZ),
                color,
                CataclysmicMapConfig.witherDoorFill
            )
            UGraphics.enableDepth()
        }
    }

    init {
        CataclysmicMapElement
    }
}