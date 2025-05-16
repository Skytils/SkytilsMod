/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.core

import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.boss.BossMaps
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.boss.icons.IconContext
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.RenderUtils
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import java.awt.Color

object CatlasBossElement : GuiElement(name = "Dungeon Boss Map", x = 0, y = 0) {
    override fun render() {
        if (!toggled || mc.thePlayer == null || DungeonTimer.bossEntryTime == -1L) return
        val map = BossMaps[DungeonFeatures.dungeonFloorNumber] ?: return
        val layer = map.getLayer(mc.thePlayer!!) ?: return
        val regionWidth = map.x.last - map.x.first + 1
        val regionHeight = map.z.last - map.z.first + 1
        val xScale = width / regionWidth.toDouble()
        val yScale = height / regionHeight.toDouble()

        val iconCtx = IconContext(
            map = map,
            layer = layer,
            element = this,
            regionWidth = regionWidth,
            regionHeight = regionHeight,
            xScale = xScale,
            yScale = yScale
        )

        mc.mcProfiler.startSection("border")

        RenderUtils.renderRect(
            0.0, 0.0, 128.0, 128.0, CatlasConfig.mapBackground
        )

        RenderUtils.renderRectBorder(
            0.0,
            0.0,
            128.0,
            128.0,
            CatlasConfig.mapBorderWidth.toDouble(),
            CatlasConfig.mapBorder
        )

        mc.mcProfiler.endSection()


        if (CatlasConfig.mapRotate) {
            GlStateManager.pushMatrix()
            val mcScale = UResolution.scaleFactor
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            GL11.glScissor(
                (scaleX * mcScale).toInt(),
                (mc.displayHeight - scaleY * mcScale - 128 * mcScale * scale).toInt(),
                (128 * mcScale * scale).toInt(),
                (128 * mcScale * scale).toInt()
            )
            GlStateManager.translate(64.0, 64.0, 0.0)
            GlStateManager.rotate(-mc.thePlayer!!.rotationYaw + 180f, 0f, 0f, 1f)

            if (CatlasConfig.mapCenter) {
                GlStateManager.translate(
                    -iconCtx.worldToIconX(mc.thePlayer!!.posX.toInt()),
                    -iconCtx.worldToIconY(mc.thePlayer!!.posZ.toInt()),
                    0.0
                )
            } else {
                GlStateManager.translate(-64.0, -64.0, 0.0)
            }
        }

        mc.mcProfiler.startSection("bossTexture")

        GlStateManager.enableTexture2D()
        GlStateManager.enableAlpha()
        GlStateManager.color(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(layer.texture)
        RenderUtils.drawTexturedQuad(0.0, 0.0, 128.0, 128.0)

        mc.mcProfiler.endSection()

        mc.mcProfiler.startSection("icons")
        layer.iconRenderers.forEach {
            it.draw(iconCtx)
        }
        mc.mcProfiler.endSection()

        if (CatlasConfig.mapRotate) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
            GlStateManager.popMatrix()
        }
    }

    override fun demoRender() {
        Gui.drawRect(0, 0, 128, 128, Color.RED.rgb)
        fr.drawString("Dungeon Boss Map", 64f, 5f, alignment = SmartFontRenderer.TextAlignment.MIDDLE)
    }

    override val toggled: Boolean
        get() = CatlasConfig.bossMapEnabled
    override val height: Int = 128
    override val width: Int = 128

    init {
        Skytils.guiManager.registerElement(this)
    }
}