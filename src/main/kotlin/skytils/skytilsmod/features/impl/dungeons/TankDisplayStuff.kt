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
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.bindColor

class TankDisplayStuff {

    @SubscribeEvent
    fun onEvent(event: Event) {
        if (!Utils.inDungeons) return
        when (event) {
            is RenderWorldLastEvent -> {
                for (player in DungeonListener.team) {
                    if (player.player.health <= 0) continue
                    if (player.dungeonClass == DungeonListener.DungeonClass.TANK) {
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
                                    player.player.posX,
                                    player.player.posY - 30,
                                    player.player.posZ,
                                    30f,
                                    60f,
                                    event.partialTicks
                                )
                            } else {
                                GlStateManager.disableDepth()
                                RenderUtil.drawCircle(
                                    player.player,
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
                        if (Skytils.config.boxedTanks) {
                            GlStateManager.disableDepth()
                            RenderUtil.drawOutlinedBoundingBox(
                                player.player.entityBoundingBox,
                                Skytils.config.boxedTankColor,
                                2f,
                                1f
                            )
                            GlStateManager.enableDepth()
                        }
                    }
                    if (Skytils.config.boxedProtectedTeammates) {
                        if (DungeonListener.team.any {
                                it.dungeonClass == DungeonListener.DungeonClass.TANK && it != player && it.player.health > 0 && it.player.getDistanceToEntity(
                                    player.player
                                ) <= 30
                            }) {
                            GlStateManager.disableDepth()
                            RenderUtil.drawOutlinedBoundingBox(
                                player.player.entityBoundingBox,
                                Skytils.config.boxedTankColor,
                                2f,
                                1f
                            )
                            GlStateManager.enableDepth()
                        }
                    }
                }
            }
        }
    }
}