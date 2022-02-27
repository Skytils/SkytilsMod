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
package skytils.skytilsmod.utils.toasts

import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.util.ResourceLocation
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.utils.RenderUtil

class ReviveStoneToast : IToast<SuperboomToast> {
    private val buffer = GLAllocation.createDirectFloatBuffer(16)
    private val maxDrawTime: Long = Skytils.config.toastTime.toLong()
    override fun draw(toastGui: GuiToast, delta: Long): IToast.Visibility {
        toastGui.mc.textureManager.bindTexture(TEXTURE)
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, 160, 32, 160f, 32f)
        toastGui.mc.fontRendererObj.drawStringWithShadow("§6Revive Stone", 30f, 7f, 16777215)
        GuiToast.drawSubline(toastGui, delta, 0L, maxDrawTime, buffer, "", false)
        RenderHelper.enableGUIStandardItemLighting()
        RenderUtil.renderTexture(REVIVE, 8, 8)
        GlStateManager.disableLighting()
        return if (delta >= maxDrawTime) IToast.Visibility.HIDE else IToast.Visibility.SHOW
    }

    companion object {
        private val TEXTURE = ResourceLocation("skytils:gui/toast.png")
        private val REVIVE = ResourceLocation("skytils:toasts/revive.png")
    }

}