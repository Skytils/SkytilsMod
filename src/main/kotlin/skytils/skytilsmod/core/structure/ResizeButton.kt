package skytils.skytilsmod.core.structure

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.graphics.colors.CommonColors

class ResizeButton(var x: Float, var y: Float, var element: GuiElement, val corner: Corner) :
    GuiButton(-1, 0, 0, null) {
    private var cornerOffsetX = 0f
    private var cornerOffsetY = 0f
    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int) {
        val scale = element.scale
        hovered = mouseX >= x && mouseY >= y && mouseX < x + SIZE * 2f * scale && mouseY < y + SIZE * 2f * scale
        val color = if (hovered) CommonColors.WHITE.toInt() else CommonColors.WHITE.toInt(70)
        RenderUtil.drawRect(0.0, 0.0, (SIZE * 2).toDouble(), (SIZE * 2).toDouble(), color)
    }

    override fun mousePressed(mc: Minecraft, mouseX: Int, mouseY: Int): Boolean {
        val sr = ScaledResolution(mc)
        val minecraftScale = sr.scaleFactor.toFloat()
        val floatMouseX = Mouse.getX() / minecraftScale
        val floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale
        cornerOffsetX = floatMouseX
        cornerOffsetY = floatMouseY
        return hovered
    }

    enum class Corner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    companion object {
        const val SIZE = 2
    }
}