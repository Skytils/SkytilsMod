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

package gg.skytils.skytilsmod.utils.rendering

import com.mojang.blaze3d.systems.RenderSystem
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.vertex.UBufferBuilder
import gg.skytils.skytilsmod.Skytils.mc
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.OverlayTexture
import net.minecraft.item.ItemDisplayContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Colors
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import org.joml.Quaternionf
import java.awt.Color

object DrawHelper {
    private val textureManager = mc.textureManager

    /**
     * Applies the camera offset to the given matrices.
     * This is useful for rendering things in world space, as it will negate the camera position.
     */
    fun cameraOffset(matrices: UMatrixStack) {
        matrices.translate(mc.gameRenderer.camera.pos.negate())
    }

    /**
     * Applies the camera rotation to the given matrices.
     * This is useful for rendering things in world space, as it will negate the camera rotation.
     */
    fun cameraRotation(matrices: UMatrixStack) {
        matrices.multiply(mc.gameRenderer.camera.rotation.conjugate(Quaternionf()))
    }

    /**
     * Applies the camera offset and rotation to the given matrices.
     * This is useful for rendering things in world space, as it will negate the camera position and rotation.
     */
    fun setupCameraTransformations(matrices: UMatrixStack) {
        cameraOffset(matrices)
        cameraRotation(matrices)
    }

    /**
    * Writes a cube outline to the given buffer. Draw must still be called manually.
    * Buffer must be created with [gg.essential.universal.UGraphics.DrawMode.LINES]
    */
    fun writeOutlineCube(
        buffer: UBufferBuilder,
        matrices: UMatrixStack,
        box: Box,
        color: Color
    ) {
        box.apply {
            buffer.pos(matrices, minX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, maxX, minY, minZ).color(color).endVertex()

            buffer.pos(matrices, maxX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, maxX, minY, maxZ).color(color).endVertex()

            buffer.pos(matrices, maxX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, minX, minY, maxZ).color(color).endVertex()

            buffer.pos(matrices, minX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, minX, minY, minZ).color(color).endVertex()

            buffer.pos(matrices, minX, maxY, minZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, minZ).color(color).endVertex()

            buffer.pos(matrices, maxX, maxY, minZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, maxZ).color(color).endVertex()

            buffer.pos(matrices, maxX, maxY, maxZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, maxZ).color(color).endVertex()

            buffer.pos(matrices, minX, maxY, maxZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, minZ).color(color).endVertex()

            buffer.pos(matrices, minX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, minZ).color(color).endVertex()

            buffer.pos(matrices, maxX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, minZ).color(color).endVertex()

            buffer.pos(matrices, maxX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, maxZ).color(color).endVertex()

            buffer.pos(matrices, minX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, maxZ).color(color).endVertex()   
        }
    }

    /**
     * Writes a filled cube to the given buffer. Draw must still be called manually.
     * Buffer must be created with [gg.essential.universal.UGraphics.DrawMode.TRIANGLE_STRIP]
     */
    fun writeFilledCube(
        buffer: UBufferBuilder,
        matrices: UMatrixStack,
        box: Box,
        color: Color
    ) {
        box.apply {
            buffer.pos(matrices, minX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, maxZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, maxZ).color(color).endVertex()
            buffer.pos(matrices, minX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, minZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, minZ).color(color).endVertex()
            buffer.pos(matrices, maxX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, maxX, minY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, minY, maxZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, minZ).color(color).endVertex()
            buffer.pos(matrices, minX, maxY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, minZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, maxZ).color(color).endVertex()
            buffer.pos(matrices, maxX, maxY, maxZ).color(color).endVertex()
        }
    }


    /**
     * Writes a texture quad to the buffer. Draw must still be called manually.
     * Buffer must be created with [gg.essential.universal.UGraphics.DrawMode.QUADS]
     */
    fun drawTexture(
        matrices: UMatrixStack,
        buffer: UBufferBuilder,
        sprite: Identifier,
        x: Double,
        y: Double,
        u: Double = 0.0,
        v: Double = 0.0,
        width: Double,
        height: Double,
        textureWidth: Double = width,
        textureHeight: Double = height,
        color: Color = Color.WHITE
    ) {
        val abstractTexture = textureManager.getTexture(sprite)
        abstractTexture.setFilter(false, false)
        RenderSystem.setShaderTexture(0, abstractTexture.glTexture)
        val x2 = x + width
        val y2 = y + height
        val u1 = u / textureWidth
        val u2 = (u + width) / textureWidth
        val v1 = v / textureHeight
        val v2 = (v + height) / textureHeight
        buffer.pos(matrices, x, y, 0.0).tex(u1, v1).color(color).endVertex()
        buffer.pos(matrices, x, y2, 0.0).tex(u1, v2).color(color).endVertex()
        buffer.pos(matrices, x2, y2, 0.0).tex(u2, v2).color(color).endVertex()
        buffer.pos(matrices, x2, y, 0.0).tex(u2, v1).color(color).endVertex()
    }

    /**
     * Draws a nametag at the given position.
     * This method will apply the camera rotation for you, but not the camera offset.
    */
    fun drawNametag(matrices: UMatrixStack, text: String, x: Double, y: Double, z: Double, shadow: Boolean = true, scale: Float = 1f, background: Boolean = true, throughWalls: Boolean = false) {
        matrices.push()
        matrices.translate(x, y + 0.5, z)
        matrices.multiply(mc.entityRenderDispatcher.rotation)
        matrices.scale(0.025f, -0.025f, 0.025f)

        matrices.scale(scale, scale, scale)
        val centerPos = UGraphics.getStringWidth(text) / -2f
        val backgroundColor = if (!background) 0 else (mc.options.getTextBackgroundOpacity(0.25f) * 255).toInt() shl 24
        mc.textRenderer.draw(
            text,
            centerPos,
            0f,
            Colors.WHITE,
            shadow,
            matrices.peek().model,
            mc.bufferBuilders.entityVertexConsumers,
            if (throughWalls) TextRenderer.TextLayerType.SEE_THROUGH else TextRenderer.TextLayerType.NORMAL,
            backgroundColor,
            15728880
        )
        matrices.pop()
    }

    /**
     * Writes a rectangle to the buffer. Draw must still be called manually.
     * Buffer must be created with [gg.essential.universal.UGraphics.DrawMode.QUADS]
    */
    fun writeRect(matrices: UMatrixStack, buffer: UBufferBuilder, x: Double, y: Double, width: Double, height: Double, color: Color) {
        writeRectCoords(matrices, buffer, x, y, x + width, y + height, color)
    }

    fun writeRectCoords(
        matrices: UMatrixStack,
        buffer: UBufferBuilder,
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        color: Color
    ) {
        buffer.pos(matrices, x1, y1, 0.0).color(color).endVertex()
        buffer.pos(matrices, x1, y2, 0.0).color(color).endVertex()
        buffer.pos(matrices, x2, y2, 0.0).color(color).endVertex()
        buffer.pos(matrices, x2, y1, 0.0).color(color).endVertex()
    }

    fun drawItemOnGUI(matrices: UMatrixStack, stack: ItemStack, x: Double, y: Double, z: Double = 100.0, dynamicDisplay: Boolean = true) {
        if (stack.isEmpty) return
        matrices.push()
        matrices.translate(x + 8, y + 8, (150 + z))

        matrices.scale(16.0f, -16.0f, 16.0f)

        mc.itemRenderer.renderItem(if (dynamicDisplay) mc.player else null, stack, ItemDisplayContext.GUI, matrices.toMC(), mc.bufferBuilders.entityVertexConsumers, mc.world, 15728880, OverlayTexture.DEFAULT_UV, 0)

        matrices.pop()
    }

    fun drawStackOverlay(matrices: UMatrixStack, stack: ItemStack, x: Double, y: Double, stackCountText: String? = null) {
        if (stack.isEmpty) return
        matrices.push()
        drawItemBar(matrices, stack, x, y)
        drawStackCount(matrices, stack, x, y, stackCountText)
        drawCooldownProgress(matrices, stack, x, y)
        matrices.pop()
    }

    fun drawItemBar(matrices: UMatrixStack, stack: ItemStack, x: Double, y: Double) {
        if (stack.isItemBarVisible) {
            val i = x + 2
            val j = y + 13
            val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)
            writeRectCoords(matrices, buffer, i, j, i + 13, j + 2, Color.BLACK)
            matrices.translate(0f, 0f, 200f)
            writeRectCoords(
                matrices,
                buffer,
                i,
                j,
                i + stack.itemBarStep,
                j + 1,
                Color(stack.itemBarColor)
            )
            matrices.translate(0f, 0f, -200f)
            buffer.build()?.drawAndClose(SRenderPipelines.guiPipeline)
        }
    }

    fun drawStackCount(matrices: UMatrixStack, stack: ItemStack, x: Double, y: Double, stackCountText: String?) {
        if (stack.count != 1 || stackCountText != null) {
            val string = stackCountText ?: stack.count.toString()
            matrices.push()
            matrices.translate(0.0f, 0.0f, 200.0f)
            UGraphics.drawString(
                matrices,
                string,
                (x + 19 - 2 - UGraphics.getStringWidth(string)).toFloat(),
                (y + 6 + 3).toFloat(),
                Colors.WHITE,
                true
            )
            matrices.pop()
        }
    }

    fun drawCooldownProgress(matrices: UMatrixStack, stack: ItemStack?, x: Double, y: Double) {
        val clientPlayerEntity = mc.player
        val f = clientPlayerEntity?.itemCooldownManager
            ?.getCooldownProgress(stack, mc.renderTickCounter.getTickProgress(true)) ?: 0f
        if (f > 0.0f) {
            val i = y + MathHelper.floor(16.0f * (1.0f - f))
            val j = i + MathHelper.ceil(16.0f * f)
            val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)
            matrices.translate(0f, 0f, 200f)
            writeRectCoords(matrices, buffer, x, i, x + 16, j, Color(Int.MAX_VALUE, true))
            matrices.translate(0f, 0f, -200f)
            buffer.build()?.drawAndClose(SRenderPipelines.guiPipeline)
        }
    }
}