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
package gg.skytils.skytilsmod.utils

import com.mojang.blaze3d.systems.RenderSystem
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.ChatColor
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft
import gg.essential.universal.UResolution
import gg.essential.universal.vertex.UBufferBuilder
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorMinecraft
import gg.skytils.skytilsmod.utils.rendering.DrawHelper
import gg.skytils.skytilsmod.utils.rendering.DrawHelper.writeRectCoords
import gg.skytils.skytilsmod.utils.rendering.SRenderPipelines
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer
import net.minecraft.client.texture.GlTexture
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.*

object RenderUtil {
    private val RARITY = Identifier.of("skytils", "gui/rarity.png")
    private val RARITY2 = Identifier.of("skytils", "gui/rarity2.png")
    private val RARITY3 = Identifier.of("skytils", "gui/rarity3.png")
    private val RARITY4 = Identifier.of("skytils", "gui/rarity4.png")
    private val CUSTOMRARITY = Identifier.of("skytils", "gui/customrarity.png")
    private val beaconBeam = Identifier.of("textures/entity/beacon_beam.png")
    private val mutex = ReentrantLock()

    fun renderBeaconBeam(matrixStack: UMatrixStack, rgb: Int, partialTicks: Float) {
        //#if MC>=12110
        //$$ val vertexConsumer = UMinecraft.getMinecraft().gameRenderer.entityRenderCommandQueue
        //$$ BeaconBlockEntityRenderer.renderBeam(
        //$$        matrixStack.toMC(),
        //$$        vertexConsumer,
        //$$        BeaconBlockEntityRenderer.BEAM_TEXTURE,
        //$$        partialTicks,
        //$$        1f,
        //$$        mc.world!!.time.toInt(),
        //$$        0,
        //$$        rgb,
        //$$        0.2f,
        //$$        0.25f
        //$$ )
        //#else
        val vertexConsumer = UMinecraft.getMinecraft().bufferBuilders.entityVertexConsumers
        BeaconBlockEntityRenderer.renderBeam(
            matrixStack.toMC(),
            vertexConsumer,
            beaconBeam,
            partialTicks,
            1f,
            mc.world!!.time,
            0,
            300,
            rgb,
            0.2f,
            0.25f
        )
        vertexConsumer.drawCurrentLayer()
        //#endif
    }

    internal fun <T> Color.withParts(block: (Int, Int, Int, Int) -> T) =
        block(this.red, this.green, this.blue, this.alpha)

    fun drawFilledBoundingBox(matrixStack: UMatrixStack, aabb: Box, c: Color, alphaMultiplier: Float = 1f, throughWalls: Boolean = false) {
        val buffer = UBufferBuilder.create(UGraphics.DrawMode.TRIANGLE_STRIP, UGraphics.CommonVertexFormats.POSITION_COLOR)
        DrawHelper.writeFilledCube(buffer, matrixStack, aabb, c.multAlpha(alphaMultiplier))
        buffer.build()?.drawAndClose(if (throughWalls) SRenderPipelines.noDepthBoxPipeline else SRenderPipelines.boxPipeline)
    }

    /**
     * @author Mojang
     */
    @JvmStatic
    fun drawOutlinedBoundingBox(aabb: Box?, color: Color, width: Float, partialTicks: Float, throughWalls: Boolean = false) {
        if (aabb == null) return
        val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)
        val matrices = UMatrixStack.Compat.get()
        matrices.push()
        DrawHelper.setupCameraTransformations(matrices)
        //#if MC<=12110
        RenderSystem.lineWidth(width)
        //#endif
        DrawHelper.writeOutlineCube(buffer, matrices, aabb, color.multAlpha(1f), width)
        buffer.build()?.drawAndClose(if (throughWalls) SRenderPipelines.noDepthBoxPipeline else SRenderPipelines.boxPipeline)
        matrices.pop()
    }

    /**
     * Taken from Skyblockcatia under MIT License
     * Modified
     * https://github.com/SteveKunG/SkyBlockcatia/blob/1.8.9/LICENSE.md
     *
     * @author SteveKunG
     */
    @JvmStatic
    fun renderItem(itemStack: ItemStack?, x: Int, y: Int) {
        DrawHelper.drawItemOnGUI(UMatrixStack.Compat.get(), itemStack ?: ItemStack.EMPTY, x.toDouble(), y.toDouble())
        DrawHelper.drawStackOverlay(UMatrixStack.Compat.get(), itemStack ?: ItemStack.EMPTY, x.toDouble(), y.toDouble())
    }

    @JvmStatic
    fun renderTexture(
        texture: Identifier?,
        x: Int,
        y: Int,
        width: Int = 16,
        height: Int = 16
    ) {
        if (texture == null) return
        DrawHelper.drawTexture(UMatrixStack.UNIT, SRenderPipelines.guiTexturePipeline, texture, x.toDouble(), y.toDouble(), width = width.toDouble(), height = height.toDouble())
    }

    fun draw3DLine(
        pos1: Vec3d,
        pos2: Vec3d,
        width: Int,
        color: Color,
        partialTicks: Float,
        matrixStack: UMatrixStack,
        alphaMultiplier: Float = 1f
    ) {
        matrixStack.push()
        DrawHelper.setupCameraTransformations(matrixStack)
        //#if MC<=12110
        RenderSystem.lineWidth(width.toFloat())
        //#endif
        val fixedColor = color.multAlpha(alphaMultiplier)
        val buffer = UBufferBuilder.create(UGraphics.DrawMode.LINE_STRIP, UGraphics.CommonVertexFormats.POSITION_COLOR)
        buffer.pos(matrixStack, pos1.x, pos1.y, pos1.z).color(fixedColor)
            //#if MC>=12111
            //$$ .lineWidth(width)
            //#endif
            .endVertex()
        buffer.pos(matrixStack, pos2.x, pos2.y, pos2.z).color(fixedColor)
            //#if MC>=12111
            //$$ .lineWidth(width)
            //#endif
            .endVertex()
        buffer.build()?.drawAndClose(SRenderPipelines.linesPipeline)
        matrixStack.pop()
    }

    fun draw3DLineStrip(
        points: Iterable<Vec3d>,
        width: Int,
        color: Color,
        partialTicks: Float,
        matrixStack: UMatrixStack,
        alphaMultiplier: Float = 1f
    ) {
        matrixStack.push()
        DrawHelper.setupCameraTransformations(matrixStack)
        //#if MC<=12110
        RenderSystem.lineWidth(width.toFloat())
        //#endif
        val fixedColor = color.multAlpha(alphaMultiplier)
        val buffer = UBufferBuilder.create(UGraphics.DrawMode.LINE_STRIP, UGraphics.CommonVertexFormats.POSITION_COLOR)
        for (pos in points) {
            buffer.pos(matrixStack, pos.x, pos.y, pos.z).color(fixedColor)
                //#if MC>=12111
                //$$ .lineWidth(width)
                //#endif
                .endVertex()
        }
        buffer.build()?.drawAndClose(SRenderPipelines.linesPipeline)
        matrixStack.pop()
    }

    fun drawLabel(
        pos: Vec3d,
        text: String,
        color: Color,
        partialTicks: Float,
        matrixStack: UMatrixStack,
        shadow: Boolean = false,
        scale: Float = 1f
    ) = drawNametag(pos.x, pos.y, pos.z, text, color, partialTicks, matrixStack, shadow, scale, false)

    fun renderWaypointText(str: String, loc: BlockPos, partialTicks: Float, matrixStack: UMatrixStack) =
        renderWaypointText(
            str,
            loc.x.toDouble(),
            loc.y.toDouble(),
            loc.z.toDouble(),
            partialTicks,
            matrixStack
        )

    fun renderWaypointText(
        str: String,
        x: Double,
        y: Double,
        z: Double,
        partialTicks: Float,
        matrixStack: UMatrixStack
    ) {
        matrixStack.push()
        val (viewerX, viewerY, viewerZ) = getViewerPos(partialTicks)
        val cameraEntity = mc.cameraEntity!!
        val distX = x - viewerX
        val distY = y - viewerY - cameraEntity.standingEyeHeight
        val distZ = z - viewerZ
        val dist = sqrt(distX * distX + distY * distY + distZ * distZ)
        val renderX: Double
        val renderY: Double
        val renderZ: Double
        if (dist > 12) {
            renderX = distX * 12 / dist + viewerX
            renderY = distY * 12 / dist + viewerY + cameraEntity.standingEyeHeight
            renderZ = distZ * 12 / dist + viewerZ
        } else {
            renderX = x
            renderY = y
            renderZ = z
        }
        drawNametag(renderX, renderY, renderZ, str, Color.WHITE, partialTicks, matrixStack)
        //#if MC>=12000
        // matrixStack.multiply(mc.entityRenderDispatcher.rotation)
        //#else
        //$$ matrixStack.rotate(-mc.entityRenderDispatcher.cameraYaw, 0.0f, 1.0f, 0.0f)
        //$$ matrixStack.rotate(mc.entityRenderDispatcher.cameraPitch, 1.0f, 0.0f, 0.0f)
        //#endif
        // matrixStack.translate(0.0, -0.25, 0.0)
        //#if MC>=12000
        // matrixStack.multiply(mc.entityRenderDispatcher.rotation.invert(Quaternionf()))
        //#else
        //$$ matrixStack.rotate(-mc.entityRenderDispatcher.cameraPitch, 1.0f, 0.0f, 0.0f)
        //$$ matrixStack.rotate(mc.entityRenderDispatcher.cameraYaw, 0.0f, 1.0f, 0.0f)
        //#endif
        drawNametag(
            renderX,
            renderY - 0.25,
            renderZ,
            "${ChatColor.YELLOW}${dist.roundToInt()}m",
            Color.WHITE,
            partialTicks,
            matrixStack
        )
        matrixStack.pop()
    }

    private fun drawNametag(
        x: Double, y: Double, z: Double,
        str: String, color: Color,
        partialTicks: Float, matrixStack: UMatrixStack,
        shadow: Boolean = true, scale: Float = 1f, background: Boolean = true
    ) {
        matrixStack.push()
        DrawHelper.cameraOffset(matrixStack)
        DrawHelper.drawNametag(matrixStack, str, x, y, z, shadow, scale, background)
        matrixStack.pop()
    }

    @JvmStatic
    fun renderRarity(itemStack: ItemStack?, xPos: Int, yPos: Int) {
        if (itemStack != null) {
            if (!Skytils.config.showPetRarity && ItemUtil.isPet(itemStack)) {
                return
            } else {
                if (!mutex.isLocked) {
                    mutex.lock()
                    renderRarity(xPos, yPos, itemStack)
                    mutex.unlock()
                }
            }
        }
    }

    @JvmStatic
    fun renderRarity(xPos: Int, yPos: Int, rarity: ItemRarity) {
        val alpha = Skytils.config.itemRarityOpacity
        val matrixStack = UMatrixStack()
        matrixStack.push()
        matrixStack.translate(xPos.toDouble(), yPos.toDouble(), 0.0)
        val texture = when (Skytils.config.itemRarityShape) {
            0 -> RARITY
            1 -> RARITY2
            2 -> RARITY3
            3 -> RARITY4
            4 -> CUSTOMRARITY
            else -> RARITY
        }
        val color = rarity.color.withAlpha(alpha)
        val tex = (mc.textureManager.getTexture(texture).glTexture as GlTexture)

        val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_TEXTURE_COLOR)
        buffer.pos(matrixStack, 0.0, 0.0, 0.0).tex(0.0, 0.0).color(color).endVertex()
        buffer.pos(matrixStack, 16.0, 0.0, 0.0).tex(1.0, 0.0).color(color).endVertex()
        buffer.pos(matrixStack, 16.0, 16.0, 0.0).tex(1.0, 1.0).color(color).endVertex()
        buffer.pos(matrixStack, 0.0, 16.0, 0.0).tex(0.0, 1.0).color(color).endVertex()
        buffer.build()?.drawAndClose(SRenderPipelines.rarityPipeline) {
            texture(0, tex.glId)
        }
        UGraphics.color4f(1f, 1f, 1f, 1f)
        matrixStack.pop()
    }

    private fun renderRarity(xPos: Int, yPos: Int, itemStack: ItemStack?) {
        if (itemStack == null) return
        val rarity = ItemUtil.getRarity(itemStack)
        if (rarity != ItemRarity.NONE) {

            if (Skytils.config.itemRarityShape < 5) {
                renderRarity(xPos, yPos, rarity)
            } else {
                renderRarity(xPos, yPos, rarity)
                // TODO: Redo using shader to apply post processing instead of hacky stencil test
//                val alpha = Skytils.config.itemRarityOpacity
//                val matrixStack = UMatrixStack()
//                matrixStack.push()
//                // save the states
//                val lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING)
//                val depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST)
//                val alphaEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST)
//
//                if (lightingEnabled) RenderSystem.method_4406()
//                if (depthEnabled) RenderSystem.disableDepthTest()
//                RenderSystem.enableBlend()
//                if (!alphaEnabled) RenderSystem.method_4456()
//
//                GL11.glEnable(GL11.GL_STENCIL_TEST) // Turn on da test
//                val scissorState = GL11.glGetInteger(GL11.GL_SCISSOR_TEST) // check if scissor test was on
//                GL11.glStencilMask(0xFF)
//                GL11.glDisable(GL11.GL_SCISSOR_TEST)
//                GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT) // Flush old data
//                if (scissorState == GL11.GL_TRUE) GL11.glEnable(GL11.GL_SCISSOR_TEST)
//
//                GL11.glStencilMask(0xFF) // Writing = ON
//                GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF) // Always "add" to frame
//                GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE) // Replace on success
//                GL11.glColorMask(false, false, false, false)
//                //Anything rendered here becomes "cut" frame.
//
//                val scale = 1.2
//                RenderSystem.method_4412(
//                    xPos.toDouble(),
//                    yPos.toDouble(),
//                    0.0
//                )
//
//                RenderSystem.pushMatrix()
//                RenderSystem.method_4412(8.0, 8.0, 0.0)
//                RenderSystem.method_4453(scale, scale, 0.0)
//                RenderSystem.method_4412(-8.0, -8.0, 0.0)
//                skipGlint = true
//                renderItem(itemStack, 0, 0)
//                skipGlint = false
//                RenderSystem.popMatrix()
//
//                GL11.glColorMask(true, true, true, true)
//
//                GL11.glStencilMask(0x00) // Writing = OFF
//                GL11.glStencilFunc(
//                    GL11.GL_NOTEQUAL,
//                    0,
//                    0xFF
//                ) // Anything that wasn't defined above will not be rendered.
//                //Anything rendered here will be cut if goes beyond frame defined before.
//                DrawContext.fill(
//                    1, 1, 17, 17,
//                    Color(
//                        (rarity.color.red).coerceAtLeast(0) / 255f,
//                        (rarity.color.green).coerceAtLeast(0) / 255f,
//                        (rarity.color.blue).coerceAtLeast(0) / 255f,
//                        alpha
//                    ).rgb
//                )
//                GL11.glDisable(GL11.GL_STENCIL_TEST)
//
//                if (lightingEnabled) RenderSystem.method_4394()
//                if (depthEnabled) RenderSystem.enableDepthTest()
//                if (!alphaEnabled) RenderSystem.method_4441()
//                RenderSystem.popMatrix()
            }

        }
    }

    /**
     * Taken from SkyblockAddons under MIT License
     * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
     * @author BiscuitDevelopment
     *
     */
    // TODO: Fix later when UC supports QUAD_STRIP
    fun drawCylinderInWorld(x: Double, y: Double, z: Double, radius: Float, height: Float, partialTicks: Float) {
//        var x1 = x
//        var y1 = y
//        var z1 = z
//        val renderViewEntity = mc.cameraEntity!!
//        val viewX =
//            renderViewEntity.lastX + (renderViewEntity.x - renderViewEntity.lastX) * partialTicks.toDouble()
//        val viewY =
//            renderViewEntity.lastY + (renderViewEntity.y - renderViewEntity.lastY) * partialTicks.toDouble()
//        val viewZ =
//            renderViewEntity.lastZ + (renderViewEntity.z - renderViewEntity.lastZ) * partialTicks.toDouble()
//        x1 -= viewX
//        y1 -= viewY
//        z1 -= viewZ
//        val tessellator = Tessellator.getInstance()
//        val worldrenderer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, VertexFormats.POSITION)
////        worldrenderer.begin(GL11.GL_QUAD_STRIP, VertexFormats.POSITION)
//        var currentAngle = 0f
//        val angleStep = 0.1f
//        while (currentAngle < 2 * Math.PI) {
//            val xOffset = radius * cos(currentAngle.toDouble()).toFloat()
//            val zOffset = radius * sin(currentAngle.toDouble()).toFloat()
//            worldrenderer.pos(x1 + xOffset, y1 + height, z1 + zOffset).next()
//            worldrenderer.pos(x1 + xOffset, y1 + 0, z1 + zOffset).next()
//            currentAngle += angleStep
//        }
//        worldrenderer.vertex(x1 + radius, y1 + height, z1).next()
//        worldrenderer.vertex(x1 + radius, y1 + 0.0, z1).next()
//        tessellator.draw()
    }

    fun drawCircle(matrixStack: UMatrixStack, x: Double, y: Double, z: Double, partialTicks: Float, radius: Double, edges: Int, r: Int, g: Int, b: Int, a: Int = 255) {
        val angleDelta = Math.PI * 2 / edges
        //#if MC<=12105
        RenderSystem.lineWidth(5f)
        //#endif
        val buffer = UBufferBuilder.create(UGraphics.DrawMode.LINE_STRIP, UGraphics.CommonVertexFormats.POSITION_COLOR)
        val (dx, dy, dz) = getViewerPos(partialTicks)
        repeat(edges) { idx ->
            buffer.pos(matrixStack, x - dx + radius * cos(idx * angleDelta), y - dy, z - dz + radius * sin(idx * angleDelta)).color(r, g, b, a)
                //#if MC>=12111
                //$$ .lineWidth(5f)
                //#endif
                .endVertex()
        }
        buffer.pos(matrixStack, x + radius - dx, y - dy, z - dz).color(r, g, b, a).endVertex()
        buffer.build()?.drawAndClose(SRenderPipelines.linesPipeline)
    }

    fun getViewerPos(partialTicks: Float): Triple<Double, Double, Double> {
        val viewer = mc.cameraEntity!!
        val viewerX = viewer.lastRenderX + (viewer.x - viewer.lastRenderX) * partialTicks
        val viewerY = viewer.lastRenderY + (viewer.y - viewer.lastRenderY) * partialTicks
        val viewerZ = viewer.lastRenderZ + (viewer.z - viewer.lastRenderZ) * partialTicks
        return Triple(viewerX, viewerY, viewerZ)
    }

    fun getPartialTicks(entity: Entity? = null) =
        (mc as AccessorMinecraft).timer.getTickProgress(entity?.let { mc.world?.tickManager?.shouldSkipTick(it)?.not() } ?: false)

    /**
     * Helper method for fixRenderPos
     */
    fun getRenderX() : Double {
        return mc.gameRenderer.camera.pos.x
    }

    /**
     * Helper method for fixRenderPos
     */
    fun getRenderY() : Double {
        return mc.gameRenderer.camera.pos.y
    }

    /**
     * Helper method for fixRenderPos
     */
    fun getRenderZ() : Double {
        return mc.gameRenderer.camera.pos.z
    }

    /**
     * Method used to Gather event location parameters and return their interpolated counterparts.
     *
     * Working particularly well in RenderLivingEvent.Pre/Post<*>
     */
    fun fixRenderPos(x: Double, y: Double, z: Double, invert: Boolean = false) : Triple<Double, Double, Double> {
        return Triple(x + getRenderX(), y + getRenderY(), z + getRenderZ())
    }

    infix fun Slot.highlight(color: Color) {
        val matrices = UMatrixStack()
        DrawHelper.setupContainerScreenTransformations(matrices)
        val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)
        writeRectCoords(matrices, buffer, x.toDouble(), y.toDouble(), x + 16.0, y + 16.0, color)
        buffer.build()?.drawAndClose(SRenderPipelines.guiPipeline)
    }

    fun drawDurabilityBar(xPos: Int, yPos: Int, durability: Double) {
        val progress = (13.0 - durability * 13.0).roundToInt()
        val f = max(0.0f, (255f - durability.toFloat()) / 255f)
        val color = Color.getHSBColor(f / 3.0f, 1.0f, 1.0f)

        val matrices = UMatrixStack()
        DrawHelper.setupContainerScreenTransformations(matrices, aboveItems = true)
        val x = xPos + 2.0
        val y = yPos + 13.0
        val buffer = UBufferBuilder.create(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)
        writeRectCoords(matrices, buffer, x, y, x + 13, y + 2, Color.BLACK)
        matrices.translate(0f, 0f, 200f)
        writeRectCoords(
            matrices,
            buffer,
            x,
            y,
            x + progress,
            y + 1,
            color
        )
        matrices.translate(0f, 0f, -200f)
        buffer.build()?.drawAndClose(SRenderPipelines.guiPipeline)
    }

    // see GuiIngame
    private val vignetteTexPath = Identifier.of("textures/misc/vignette.png")

    /**
     * @author Mojang (modified)
     * @see net.minecraft.client.gui.GuiIngame.renderVignette
     */
    fun drawVignette(color: Color) {
        // Changing the alpha doesn't affect the vignette, so we have to use the alpha to change the color values
        val newColor = Color(
            (1f - (color.red / 255f)) * (color.alpha / 255f),
            (1f - (color.green / 255f)) * (color.alpha / 255f),
            (1f - (color.blue / 255f)) * (color.alpha / 255f),
            1f
        )

        val sr = UResolution
        val matrices = UMatrixStack.UNIT

        DrawHelper.drawTexture(
            matrices,
            SRenderPipelines.vignettePipeline,
            vignetteTexPath,
            0.0,
            0.0,
            0.0,
            0.0,
            sr.scaledWidth.toDouble(),
            sr.scaledHeight.toDouble(),
            color = newColor
        )
    }

    /*
    * @link https://stackoverflow.com/a/54913292
    */
    fun mixColors(vararg colors: Color): Color {
        val ratio = 1f / colors.size.toFloat()
        var r = 0
        var g = 0
        var b = 0
        var a = 0
        for (color in colors) {
            r += (color.red * ratio).toInt()
            g += (color.green * ratio).toInt()
            b += (color.blue * ratio).toInt()
            a += (color.alpha * ratio).toInt()
        }
        return Color(r, g, b, a)
    }

    fun interpolate(currentValue: Double, lastValue: Double, multiplier: Float): Double {
        return lastValue + (currentValue - lastValue) * multiplier
    }

    fun drawSelectionBox(
        pos: BlockPos,
        color: Color,
        partialTicks: Float
    ) {
        // TODO: convert this to VertexRendering#drawOutline
        val matrixStack = UMatrixStack()
        DrawHelper.setupCameraTransformations(matrixStack)
        drawFilledBoundingBox(
            matrixStack,
            Box(pos)
                .expandBlock(),
            color
        )
    }
}

fun Color.withAlpha(alpha: Int): Int = (alpha.coerceIn(0, 255) shl 24) or (this.rgb and 0x00ffffff)

fun Color.multAlpha(mult: Float) = Color(
    red,
    green,
    blue,
    (alpha * mult).toInt().coerceIn(0, 255)
)

fun Box.expandBlock(): Box =
    expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)