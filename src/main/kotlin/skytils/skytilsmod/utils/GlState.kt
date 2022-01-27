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

package skytils.skytilsmod.utils

import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GLContext
import java.nio.ByteBuffer

object GlState {
    var lightingState = false
    var blendState = false
    var blendSrc = 0
    var blendDst = 0
    var blendAlphaSrc = 1
    var blendAlphaDst = 0
    var alphaState = false
    var depthState = false
    var colorState = ByteBuffer.allocateDirect(64).asFloatBuffer()

    val newBlend: Boolean

    init {
        val context = GLContext.getCapabilities()
        newBlend = context.OpenGL14 || context.GL_EXT_blend_func_separate
    }

    fun pushState() {
        lightingState = GL11.glIsEnabled(GL11.GL_LIGHTING)
        blendState = GL11.glIsEnabled(GL11.GL_BLEND)
        blendSrc = GL11.glGetInteger(GL11.GL_BLEND_SRC)
        blendDst = GL11.glGetInteger(GL11.GL_BLEND_DST)
        alphaState = GL11.glIsEnabled(GL11.GL_ALPHA_TEST)
        if (newBlend) {
            blendAlphaSrc = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA)
            blendAlphaDst = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA)
        }
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, colorState)
    }

    fun popState() {
        if (depthState) GlStateManager.enableDepth()
        else GlStateManager.disableDepth()

        if (blendState) GlStateManager.enableBlend()
        else GlStateManager.disableBlend()

        if (alphaState) GlStateManager.enableAlpha()
        else GlStateManager.disableAlpha()

        GlStateManager.tryBlendFuncSeparate(blendSrc, blendDst, blendAlphaSrc, blendAlphaDst)
        GlStateManager.color(colorState.get(0), colorState.get(1), colorState.get(2), colorState.get(3))
    }
}