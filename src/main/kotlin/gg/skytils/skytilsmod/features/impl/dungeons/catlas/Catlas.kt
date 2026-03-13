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
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.CustomRenderLayers
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.HeightProvider
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.MapUtils
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.ScanUtils
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.listeners.DungeonListener.outboundRoomQueue
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.printDevMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.FilledMapItem
import net.minecraft.item.map.MapDecorationTypes
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket
import net.minecraft.util.math.Box
import net.minecraft.util.shape.VoxelShapes

object Catlas : EventSubscriber {

    fun reset() {
        outboundRoomQueue.also {
            outboundRoomQueue = Channel(UNLIMITED) {
                printDevMessage({ "failed to deliver $it" }, "dungeonws")
            }
            it.cancel()
        }
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
        if (!Utils.inDungeons) return

        val player = mc.player ?: return

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

        if (CatlasConfig.mapShowBeforeStart && DungeonTimer.dungeonStartTime == -1L) {
            ScanUtils.getRoomFromPos(player.blockPos)?.uniqueRoom?.let { unq ->
                if (unq.state == RoomState.PREVISITED) return@let
                unq.state = RoomState.PREVISITED
                // TODO: unq.tiles does not work here, figure out why #536
                DungeonInfo.dungeonList.filter { (it as? Room)?.uniqueRoom == unq && it.state != RoomState.PREVISITED }.forEach {
                    it.state = RoomState.PREVISITED
                }
            }
            MapUpdater.updatePlayersUsingEntity()
        }
    }

    fun onWorldLoad(event: WorldUnloadEvent) {
        reset()
    }

    private val doorShape = Box(-1.0, 69.0, -1.0, 2.0, 73.0, 2.0)
    fun onWorldRender(event: WorldDrawEvent) {
        if (!Utils.inDungeons || DungeonTimer.bossEntryTime != -1L || !CatlasConfig.boxWitherDoors) return

        val doors = DungeonInfo.dungeonList.filter {
            it is Door && it.type != DoorType.NORMAL && it.state == RoomState.DISCOVERED && !it.opened
        }

        if (doors.isEmpty()) return

        val color = if (DungeonInfo.keys > 0) CatlasConfig.witherDoorKeyColor else CatlasConfig.witherDoorNoKeyColor

        val linesBuffer: VertexConsumer = event.entityVertexConsumers.getBuffer(CustomRenderLayers.espLines)
        event.matrices.push()
        event.matrices.translate(event.camera.pos.negate())
        doors.forEach {
            VertexRendering.drawOutline(
                event.matrices,
                linesBuffer,
                VoxelShapes.cuboid(doorShape),
                it.x.toDouble(),
                0.0,
                it.z.toDouble(),
                color.withAlpha(CatlasConfig.witherDoorOutline).rgb
            )
        }
        event.entityVertexConsumers.draw(CustomRenderLayers.espLines)

        val triangleStripBuffer: VertexConsumer = event.entityVertexConsumers.getBuffer(CustomRenderLayers.espFilledBoxLayer)
        doors.forEach {
            VertexRendering.drawFilledBox(
                event.matrices,
                triangleStripBuffer,
                doorShape.minX + it.x,
                doorShape.minY,
                doorShape.minZ + it.z,
                doorShape.maxX + it.x,
                doorShape.maxY,
                doorShape.maxZ + it.z,
                color.red / 255f,
                color.green / 255f,
                color.blue / 255f,
                CatlasConfig.witherDoorFill
            )
        }
        event.entityVertexConsumers.draw(CustomRenderLayers.espFilledBoxLayer)
        event.matrices.pop()
    }

    fun onPuzzleReset(event: DungeonPuzzleResetEvent) {
        val mapRoom = DungeonInfo.uniqueRooms.values.find { room ->
            room.mainRoom.data.type == RoomType.PUZZLE && Puzzle.fromName(room.name)?.tabName == event.puzzle
        }

        mapRoom?.mainRoom?.state = RoomState.DISCOVERED
    }

    fun onPacket(event: PacketReceiveEvent<*>) {
        if (event.packet is MapUpdateS2CPacket && Utils.inDungeons && DungeonInfo.dungeonMap == null) {
            val world = mc.world ?: return
            val id = event.packet.mapId
            val guess = FilledMapItem.getMapState(id, world) ?: return

            if (guess.decorations.any { it.type.matches(MapDecorationTypes.FRAME) }) {
                DungeonInfo.guessMapData = guess
            }
        }
    }

    init {
        CatlasElement

        arrayOf(
            HeightProvider,
            MimicDetector,
        ).forEach(EventSubscriber::setup)
    }
}
