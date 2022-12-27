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

package gg.skytils.skytilsmod.features.impl.dungeons

import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.structure.FloatPair
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.bindColor
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import skytils.hylin.skyblock.dungeons.DungeonClass

object TankDisplayStuff {
    enum class TankStatus() {
        PLAYER,
        DEAD,
        NEARBY,
        FAR,
        MISSING
    }
    var tankStatus = TankStatus.MISSING

    init {
        NearbyTankDisplay()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Utils.inDungeons) return
        if (Skytils.config.nearbyTankDisplay) {
            tankStatus = if (DungeonListener.team.values.find {
                    it.player == mc.thePlayer
                }?.dungeonClass == DungeonClass.TANK) {
                TankStatus.PLAYER
            } else if (DungeonListener.team.values.any {
                    it.player != mc.thePlayer && DungeonClass.TANK == it.dungeonClass
                }) {
                val tanks = DungeonListener.team.values.filter {
                    it.player != mc.thePlayer && DungeonClass.TANK == it.dungeonClass
                }
                if (DungeonListener.deads.containsAll(tanks)) TankStatus.DEAD
                else {
                    var foundNearby = false
                    for (teammate in tanks) {
                        val tank = teammate.player ?: continue
                        if (tank.getDistanceToEntity(mc.thePlayer) <= 30) {
                            foundNearby = true
                            break
                        }
                    }
                    if (foundNearby) TankStatus.NEARBY else TankStatus.FAR
                }
            } else TankStatus.MISSING
        }

        for (teammate in DungeonListener.team.values) {
            val player = teammate.player ?: continue
            if (!teammate.canRender()) continue
            if (teammate.dungeonClass == DungeonClass.TANK) {
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
                if (DungeonListener.team.values.any {
                        it.canRender() && it.dungeonClass == DungeonClass.TANK && it != teammate && it.player?.getDistanceToEntity(
                            player
                        )!! <= 30
                    }) {
                    GlStateManager.disableCull()
                    GlStateManager.disableDepth()
                    RenderUtil.drawOutlinedBoundingBox(
                        player.entityBoundingBox, Skytils.config.boxedProtectedTeammatesColor, 2f, 1f
                    )
                    GlStateManager.enableDepth()
                    GlStateManager.enableCull()
                }
            }
        }
    }

    class NearbyTankDisplay : GuiElement("Nearby Tank Display", FloatPair(0.25f, 0.85f)) {
        override fun render() {
            if (toggled && Utils.inDungeons && !Utils.equalsOneOf(tankStatus, TankStatus.MISSING, TankStatus.PLAYER)) {
                val alignment =
                    if (actualX < UResolution.scaledWidth / 2f) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                val status = when (tankStatus) {
                    TankStatus.DEAD -> "§cDead"
                    TankStatus.NEARBY -> "§aNearby"
                    TankStatus.FAR -> "§eFar Away"
                    else -> "§7Unknown"
                }

                ScreenRenderer.fontRenderer.drawString(
                    "§aTank: $status",
                    if (actualX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            val alignment =
                if (actualX < UResolution.scaledWidth / 2f) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "§aTank: Nearby",
                if (actualX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                0f,
                CommonColors.WHITE,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§aTank: Nearby")
        override val toggled: Boolean
            get() = Skytils.config.nearbyTankDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}