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

package gg.skytils.skytilsmod.features.impl.misc

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.cheats.RenderUtils
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.item.ItemEnderPearl
import net.minecraft.util.*
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.awt.Color

object Projectiles {

    @SubscribeEvent
    fun onRenderLivingPre(event: RenderLivingEvent.Pre<*>) {
        if (Skytils.config.projectiles) {
            mc.thePlayer.heldItem ?: return

            val item = mc.thePlayer.heldItem.item
            if (item !is ItemEnderPearl) {
                return
            }
            mc.renderManager

            var motionFactor = 1.5F
            var motionSlowdown = 0.99F
            val gravity: Float
            val size: Float



            gravity = 0.03F
            size = 0.25F


            // Yaw and pitch of player
            val yaw = mc.thePlayer.rotationYaw

            val pitch =
                mc.thePlayer.rotationPitch


            // Positions
            var posX = RenderUtil.getRenderX() - MathHelper.cos(yaw / 180F * 3.1415927F) * 0.16F
            var posY = RenderUtil.getRenderY() + mc.thePlayer.getEyeHeight() - 0.10000000149011612
            var posZ = RenderUtil.getRenderZ() - MathHelper.sin(yaw / 180F * 3.1415927F) * 0.16F

            // Motions
            var motionX = (-MathHelper.sin(yaw / 180f * 3.1415927F) * MathHelper.cos(pitch / 180F * 3.1415927F) *
                    0.4)
            var motionY = -MathHelper.sin(
                (pitch) /
                        180f * 3.1415927f
            ) * 0.4
            var motionZ = (MathHelper.cos(yaw / 180f * 3.1415927F) * MathHelper.cos(pitch / 180F * 3.1415927F) *
                    0.4)
            val distance = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ)

            motionX /= distance
            motionY /= distance
            motionZ /= distance
            motionX *= motionFactor
            motionY *= motionFactor
            motionZ *= motionFactor

            // Landing
            var landingPosition: MovingObjectPosition? = null
            var hasLanded = false
            var hitEntity = false

            val tessellator = Tessellator.getInstance()
            val worldRenderer = tessellator.worldRenderer
            val pos = mutableListOf<Vec3>()

            // calc path
            while (!hasLanded && posY > 0.0) {
                // Set pos before and after
                var posBefore = Vec3(posX, posY, posZ)
                var posAfter = Vec3(posX + motionX, posY + motionY, posZ + motionZ)

                // Get landing position
                landingPosition = mc.theWorld.rayTraceBlocks(
                    posBefore, posAfter, false,
                    true, false
                )

                // Set pos before and after
                posBefore = Vec3(posX, posY, posZ)
                posAfter = Vec3(posX + motionX, posY + motionY, posZ + motionZ)

                // Check if arrow is landing
                if (landingPosition != null) {
                    hasLanded = true
                    posAfter = Vec3(
                        landingPosition.hitVec.xCoord,
                        landingPosition.hitVec.yCoord,
                        landingPosition.hitVec.zCoord
                    )
                }

                // Set arrow box
                val arrowBox = AxisAlignedBB(
                    posX - size, posY - size, posZ - size, posX + size,
                    posY + size, posZ + size
                ).addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0)
                val chunkMinX = MathHelper.floor_double((arrowBox.minX - 2.0) / 16.0)
                val chunkMaxX = MathHelper.floor_double((arrowBox.maxX + 2.0) / 16.0)
                val chunkMinZ = MathHelper.floor_double((arrowBox.minZ - 2.0) / 16.0)
                val chunkMaxZ = MathHelper.floor_double((arrowBox.maxZ + 2.0) / 16.0)

                // Check which entities colliding with the arrow
                val collidedEntities = mutableListOf<Entity>()
                for (x in chunkMinX..chunkMaxX)
                    for (z in chunkMinZ..chunkMaxZ)
                        mc.theWorld.getChunkFromChunkCoords(x, z)
                            .getEntitiesWithinAABBForEntity(mc.thePlayer, arrowBox, collidedEntities, null)

                // Check all possible entities
                for (possibleEntity in collidedEntities) {
                    if (possibleEntity.canBeCollidedWith() && possibleEntity !== mc.thePlayer) {
                        val possibleEntityBoundingBox = possibleEntity.entityBoundingBox
                            .expand(size.toDouble(), size.toDouble(), size.toDouble())

                        val possibleEntityLanding = possibleEntityBoundingBox
                            .calculateIntercept(posBefore, posAfter) ?: continue

                        hitEntity = true
                        hasLanded = true
                        landingPosition = possibleEntityLanding
                    }
                }

                // Affect motions of arrow
                posX += motionX
                posY += motionY
                posZ += motionZ

                // Check is next position water
                if (mc.theWorld.getBlockState(BlockPos(posX, posY, posZ)).block.material === Material.water) {
                    // Update motion
                    motionX *= 0.6
                    motionY *= 0.6
                    motionZ *= 0.6
                } else { // Update motion
                    motionX *= motionSlowdown.toDouble()
                    motionY *= motionSlowdown.toDouble()
                    motionZ *= motionSlowdown.toDouble()
                }

                motionY -= gravity.toDouble()

                // Draw path
                pos.add(
                    Vec3(
                        posX - RenderUtil.getRenderX(), posY - RenderUtil.getRenderY(),
                        posZ - RenderUtil.getRenderZ()
                    )
                )
            }

            // Start drawing of path
            GL11.glDepthMask(false)
            RenderUtils.enableGlCap(GL11.GL_BLEND, GL11.GL_LINE_SMOOTH)
            RenderUtils.disableGlCap(GL11.GL_DEPTH_TEST, GL11.GL_ALPHA_TEST, GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
            RenderUtils.glColor(
                if (hitEntity) {
                    Color(255, 140, 140)
                } else {
                    Color(140, 255, 140)
                }
            )
            GL11.glLineWidth(2f)

            worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)

            pos.forEach {
                worldRenderer.pos(it.xCoord, it.yCoord, it.zCoord).endVertex()
            }

            // End the rendering of the path
            tessellator.draw()
            GL11.glPushMatrix()
            GL11.glTranslated(
                posX - RenderUtil.getRenderX(), posY - RenderUtil.getRenderY(),
                posZ - RenderUtil.getRenderZ()
            )

            if (landingPosition != null) {
                when (landingPosition.sideHit.axis.ordinal) {
                    0 -> GL11.glRotatef(90F, 0F, 0F, 1F)
                    2 -> GL11.glRotatef(90F, 1F, 0F, 0F)
                }

                RenderUtils.drawAxisAlignedBB(
                    AxisAlignedBB(-0.5, 0.0, -0.5, 0.5, 0.1, 0.5), if (hitEntity) {
                        Color(255, 140, 140)
                    } else {
                        Color(140, 255, 140)
                    }, true, true, 3f
                )
            }
            GL11.glPopMatrix()
            GL11.glDepthMask(true)
            RenderUtils.resetCaps()
            GL11.glColor4f(1F, 1F, 1F, 1F)
        }

    }

}