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

package gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.utils

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.CataclysmicMap
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.CataclysmicMapConfig
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.core.DungeonMapPlayer
import gg.skytils.skytilsmod.features.impl.dungeons.cataclysmicmap.features.dungeon.DungeonScan
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.bindColor
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.GL_QUADS
import java.awt.Color

object RenderUtils {

    private val tessellator: Tessellator = Tessellator.getInstance()
    private val worldRenderer: WorldRenderer = tessellator.worldRenderer
    private val mapIcons = ResourceLocation("skytils", "cataclysmicmap/marker.png")

    private fun preDraw() {
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    }

    private fun postDraw() {
        GlStateManager.disableBlend()
        GlStateManager.enableDepth()
        GlStateManager.enableTexture2D()
    }

    private fun addQuadVertices(x: Double, y: Double, w: Double, h: Double) {
        worldRenderer.pos(x, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y + h, 0.0).endVertex()
        worldRenderer.pos(x + w, y, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).endVertex()
    }

    fun drawTexturedQuad(x: Double, y: Double, width: Double, height: Double) {
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(x, y + height, 0.0).tex(0.0, 1.0).endVertex()
        worldRenderer.pos(x + width, y + height, 0.0).tex(1.0, 1.0).endVertex()
        worldRenderer.pos(x + width, y, 0.0).tex(1.0, 0.0).endVertex()
        worldRenderer.pos(x, y, 0.0).tex(0.0, 0.0).endVertex()
        tessellator.draw()
    }

    fun renderRect(x: Double, y: Double, w: Double, h: Double, color: Color) {
        if (color.alpha == 0) return
        preDraw()
        color.bindColor()

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x, y, w, h)
        tessellator.draw()

        postDraw()
    }

    fun renderRectBorder(x: Double, y: Double, w: Double, h: Double, thickness: Double, color: Color) {
        if (color.alpha == 0) return
        preDraw()
        color.bindColor()

        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        addQuadVertices(x - thickness, y, thickness, h)
        addQuadVertices(x - thickness, y - thickness, w + thickness * 2, thickness)
        addQuadVertices(x + w, y, thickness, h)
        addQuadVertices(x - thickness, y + h, w + thickness * 2, thickness)
        tessellator.draw()

        postDraw()
    }

    fun renderCenteredText(text: List<String>, x: Int, y: Int, color: Int) {
        if (text.isEmpty()) return
        GlStateManager.pushMatrix()
        GlStateManager.translate(x.toFloat(), y.toFloat(), 0f)
        GlStateManager.scale(CataclysmicMapConfig.textScale, CataclysmicMapConfig.textScale, 1f)

        if (CataclysmicMapConfig.mapRotate) {
            GlStateManager.rotate(mc.thePlayer.rotationYaw + 180f, 0f, 0f, 1f)
        } else if (CataclysmicMapConfig.mapDynamicRotate) {
            GlStateManager.rotate(-CataclysmicMap.CataclysmicMapRender.dynamicRotation, 0f, 0f, 1f)
        }

        val fontHeight = mc.fontRendererObj.FONT_HEIGHT + 1
        val yTextOffset = text.size * fontHeight / -2f

        text.withIndex().forEach { (index, text) ->
            mc.fontRendererObj.drawString(
                text,
                mc.fontRendererObj.getStringWidth(text) / -2f,
                yTextOffset + index * fontHeight,
                color,
                true
            )
        }

        if (CataclysmicMapConfig.mapDynamicRotate) {
            GlStateManager.rotate(CataclysmicMap.CataclysmicMapRender.dynamicRotation, 0f, 0f, 1f)
        }

        GlStateManager.popMatrix()
    }

    fun drawPlayerHead(name: String, player: DungeonMapPlayer) {
        GlStateManager.pushMatrix()
        try {
            // Translates to the player's location which is updated every tick.
            if (player.isPlayer || name == mc.thePlayer.name) {
                GlStateManager.translate(
                    (mc.thePlayer.posX - DungeonScan.startX + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.first,
                    (mc.thePlayer.posZ - DungeonScan.startZ + 15) * MapUtils.coordMultiplier + MapUtils.startCorner.second,
                    0.0
                )
            } else {
                GlStateManager.translate(player.mapX.toFloat(), player.mapZ.toFloat(), 0f)
            }

            // Apply head rotation and scaling
            GlStateManager.rotate(player.yaw + 180f, 0f, 0f, 1f)
            GlStateManager.scale(CataclysmicMapConfig.playerHeadScale, CataclysmicMapConfig.playerHeadScale, 1f)

            if (CataclysmicMapConfig.mapVanillaMarker && (player.isPlayer || name == mc.thePlayer.name)) {
                GlStateManager.rotate(180f, 0f, 0f, 1f)
                GlStateManager.color(1f, 1f, 1f, 1f)
                mc.textureManager.bindTexture(mapIcons)
                worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
                worldRenderer.pos(-6.0, 6.0, 0.0).tex(0.0, 0.0).endVertex()
                worldRenderer.pos(6.0, 6.0, 0.0).tex(1.0, 0.0).endVertex()
                worldRenderer.pos(6.0, -6.0, 0.0).tex(1.0, 1.0).endVertex()
                worldRenderer.pos(-6.0, -6.0, 0.0).tex(0.0, 1.0).endVertex()
                tessellator.draw()
                GlStateManager.rotate(-180f, 0f, 0f, 1f)
            } else {
                // Render black border around the player head
                renderRectBorder(-6.0, -6.0, 12.0, 12.0, 1.0, Color(0, 0, 0, 255))

                preDraw()
                GlStateManager.enableTexture2D()
                GlStateManager.color(1f, 1f, 1f, 1f)

                mc.textureManager.bindTexture(player.skin)

                Gui.drawScaledCustomSizeModalRect(-6, -6, 8f, 8f, 8, 8, 12, 12, 64f, 64f)
                if (player.renderHat) {
                    Gui.drawScaledCustomSizeModalRect(-6, -6, 40f, 8f, 8, 8, 12, 12, 64f, 64f)
                }

                postDraw()
            }

            // Handle player names
            if (CataclysmicMapConfig.playerHeads == 2 || CataclysmicMapConfig.playerHeads == 1 && Utils.equalsOneOf(
                    ItemUtil.getSkyBlockItemID(mc.thePlayer.heldItem),
                    "SPIRIT_LEAP", "INFINITE_SPIRIT_LEAP", "HAUNT_ABILITY"
                )
            ) {
                if (!CataclysmicMapConfig.mapRotate) {
                    GlStateManager.rotate(-player.yaw + 180f, 0f, 0f, 1f)
                }
                GlStateManager.translate(0f, 10f, 0f)
                GlStateManager.scale(CataclysmicMapConfig.playerNameScale, CataclysmicMapConfig.playerNameScale, 1f)
                mc.fontRendererObj.drawString(
                    name, -mc.fontRendererObj.getStringWidth(name) / 2f, 0f, 0xffffff, true
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        GlStateManager.popMatrix()
    }
}
