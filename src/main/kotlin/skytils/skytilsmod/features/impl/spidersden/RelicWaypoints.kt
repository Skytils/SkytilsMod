/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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
package skytils.skytilsmod.features.impl.spidersden

import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import skytils.skytilsmod.events.impl.PacketEvent.SendEvent
import skytils.skytilsmod.features.impl.trackers.Tracker
import skytils.skytilsmod.utils.*
import java.awt.Color
import java.io.FileReader
import java.io.FileWriter

class RelicWaypoints : Tracker("found_spiders_den_relics") {
    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inSkyblock) return
        if (event.packet is S2APacketParticles) {
            val packet = event.packet
            val type = packet.particleType
            val longDistance = packet.isLongDistance
            val count = packet.particleCount
            val speed = packet.particleSpeed
            val xOffset = packet.xOffset
            val yOffset = packet.yOffset
            val zOffset = packet.zOffset
            val x = packet.xCoordinate
            val y = packet.yCoordinate
            val z = packet.zCoordinate
            if (Skytils.config.rareRelicFinder) {
                val filter =
                    type == EnumParticleTypes.SPELL_WITCH && count == 2 && longDistance && speed == 0f && xOffset == 0.3f && yOffset == 0.3f && zOffset == 0.3f
                if (filter && relicLocations.contains(BlockPos(x, y, z))) {
                    rareRelicLocations.add(BlockPos(x, y, z))
                }
            }
        }
    }

    @SubscribeEvent
    fun onSendPacket(event: SendEvent) {
        if (!Utils.inSkyblock) return
        if (SBInfo.mode != SkyblockIsland.SpiderDen.mode) return
        if (event.packet is C08PacketPlayerBlockPlacement) {
            val packet = event.packet as C08PacketPlayerBlockPlacement?
            if (relicLocations.contains(packet!!.position)) {
                foundRelics.add(packet.position)
                rareRelicLocations.remove(packet.position)
                markDirty<RelicWaypoints>()
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock) return
        if (SBInfo.mode != SkyblockIsland.SpiderDen.mode) return
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)

        if (Skytils.config.relicWaypoints) {
            for (relic in relicLocations) {
                if (foundRelics.contains(relic)) continue
                val x = relic.x - viewerX
                val y = relic.y - viewerY
                val z = relic.z - viewerZ
                val distSq = x * x + y * y + z * z
                GlStateManager.disableDepth()
                GlStateManager.disableCull()
                RenderUtil.drawFilledBoundingBox(AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), Color(114, 245, 82), 1f)
                GlStateManager.disableTexture2D()
                if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(
                    x,
                    y + 1,
                    z,
                    Color(114, 245, 82).rgb,
                    1.0f,
                    event.partialTicks
                )
                RenderUtil.renderWaypointText("Relic", relic, event.partialTicks)
                GlStateManager.disableLighting()
                GlStateManager.enableTexture2D()
                GlStateManager.enableDepth()
                GlStateManager.enableCull()
            }
        }
        if (Skytils.config.rareRelicFinder) {
            for (relic in rareRelicLocations) {
                val x = relic.x - viewerX
                val y = relic.y - viewerY
                val z = relic.z - viewerZ
                val distSq = x * x + y * y + z * z
                GlStateManager.disableDepth()
                GlStateManager.disableCull()
                RenderUtil.drawFilledBoundingBox(AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), Color(152, 41, 222), 1f)
                GlStateManager.disableTexture2D()
                if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(
                    x,
                    y + 1,
                    z,
                    Color(152, 41, 222).rgb,
                    1.0f,
                    event.partialTicks
                )
                RenderUtil.renderWaypointText("Rare Relic", relic, event.partialTicks)
                GlStateManager.disableLighting()
                GlStateManager.enableTexture2D()
                GlStateManager.enableDepth()
                GlStateManager.enableCull()
            }
        }
    }

    override fun resetLoot() {
        foundRelics.clear()
    }

    override fun read(reader: FileReader) {
        foundRelics.clear()
        for (serializedPosition in gson.fromJson(reader, JsonArray::class.java).asJsonArray.map { it.asString }) {
            val parts = serializedPosition.split(",".toRegex()).toTypedArray()
            foundRelics.add(BlockPos(parts[0].toInt(), parts[1].toInt(), parts[2].toInt()))
        }
    }

    override fun write(writer: FileWriter) {
        val arr = JsonArray()
        for (found in foundRelics) {
            arr.add(JsonPrimitive(found.x.toString() + "," + found.y + "," + found.z))
        }
        gson.toJson(arr, writer)
    }

    override fun setDefault(writer: FileWriter) {
        gson.toJson(JsonArray(), writer)
    }

    companion object {
        val relicLocations = ConcurrentHashSet<BlockPos>()
        val foundRelics = ConcurrentHashSet<BlockPos>()
        private val rareRelicLocations = ConcurrentHashSet<BlockPos>()
    }
}