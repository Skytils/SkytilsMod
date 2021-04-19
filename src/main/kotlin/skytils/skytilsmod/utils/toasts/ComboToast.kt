/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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
package skytils.skytilsmod.utils.toasts

import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.util.ResourceLocation
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.utils.RenderUtil
import java.util.regex.Pattern

class ComboToast(input: String) : IToast<ComboToast> {
    private val buffer = GLAllocation.createDirectFloatBuffer(16)
    private val maxDrawTime: Long = Skytils.config.toastTime.toLong()
    private var length: String? = null
    private var buff: String? = null
    private var buffTexture: ResourceLocation? = ResourceLocation("skytils:combo/comboFail.png")
    override fun draw(toastGui: GuiToast, delta: Long): IToast.Visibility {
        toastGui.mc.textureManager.bindTexture(TEXTURE)
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, 160, 32, 160f, 32f)
        toastGui.mc.fontRendererObj.drawStringWithShadow(length, 30f, 7f, 16777215)
        GuiToast.drawSubline(toastGui, delta, 0L, maxDrawTime, buffer, buff, false)
        RenderHelper.enableGUIStandardItemLighting()
        if (buffTexture != null) RenderUtil.renderTexture(buffTexture, 8, 8)
        GlStateManager.disableLighting()
        return if (delta >= maxDrawTime) IToast.Visibility.HIDE else IToast.Visibility.SHOW
    }

    companion object {
        private val TEXTURE = ResourceLocation("skytils:gui/toast.png")
        private val comboPattern = Pattern.compile("(§r§.§l)\\+(\\d+ Kill Combo) (§r§8.+)")
        private val noBuffPattern = Pattern.compile("(§r§.§l)\\+(\\d+ Kill Combo)")
    }

    init {
        val comboMatcher = comboPattern.matcher(input)
        if (comboMatcher.find()) {
            length = comboMatcher.group(1) + comboMatcher.group(2)
            buff = comboMatcher.group(3)
            when {
                input.contains("Magic Find") -> {
                    buffTexture = ResourceLocation("skytils:toasts/combo/luck.png")
                }
                input.contains("coins per kill") -> {
                    buffTexture = ResourceLocation("skytils:toasts/combo/coin.png")
                }
                input.contains("Combat Exp") -> {
                    buffTexture = ResourceLocation("skytils:toasts/combo/combat.png")
                }
            }
        } else {
            buffTexture = null
            val noBuffMatcher = noBuffPattern.matcher(input)
            if (noBuffMatcher.find()) {
                length = noBuffMatcher.group(1) + noBuffMatcher.group(2)
            }
        }
    }
}