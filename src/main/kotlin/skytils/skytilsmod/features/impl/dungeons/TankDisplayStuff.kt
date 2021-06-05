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

package skytils.skytilsmod.features.impl.dungeons

import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.bindColor

class TankDisplayStuff {

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Utils.inDungeons) return
        for (teammate in DungeonListener.team) {
            val player = teammate.player ?: continue
            if (!teammate.canRender()) continue
            if (teammate.dungeonClass == DungeonListener.DungeonClass.TANK) {
                if (Skytils.config.showTankRadius) {
                    // not sba healing circle wall code
                    GlStateManager.pushMatrix()
                    GL11.glNormal3f(0.0f, 1.0f, 0.0f)

                    GlStateManager.disableLighting()
                    GlStateManager.depthMask(false)
                    GlStateManager.enableDepth()
                    GlStateManager.enableBlend()
                    GlStateManager.depthFunc(GL11.GL_LEQUAL)
                    GlStateManager.disableCull()
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    GlStateManager.enableAlpha()
                    GlStateManager.disableTexture2D()

                    if (Skytils.config.showTankRadiusWall) {
                        Skytils.config.tankRadiusDisplayColor.bindColor()
                        RenderUtil.drawCylinderInWorld(
                            player.posX,
                            player.posY - 30,
                            player.posZ,
                            30f,
                            60f,
                            event.partialTicks
                        )
                    } else {
                        GlStateManager.disableDepth()
                        RenderUtil.drawCircle(
                            player,
                            event.partialTicks,
                            30.0,
                            Skytils.config.tankRadiusDisplayColor
                        )
                        GlStateManager.enableDepth()
                    }

                    GlStateManager.enableCull()
                    GlStateManager.enableTexture2D()
                    GlStateManager.enableDepth()
                    GlStateManager.depthMask(true)
                    GlStateManager.enableLighting()
                    GlStateManager.disableBlend()
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                    GlStateManager.popMatrix()
                }
                if (Skytils.config.boxedTanks && (teammate.player != mc.thePlayer || mc.gameSettings.thirdPersonView != 0)) {
                    GlStateManager.disableCull()
                    GlStateManager.disableDepth()
                    RenderUtil.drawOutlinedBoundingBox(
                        player.entityBoundingBox,
                        Skytils.config.boxedTankColor,
                        2f,
                        1f
                    )
                    GlStateManager.enableDepth()
                    GlStateManager.enableCull()
                }
            }
            if (Skytils.config.boxedProtectedTeammates && (player != mc.thePlayer || mc.gameSettings.thirdPersonView != 0)) {
                if (DungeonListener.team.any {
                        it.canRender() && it.dungeonClass == DungeonListener.DungeonClass.TANK && it != teammate && it.player?.getDistanceToEntity(
                            player
                        )!! <= 30
                    }) {
                    GlStateManager.disableCull()
                    GlStateManager.disableDepth()
                    RenderUtil.drawOutlinedBoundingBox(
                        player.entityBoundingBox,
                        Skytils.config.boxedProtectedTeammatesColor,
                        2f,
                        1f
                    )
                    GlStateManager.enableDepth()
                    GlStateManager.enableCull()
                }
            }
        }
    }
}