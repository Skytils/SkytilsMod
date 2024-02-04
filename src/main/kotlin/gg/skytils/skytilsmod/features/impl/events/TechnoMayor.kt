/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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
package gg.skytils.skytilsmod.features.impl.events

import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import gg.skytils.skytilsmod.utils.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

object TechnoMayor {
    private val orbLocations = CopyOnWriteArrayList<Vec3>()

    @SubscribeEvent
    fun onRenderSpecialLivingPre(event: RenderLivingEvent.Specials.Pre<EntityLivingBase?>) {
        if (!Utils.inSkyblock) return
        val e = event.entity
        if (e !is EntityArmorStand || !e.hasCustomName() || e.isDead) return
        if (e.customNameTag != "§6§lSHINY ORB") return
        val pos = e.position
        mc.theWorld.getEntitiesWithinAABBExcludingEntity(
            e,
            AxisAlignedBB(pos, BlockPos(pos.x + 1, pos.y + 1, pos.z + 1))
        ).find {
            if (it is EntityArmorStand && it.hasCustomName() && it.getCustomNameTag().contains(mc.thePlayer.name)) {
                it.worldObj.removeEntity(it)
                e.worldObj.removeEntity(e)
                orbLocations.add(Vec3(pos.x + 0.5, (pos.y - 2).toDouble(), pos.z + 0.5))
                return@find true
            }
            return@find false
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock) return
        if (SBInfo.mode != SkyblockIsland.Hub.mode && SBInfo.mode != SkyblockIsland.FarmingIsland.mode) return
        if (!Skytils.config.shinyOrbWaypoints) return

        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
        val matrixStack = UMatrixStack()
        for (orb in orbLocations) {
            val x = orb.xCoord - viewerX
            val y = orb.yCoord - viewerY
            val z = orb.zCoord - viewerZ
            val distSq = x * x + y * y + z * z
            GlStateManager.disableCull()
            GlStateManager.disableTexture2D()
            if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(x, y, z, Color(114, 245, 82).rgb, 0.75f, event.partialTicks)
            GlStateManager.disableDepth()
            RenderUtil.renderWaypointText(
                "Orb",
                orb.xCoord,
                orb.yCoord + 1.5f,
                orb.zCoord,
                event.partialTicks,
                matrixStack
            )
            GlStateManager.disableLighting()
            GlStateManager.enableTexture2D()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    fun onChatPacket(event: ReceiveEvent) {
        if (event.packet !is S02PacketChat) return
        val packet = event.packet
        if (packet.type == 2.toByte()) return
        val unformatted = packet.chatComponent.unformattedText.stripControlCodes()
        if (unformatted == "Your Shiny Orb and associated pig expired and disappeared." || unformatted == "SHINY! The orb is charged! Click on it for loot!") {
            orbLocations.removeIf { pos: Vec3 ->
                mc.thePlayer.position.distanceSq(
                    pos.xCoord,
                    pos.yCoord,
                    pos.zCoord
                ) < 7 * 7
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        orbLocations.clear()
    }
}