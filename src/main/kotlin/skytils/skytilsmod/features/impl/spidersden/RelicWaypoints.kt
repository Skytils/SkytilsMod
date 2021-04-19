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

import com.google.common.collect.ImmutableSet
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.DataFetcher
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.events.PacketEvent.SendEvent
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.Utils
import java.awt.Color
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class RelicWaypoints {
    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inSkyblock) return
        if (event.packet is S2APacketParticles) {
            val packet = event.packet as S2APacketParticles?
            val type = packet!!.particleType
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
        if (SBInfo.instance.location == null || !SBInfo.instance.location
                .equals("combat_1", ignoreCase = true)
        ) return
        if (event.packet is C08PacketPlayerBlockPlacement) {
            val packet = event.packet as C08PacketPlayerBlockPlacement?
            if (relicLocations.contains(packet!!.position)) {
                foundRelics.add(packet.position)
                rareRelicLocations.remove(packet.position)
                writeSave()
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock) return
        if (SBInfo.instance.location == null || !SBInfo.instance.location
                .equals("combat_1", ignoreCase = true)
        ) return
        val viewer = Minecraft.getMinecraft().renderViewEntity
        val viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks
        val viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks
        val viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks
        if (Skytils.config.relicWaypoints) {
            for (relic in ImmutableSet.copyOf(relicLocations)) {
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
            for (relic in ImmutableSet.copyOf(rareRelicLocations)) {
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

    companion object {
        val relicLocations = LinkedHashSet<BlockPos>()
        val foundRelics = HashSet<BlockPos>()
        private val rareRelicLocations = HashSet<BlockPos>()
        private val gson = GsonBuilder().setPrettyPrinting().create()
        private var saveFile = File(Skytils.modDir, "found_spiders_den_relics.json");
        fun reloadSave() {
            foundRelics.clear()
            var dataArray: JsonArray?
            try {
                FileReader(saveFile).use { `in` ->
                    dataArray = gson.fromJson(`in`, JsonArray::class.java)
                    for (serializedPosition in DataFetcher.getStringArrayFromJsonArray(dataArray as JsonArray)) {
                        val parts = serializedPosition!!.split(",".toRegex()).toTypedArray()
                        foundRelics.add(BlockPos(parts[0].toInt(), parts[1].toInt(), parts[2].toInt()))
                    }
                }
            } catch (e: Exception) {
                dataArray = JsonArray()
                try {
                    FileWriter(saveFile).use { writer -> gson.toJson(dataArray, writer) }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        fun writeSave() {
            try {
                FileWriter(saveFile).use { writer ->
                    val arr = JsonArray()
                    for (found in foundRelics) {
                        arr.add(JsonPrimitive(found.x.toString() + "," + found.y + "," + found.z))
                    }
                    gson.toJson(arr, writer)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    init {
        reloadSave()
    }
}