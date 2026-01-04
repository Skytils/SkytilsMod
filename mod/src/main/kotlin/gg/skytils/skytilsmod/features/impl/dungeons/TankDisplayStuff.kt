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

package gg.skytils.skytilsmod.features.impl.dungeons

import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.render.WorldDrawEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.DungeonClass
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import com.mojang.blaze3d.systems.RenderSystem
import org.lwjgl.opengl.GL11

object TankDisplayStuff : EventSubscriber {

    override fun setup() {
        register(::onRenderWorld)
    }

    fun onRenderWorld(event: WorldDrawEvent) {
        if (!Utils.inDungeons) return
        for (teammate in DungeonListener.team.values) {
            val player = teammate.player ?: continue
            if (!teammate.canRender()) continue
            // TODO: fix later (with render pipeline)
//            if (teammate.dungeonClass == DungeonClass.TANK) {
//                if (Skytils.config.showTankRadius) {
//                    // not sba healing circle wall code
//                    RenderSystem.pushMatrix()
//                    GL11.glNormal3f(0.0f, 1.0f, 0.0f)
//
//                    RenderSystem.method_4406()
//                    RenderSystem.depthMask(false)
//                    RenderSystem.enableDepthTest()
//                    RenderSystem.enableBlend()
//                    RenderSystem.depthFunc(GL11.GL_LEQUAL)
//                    RenderSystem.disableCull()
//                    RenderSystem.blendFuncSeparate(770, 771, 1, 0)
//                    RenderSystem.method_4456()
//                    RenderSystem.method_4407()
//
//                    if (Skytils.config.showTankRadiusWall) {
//                        Skytils.config.tankRadiusDisplayColor.bindColor()
//                        RenderUtil.drawCylinderInWorld(
//                            player.x,
//                            player.y - 30,
//                            player.z,
//                            30f,
//                            60f,
//                            event.partialTicks
//                        )
//                    } else {
//                        RenderSystem.disableDepthTest()
//                        RenderUtil.drawCircle(
//                            player,
//                            event.partialTicks,
//                            30.0,
//                            Skytils.config.tankRadiusDisplayColor
//                        )
//                        RenderSystem.enableDepthTest()
//                    }
//
//                    RenderSystem.enableCull()
//                    RenderSystem.method_4397()
//                    RenderSystem.enableDepthTest()
//                    RenderSystem.depthMask(true)
//                    RenderSystem.method_4394()
//                    RenderSystem.disableBlend()
//                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
//                    RenderSystem.popMatrix()
//                }
//                if (Skytils.config.boxedTanks && (teammate.player != mc.player || mc.options.perspective != 0)) {
//                    RenderSystem.disableCull()
//                    RenderSystem.disableDepthTest()
//                    RenderUtil.drawOutlinedBoundingBox(
//                        player.boundingBox,
//                        Skytils.config.boxedTankColor,
//                        2f,
//                        1f
//                    )
//                    RenderSystem.enableDepthTest()
//                    RenderSystem.enableCull()
//                }
//            }
//            if (Skytils.config.boxedProtectedTeammates && (player != mc.player || mc.options.perspective != 0)) {
//                if (DungeonListener.team.values.any {
//                        it.canRender() && it.dungeonClass == DungeonClass.TANK && it != teammate && it.player?.distanceTo(
//                            player
//                        )!! <= 30
//                    }) {
//                    RenderSystem.disableCull()
//                    RenderSystem.disableDepthTest()
//                    RenderUtil.drawOutlinedBoundingBox(
//                        player.boundingBox,
//                        Skytils.config.boxedProtectedTeammatesColor,
//                        2f,
//                        1f
//                    )
//                    RenderSystem.enableDepthTest()
//                    RenderSystem.enableCull()
//                }
//            }
        }
    }
}