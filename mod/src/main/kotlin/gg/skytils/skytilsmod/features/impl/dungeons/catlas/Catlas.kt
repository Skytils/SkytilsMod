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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas

import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.TickEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.render.WorldDrawEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.DungeonPuzzleResetEvent
import gg.skytils.skytilsmod._event.PacketReceiveEvent
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasElement
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.*
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonInfo
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonScanner
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.MapUpdater
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.MimicDetector
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.MapUtils
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.network.play.server.S34PacketMaps
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.storage.MapData

object Catlas : EventSubscriber {

    fun reset() {
        DungeonInfo.reset()
        MapUtils.calibrated = false
        DungeonScanner.hasScanned = false
        MimicDetector.mimicOpenTime = 0
        MimicDetector.mimicPos = null
    }

    override fun setup() {
        register(::onTick)
        register(::onWorldLoad)
        register(::onWorldRender)
        register(::onPuzzleReset)
        register(::onPacket)
    }

    fun onTick(event: TickEvent) {
        if (!Utils.inDungeons || mc.thePlayer == null) return

        if (!MapUtils.calibrated) {
            if (DungeonInfo.dungeonMap == null) {
                DungeonInfo.dungeonMap = MapUtils.getMapData()
            }

            MapUtils.calibrated = MapUtils.calibrateMap()
        } else if (DungeonTimer.scoreShownAt == -1L && DungeonTimer.bossEntryTime == -1L) {
            (DungeonInfo.dungeonMap ?: DungeonInfo.guessMapData)?.let {
                MapUpdater.updateRooms(it)
                MapUpdater.updatePlayers(it)
            }

            if ((DungeonFeatures.dungeonFloorNumber ?: 0) >= 6) {
                MimicDetector.checkMimicDead()
            }
        }

        if (DungeonScanner.shouldScan) {
            DungeonScanner.scan()
        }
    }

    fun onWorldLoad(event: WorldUnloadEvent) {
        reset()
    }

    fun onWorldRender(event: WorldDrawEvent) {
        if (!Utils.inDungeons || DungeonTimer.bossEntryTime != -1L || !CatlasConfig.boxWitherDoors) return

        DungeonInfo.dungeonList.filterIsInstance<Door>().filter {
            it.type != DoorType.NORMAL && it.state == RoomState.DISCOVERED && !it.opened
        }.forEach {
            val matrixStack = UMatrixStack()
            val aabb = AxisAlignedBB(it.x - 1.0, 69.0, it.z - 1.0, it.x + 2.0, 73.0, it.z + 2.0)
            val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)

            val color =
                if (DungeonInfo.keys > 0) CatlasConfig.witherDoorKeyColor else CatlasConfig.witherDoorNoKeyColor

            UGraphics.disableDepth()
            RenderUtil.drawOutlinedBoundingBox(
                aabb,
                color.withAlpha(CatlasConfig.witherDoorOutline),
                CatlasConfig.witherDoorOutlineWidth,
                event.partialTicks
            )
            RenderUtil.drawFilledBoundingBox(
                matrixStack,
                aabb.offset(-viewerX, -viewerY, -viewerZ),
                color,
                CatlasConfig.witherDoorFill
            )
            UGraphics.enableDepth()
        }
    }

    fun onPuzzleReset(event: DungeonPuzzleResetEvent) {
        val mapRoom = DungeonInfo.uniqueRooms.find { room ->
            room.mainRoom.data.type == RoomType.PUZZLE && Puzzle.fromName(room.name)?.tabName == event.puzzle
        }

        mapRoom?.mainRoom?.state = RoomState.DISCOVERED
    }

    fun onPacket(event: PacketReceiveEvent<*>) {
        if (event.packet is S34PacketMaps && Utils.inDungeons && DungeonInfo.dungeonMap == null && mc.theWorld != null) {
            val id = event.packet.mapId
            if (id and 1000 == 0) {
                val guess = mc.theWorld.mapStorage.loadData(MapData::class.java, "map_${id}") as MapData? ?: return
                if (guess.mapDecorations.any { it.value.func_176110_a() == 1.toByte() }) {
                    DungeonInfo.guessMapData = guess
                }
            }
        }
    }

    init {
        CatlasElement

        arrayOf(
            MimicDetector,
        ).forEach(EventSubscriber::setup)
    }
}