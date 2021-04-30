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
package skytils.skytilsmod.features.impl.events

import com.google.common.collect.ImmutableList
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
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
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.utils.*
import java.awt.Color

class TechnoMayor {
    @SubscribeEvent
    fun onRenderSpecialLivingPre(event: RenderLivingEvent.Specials.Pre<EntityLivingBase?>) {
        if (!Utils.inSkyblock) return
        val entity: Entity = event.entity
        if (event.entity !is EntityArmorStand || !entity.hasCustomName() || event.entity.isDead) return
        val e = entity as EntityArmorStand
        if (e.customNameTag != "§6§lSHINY ORB") return
        val origin = e.position
        val nearbyEntities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
            e,
            AxisAlignedBB(origin, BlockPos(origin.x + 1, origin.y + 1, origin.z + 1))
        )
        for (ent in nearbyEntities) {
            if (ent is EntityArmorStand && ent.hasCustomName() && ent.getCustomNameTag().contains(mc.thePlayer.name)) {
                ent.worldObj.removeEntity(ent)
                e.worldObj.removeEntity(e)
                orbLocations.add(Vec3(origin.x + 0.5, (origin.y - 2).toDouble(), origin.z + 0.5))
                break
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock) return
        if (SBInfo.mode != SBInfo.SkyblockIslands.HUB.mode && SBInfo.mode != SBInfo.SkyblockIslands.FARMINGISLANDS.mode) return
        if (!Skytils.config.shinyOrbWaypoints) return

        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
        for (orb in ImmutableList.copyOf(orbLocations)) {
            val x = orb.xCoord - viewerX
            val y = orb.yCoord - viewerY
            val z = orb.zCoord - viewerZ
            val distSq = x * x + y * y + z * z
            GlStateManager.disableCull()
            GlStateManager.disableTexture2D()
            if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(x, y, z, Color(114, 245, 82).rgb, 0.75f, event.partialTicks)
            GlStateManager.disableDepth()
            RenderUtil.renderWaypointText("Orb", orb.xCoord, orb.yCoord + 1.5f, orb.zCoord, event.partialTicks)
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
        if (packet.type.toInt() == 2) return
        val unformatted = StringUtils.stripControlCodes(packet.chatComponent.unformattedText)
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
    fun onWorldChange(event: WorldEvent.Load?) {
        orbLocations.clear()
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        private val orbLocations: MutableList<Vec3> = ArrayList()
    }
}