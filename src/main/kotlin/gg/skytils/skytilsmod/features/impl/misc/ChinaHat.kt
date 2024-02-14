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
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorMinecraft
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.bindColor
import gg.skytils.skytilsmod.utils.cheats.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

object ChinaHat {

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (Skytils.config.chinaHat) {
            if (!(Skytils.config.chinaHattrd && mc.gameSettings.thirdPersonView == 0)) {
                val entity = mc.thePlayer
                val timer = (mc as AccessorMinecraft).timer
                GL11.glPushMatrix()
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glDepthMask(false)
                GL11.glDisable(GL11.GL_CULL_FACE)
                if (!Skytils.config.chinaHatRainbow) {
                    GL11.glColor4f(
                        Skytils.config.chinaHatColor.red / 255f,
                        Skytils.config.chinaHatColor.green / 255f,
                        Skytils.config.chinaHatColor.blue / 255f,
                        Skytils.config.chinaHatColor.alpha / 255f
                    )
                }
                GL11.glTranslated(
                    entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - RenderUtil.getRenderX(),
                    entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - RenderUtil.getRenderY() + entity.height + 0f,
                    entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - RenderUtil.getRenderZ()
                )
                GL11.glRotatef((entity.ticksExisted + timer.renderPartialTicks) * 2f, 0f, 1f, 0f)

                GL11.glBegin(GL11.GL_TRIANGLE_FAN)
                GL11.glVertex3d(0.0, 0.3, 0.0)
                val radius = 0.7
                for (i in 0..360 step 5) {
                    if (Skytils.config.chinaHatRainbow) {
                        RenderUtils.glColor(
                            Color.getHSBColor(
                                if (i < 180) {
                                    0.41f + (0.58f - 0.41f) * (i / 180f)
                                } else {
                                    0.41f + (0.58f - 0.41f) * (-(i - 360) / 180f)
                                }, 0.7f, 1.0f
                            ), Skytils.config.chinaHatColor.alpha / 255f
                        )
                    }
                    GL11.glVertex3d(
                        cos(i.toDouble() * Math.PI / 180.0) * radius,
                        0.0,
                        sin(i.toDouble() * Math.PI / 180.0) * radius
                    )
                }
                GL11.glVertex3d(0.0, 0.3, 0.0)
                GL11.glEnd()

                GL11.glEnable(GL11.GL_CULL_FACE)
                GlStateManager.resetColor()
                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDepthMask(true)
                GL11.glDisable(GL11.GL_BLEND)
                GL11.glPopMatrix()
            }
        }

    }

}