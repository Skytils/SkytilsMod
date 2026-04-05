package gg.skytils.skytilsmod.util

import gg.essential.elementa.utils.withAlpha
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexRendering
import net.minecraft.text.Text
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.Vec3i
import net.minecraft.util.shape.VoxelShapes
import java.awt.Color
import kotlin.math.sqrt

fun WorldRenderContext.drawOutline(pos: Vec3i, color: Color, width: Float) {
    val camPos = this.worldState().cameraRenderState.pos
    val ms = this.matrices()
    val vcp = this.consumers()

    ms.push()
    ms.translate(-camPos.x, -camPos.y, -camPos.z)

    VertexRendering.drawOutline(
        ms,
        vcp.getBuffer(RenderLayers.lines()),
        VoxelShapes.fullCube(),
        pos.x.toDouble(),
        pos.y.toDouble(),
        pos.z.toDouble(),
        color.rgb,
        width
    )

    ms.pop()
}

fun WorldRenderContext.drawOverlay(pos: Vec3i, color: Color) {
    val camPos = this.worldState().cameraRenderState.pos
    val ms = this.matrices()
    val vcp = this.consumers()
    val consumer = vcp.getBuffer(RenderLayers.debugFilledBox())

    // We love Z-Fighting
    val inflate = 0.0005f

    val x1 = pos.x.toFloat() - inflate
    val y1 = pos.y.toFloat() - inflate
    val z1 = pos.z.toFloat() - inflate
    val x2 = x1 + 1f + inflate * 2
    val y2 = y1 + 1f + inflate * 2
    val z2 = z1 + 1f + inflate * 2

    val r = color.red / 255f
    val g = color.green / 255f
    val b = color.blue / 255f
    val a = color.alpha / 255f

    ms.push()
    ms.translate(-camPos.x, -camPos.y, -camPos.z)

    val entry = ms.peek()

    // bottom
    consumer.vertex(entry, x1, y1, z1).color(r, g, b, a)
    consumer.vertex(entry, x2, y1, z1).color(r, g, b, a)
    consumer.vertex(entry, x2, y1, z2).color(r, g, b, a)
    consumer.vertex(entry, x1, y1, z2).color(r, g, b, a)
    // top
    consumer.vertex(entry, x1, y2, z1).color(r, g, b, a)
    consumer.vertex(entry, x1, y2, z2).color(r, g, b, a)
    consumer.vertex(entry, x2, y2, z2).color(r, g, b, a)
    consumer.vertex(entry, x2, y2, z1).color(r, g, b, a)
    // north
    consumer.vertex(entry, x1, y1, z1).color(r, g, b, a)
    consumer.vertex(entry, x1, y2, z1).color(r, g, b, a)
    consumer.vertex(entry, x2, y2, z1).color(r, g, b, a)
    consumer.vertex(entry, x2, y1, z1).color(r, g, b, a)
    // south
    consumer.vertex(entry, x1, y1, z2).color(r, g, b, a)
    consumer.vertex(entry, x2, y1, z2).color(r, g, b, a)
    consumer.vertex(entry, x2, y2, z2).color(r, g, b, a)
    consumer.vertex(entry, x1, y2, z2).color(r, g, b, a)
    // west
    consumer.vertex(entry, x1, y1, z1).color(r, g, b, a)
    consumer.vertex(entry, x1, y1, z2).color(r, g, b, a)
    consumer.vertex(entry, x1, y2, z2).color(r, g, b, a)
    consumer.vertex(entry, x1, y2, z1).color(r, g, b, a)
    // east
    consumer.vertex(entry, x2, y1, z1).color(r, g, b, a)
    consumer.vertex(entry, x2, y2, z1).color(r, g, b, a)
    consumer.vertex(entry, x2, y2, z2).color(r, g, b, a)
    consumer.vertex(entry, x2, y1, z2).color(r, g, b, a)

    ms.pop()
}

fun WorldRenderContext.drawLine(
    from: Vec3i,
    to: Vec3i,
    color: Color,
    width: Float,
    xOffset: Float = 0.5f,
    yOffset: Float = 0.5f,
    zOffset: Float = 0.5f
) {
    val camPos = this.worldState().cameraRenderState.pos
    val ms = this.matrices()
    val vcp = this.consumers()

    val x1 = from.x + xOffset
    val y1 = from.y + yOffset
    val z1 = from.z + zOffset
    val x2 = to.x + xOffset
    val y2 = to.y + yOffset
    val z2 = to.z + zOffset

    val r = color.red / 255f
    val g = color.green / 255f
    val b = color.blue / 255f
    val a = color.alpha / 255f

    val dx = x2 - x1
    val dy = y2 - y1
    val dz = z2 - z1
    val len = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    val nx = dx / len
    val ny = dy / len
    val nz = dz / len

    ms.push()
    ms.translate(-camPos.x, -camPos.y, -camPos.z)

    val entry = ms.peek()
    val consumer = vcp.getBuffer(RenderLayers.lines())

    consumer.vertex(entry, x1, y1, z1).color(r, g, b, a).normal(entry, nx, ny, nz).lineWidth(width)
    consumer.vertex(entry, x2, y2, z2).color(r, g, b, a).normal(entry, nx, ny, nz).lineWidth(width)

    ms.pop()
}

fun WorldRenderContext.drawDebugBlocks(pos: Vec3i, text: String, color: Color) {
    if (FabricLoader.getInstance().isDevelopmentEnvironment) {
        this.drawOverlay(pos, color)
        this.drawLabel(pos, text, color.withAlpha(1f))
    }
}

fun WorldRenderContext.drawLabel(pos: Vec3i, text: String, color: Color = Color.WHITE) {
    val client = MinecraftClient.getInstance()
    val camPos = this.worldState().cameraRenderState.pos
    val ms = this.matrices()
    val vcp = this.consumers() as VertexConsumerProvider.Immediate

    val x = pos.x + 0.5 - camPos.x
    val y = pos.y + 1.8 - camPos.y
    val z = pos.z + 0.5 - camPos.z

    ms.push()
    ms.translate(x, y, z)

    val yaw = client.gameRenderer.camera.yaw
    val pitch = client.gameRenderer.camera.pitch
    ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-yaw))
    ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch))

    val scale = 0.025f
    ms.scale(-scale, -scale, scale)

    val textRenderer = client.textRenderer
    val orderedText = Text.literal(text).asOrderedText()
    val halfWidth = (-textRenderer.getWidth(orderedText) / 2).toFloat()

    textRenderer.draw(
        orderedText,
        halfWidth,
        0f,
        color.rgb,
        false,
        ms.peek().positionMatrix,
        vcp,
        TextRenderer.TextLayerType.SEE_THROUGH,
        0x40000000,
        0xF000F0
    )

    ms.pop()
}