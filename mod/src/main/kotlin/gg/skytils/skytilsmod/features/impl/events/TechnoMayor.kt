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
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.utils.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.passive.EntityPig
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.event.entity.player.EntityInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object TechnoMayor {
    private val shinyPigs = HashMap<Vec3, EntityPig?>()
    private var latestPig: EntityPig? = null

    @SubscribeEvent
    fun onRenderSpecialLivingPre(event: RenderLivingEvent.Specials.Pre<EntityLivingBase?>) {
        if (!Utils.inSkyblock) return
        val e = event.entity
        if (!e.isValidPigLabel()) return
        val pos = e.position
        mc.theWorld.getEntitiesWithinAABBExcludingEntity(
            e,
            AxisAlignedBB(pos, BlockPos(pos.x + 1, pos.y + 1, pos.z + 1))
        ).find {
            it is EntityArmorStand && mc.thePlayer.name in it.customNameTag
        }?.let {
            it.worldObj.removeEntity(it)
            e.worldObj.removeEntity(e)
            shinyPigs.putIfAbsent(Vec3(pos.x + 0.5, (pos.y - 2).toDouble(), pos.z + 0.5), latestPig)
            latestPig = null
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock) return
        if (SBInfo.mode != SkyblockIsland.Hub.mode && SBInfo.mode != SkyblockIsland.FarmingIsland.mode) return
        if (!Skytils.config.shinyOrbWaypoints) return

        shinyPigs.values.removeAll { it?.isDead != false }

        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
        val matrixStack = UMatrixStack()
        GlState.pushState()
        for (entry in shinyPigs) {
            val orb = entry.key
            val pig = entry.value
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
            if (Skytils.config.shinyPigLocations) {
                if (pig != null) {
                    RenderUtil.renderWaypointText(
                        "Pig",
                        pig.posX,
                        pig.posY + 0.5,
                        pig.posZ,
                        event.partialTicks,
                        matrixStack
                    )
                    RenderUtil.draw3DLine(
                        Vec3(pig.posX, pig.posY + 0.5, pig.posZ), Vec3(orb.x, orb.y + 1.5, orb.z),
                        1, Color.RED, event.partialTicks, matrixStack
                    )
                    RenderUtil.drawOutlinedBoundingBox(pig.entityBoundingBox, Color.RED, 1f, event.partialTicks)
                }
            }
        }
        GlState.popState()
    }

    @SubscribeEvent
    fun onEntityInteract(event: EntityInteractEvent) {
        if (!Utils.inSkyblock) return
        checkPig(event.target as? EntityPig ?: return)
    }

    @SubscribeEvent
    fun onEntityAttack(event: AttackEntityEvent) {
        if (!Utils.inSkyblock) return
        checkPig(event.target as? EntityPig ?: return)
    }

    fun checkPig(entity: EntityPig) {
        if (mc.theWorld.getEntitiesWithinAABBExcludingEntity(
            entity,
            AxisAlignedBB(
                BlockPos(entity.posX - 1, entity.posY, entity.posZ - 1),
                BlockPos(entity.posX + 1, entity.posY + 2, entity.posZ + 1)
            )
        ).any { it.isValidPigLabel() }) {
            latestPig = entity
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        shinyPigs.clear()
        latestPig = null
    }

    private fun Entity.isValidPigLabel() = this is EntityArmorStand && !isDead && customNameTag == "§6§lSHINY PIG"
}