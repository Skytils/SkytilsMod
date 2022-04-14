/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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
import gg.essential.universal.UMatrixStack
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.impl.MainReceivePacketEvent
import skytils.skytilsmod.events.impl.PacketEvent.SendEvent
import skytils.skytilsmod.features.impl.trackers.Tracker
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.SkyblockIsland
import skytils.skytilsmod.utils.Utils
import java.awt.Color
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class RelicWaypoints : Tracker("found_spiders_den_relics") {
    @SubscribeEvent
    fun onReceivePacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inSkyblock) return
        if (event.packet is S2APacketParticles) {
            if (Skytils.config.rareRelicFinder) {
                event.packet.apply {
                    if (particleType == EnumParticleTypes.SPELL_WITCH && particleCount == 2 && isLongDistance && particleSpeed == 0f && xOffset == 0.3f && yOffset == 0.3f && zOffset == 0.3f) {
                        rareRelicLocations.add(BlockPos(xCoordinate, yCoordinate, zCoordinate))
                    }
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
        val matrixStack = UMatrixStack()

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
                if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(
                    x,
                    y + 1,
                    z,
                    Color(114, 245, 82).rgb,
                    1.0f,
                    event.partialTicks
                )
                RenderUtil.renderWaypointText("Relic", relic, event.partialTicks, matrixStack)
                GlStateManager.disableLighting()
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
                if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(
                    x,
                    y + 1,
                    z,
                    Color(152, 41, 222).rgb,
                    1.0f,
                    event.partialTicks
                )
                RenderUtil.renderWaypointText("Rare Relic", relic, event.partialTicks, matrixStack)
                GlStateManager.disableLighting()
                GlStateManager.enableDepth()
                GlStateManager.enableCull()
            }
        }
    }

    override fun resetLoot() {
        foundRelics.clear()
    }

    override fun read(reader: InputStreamReader) {
        foundRelics.clear()
        for (serializedPosition in gson.fromJson(reader, JsonArray::class.java).asJsonArray.map { it.asString }) {
            val parts = serializedPosition.split(",".toRegex()).toTypedArray()
            foundRelics.add(BlockPos(parts[0].toInt(), parts[1].toInt(), parts[2].toInt()))
        }
    }

    override fun write(writer: OutputStreamWriter) {
        val arr = JsonArray()
        for (found in foundRelics) {
            arr.add(JsonPrimitive(found.x.toString() + "," + found.y + "," + found.z))
        }
        gson.toJson(arr, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        gson.toJson(JsonArray(), writer)
    }

    companion object {
        val relicLocations = HashSet<BlockPos>()
        val foundRelics = HashSet<BlockPos>()
        private val rareRelicLocations = HashSet<BlockPos>()
    }
}