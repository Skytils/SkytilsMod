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

package gg.skytils.skytilsmod.utils.cheats

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorMinecraft
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.cheats.RenderUtils.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object Nametags {
    public fun renderNameTag(entity: EntityLivingBase, tag: String) {

        // Set fontrenderer local
        val fontRenderer = mc.fontRendererObj

        // Push
        glPushMatrix()

        // Translate to player position
        val timer = (mc as AccessorMinecraft).timer

        glTranslated( // Translate to player position with render pos and interpolate it
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - RenderUtil.getRenderX(),
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - RenderUtil.getRenderY() + entity.eyeHeight.toDouble() + 0.55,
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - RenderUtil.getRenderZ()
        )

        // Rotate view to player
        glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)

        // Scale
        var distance = mc.thePlayer.getDistanceToEntity(entity) / 4F

        if (distance < 1F) {
            distance = 1F
        }

        val scale = (distance / 150F)

        // Disable lightning and depth test
        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)

        // Enable blend
        enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Draw nametag
        // colors
        var hpBarColor = Color(255, 255, 255, 100)
        val name = entity.displayName.unformattedText
        if (name.startsWith("§")) {
            hpBarColor = ColorUtils.colorCode(name.substring(1, 2), 100)
        }
        val bgColor = Color(50, 50, 50, 100)
        val width = fontRenderer.getStringWidth(tag) / 2
        val maxWidth = (width + 4F) - (-width - 4F)
        var healthPercent = entity.health / entity.maxHealth

        // render bg
        glScalef(-scale * 2, -scale * 2, scale * 2)
        drawRect(-width - 4F, -fontRenderer.FONT_HEIGHT * 3F, width + 4F, -3F, bgColor)

        // render hp bar
        if (healthPercent > 1) {
            healthPercent = 1F
        }

        drawRect(-width - 4F, -3F, (-width - 4F) + (maxWidth * healthPercent), 1F, hpBarColor)
        drawRect((-width - 4F) + (maxWidth * healthPercent), -3F, width + 4F, 1F, bgColor)

        // string
        fontRenderer.drawString(tag, -width, -fontRenderer.FONT_HEIGHT * 2 - 4, Color.WHITE.rgb)
        glScalef(0.5F, 0.5F, 0.5F)
        fontRenderer.drawString(
            "Health: " + entity.health.toInt(),
            -width * 2,
            -fontRenderer.FONT_HEIGHT * 2,
            Color.WHITE.rgb
        )


        // Reset caps
        resetCaps()

        // Reset color
        resetColor()
        glColor4f(1F, 1F, 1F, 1F)

        // Pop
        glPopMatrix()
    }
}