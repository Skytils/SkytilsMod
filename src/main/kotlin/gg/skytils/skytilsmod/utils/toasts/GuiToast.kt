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
package gg.skytils.skytilsmod.utils.toasts

import gg.essential.universal.UResolution
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.util.MathHelper
import org.lwjgl.opengl.GL11
import java.nio.Buffer
import java.nio.FloatBuffer
import java.util.*

/**
 * Taken from Skyblockcatia under MIT License
 * Modified
 * https://github.com/SteveKunG/SkyBlockcatia/blob/1.8.9/LICENSE.md
 *
 * @author SteveKunG
 */
class GuiToast(val mc: Minecraft) : Gui() {
    private val visible = arrayOfNulls<ToastInstance<*>?>(5)
    private val toastsQueue: Deque<IToast<*>> = ArrayDeque()
    fun drawToast(resolution: ScaledResolution) {
        RenderHelper.disableStandardItemLighting()
        for (i in visible.indices) {
            val toastinstance = visible[i]
            if (toastinstance != null && toastinstance.render(resolution.scaledWidth, i)) {
                visible[i] = null
            }
            if (visible[i] == null && !toastsQueue.isEmpty()) {
                visible[i] = ToastInstance<IToast<*>>(toastsQueue.removeFirst())
            }
        }
    }

    @Suppress("unchecked_cast")
    fun <T : IToast<*>> getToast(clazz: Class<out T>, obj: Any): T? {
        for (ins in visible) {
            if (ins != null && clazz.isAssignableFrom(ins.toast!!.javaClass) && ins.toast.getType() == obj) {
                return ins.toast as T
            }
        }
        for (toast in toastsQueue) {
            if (clazz.isAssignableFrom(toast.javaClass) && toast.getType() == obj) {
                return toast as T
            }
        }
        return null
    }

    fun clear() {
        Arrays.fill(visible, null)
        toastsQueue.clear()
    }

    fun add(toast: IToast<*>): Boolean {
        return toastsQueue.add(toast)
    }

    internal inner class ToastInstance<T : IToast<*>?>(toast: T) {
        val toast: T
        private var animationTime: Long
        private var visibleTime: Long
        private var visibility: IToast.Visibility?
        private fun getVisibility(delta: Long): Float {
            var f = MathHelper.clamp_float((delta - animationTime) / 600.0f, 0.0f, 1.0f)
            f *= f
            return if (visibility == IToast.Visibility.HIDE) 1.0f - f else f
        }

        fun render(x: Int, z: Int): Boolean {
            val i = Minecraft.getSystemTime()
            if (animationTime == -1L) {
                animationTime = i
            }
            if (visibility == IToast.Visibility.SHOW && i - animationTime <= 600L) {
                visibleTime = i
            }
            GlStateManager.pushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.enableAlpha()
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
            GlStateManager.translate(x - 160.0f * getVisibility(i), (z * 32).toFloat(), (500 + z).toFloat())
            val toastVisibility = toast!!.draw(this@GuiToast, i - visibleTime)
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()
            if (toastVisibility != visibility) {
                animationTime = i - ((1.0f - getVisibility(i)) * 600.0f).toInt()
                visibility = toastVisibility
            }
            return visibility == IToast.Visibility.HIDE && i - animationTime > 600L
        }

        init {
            animationTime = -1L
            visibleTime = -1L
            visibility = IToast.Visibility.SHOW
            this.toast = toast
        }
    }

    companion object {
        fun drawSubline(
            toastGui: GuiToast,
            delta: Long,
            firstDrawTime: Long,
            maxDrawTime: Long,
            buffer: FloatBuffer,
            subLine: String?,
            shadow: Boolean
        ) {
            val minDraw = (maxDrawTime * 0.1).toLong()
            val maxDraw = maxDrawTime + 500L
            val backwardDraw = (maxDrawTime * 0.5).toLong()
            val textSpeed = 1500L + (maxDrawTime * 0.1).toLong()
            var x = 30
            val textWidth = toastGui.mc.fontRendererObj.getStringWidth(subLine)
            val maxSize = textWidth - 135
            val timeElapsed = delta - firstDrawTime - minDraw
            val timeElapsed2 = maxDraw - delta - backwardDraw
            val maxTextLength = 125
            if (textWidth > maxSize && textWidth > maxTextLength) {
                if (timeElapsed > 0) {
                    x = (-textWidth * timeElapsed / textSpeed + x).toInt().coerceAtLeast(-maxSize + 16)
                }
                val backward =
                    (-(textWidth * timeElapsed2 / textSpeed)).toInt().coerceAtMost(30).coerceAtLeast(-maxSize + 16)
                if (timeElapsed > timeElapsed2) {
                    x = backward
                }
            }
            val res = UResolution
            val height = res.scaledHeight.toDouble()
            val scale = res.scaleFactor
            val trans = FloatArray(16)
            (buffer as Buffer).clear()
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer)
            buffer[trans]
            val xpos = trans[12]
            GL11.glEnable(GL11.GL_SCISSOR_TEST)
            GL11.glScissor(
                ((xpos + 29) * scale).toInt(),
                ((height - 196) * scale).toInt(),
                (126 * scale).toInt(),
                (195 * scale).toInt()
            )
            if (shadow) {
                toastGui.mc.fontRendererObj.drawStringWithShadow(subLine, x.toFloat(), 18f, 0xFFFFFF)
            } else {
                toastGui.mc.fontRendererObj.drawString(subLine, x, 18, 0xFFFFFF)
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }
    }
}