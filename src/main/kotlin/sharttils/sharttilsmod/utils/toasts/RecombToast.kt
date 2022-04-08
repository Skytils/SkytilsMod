/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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

package sharttils.sharttilsmod.utils.toasts

import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.utils.ItemUtil
import sharttils.sharttilsmod.utils.RenderUtil
import java.util.regex.Pattern

class RecombToast(private val recombItem: String) : IToast<KeyToast> {
    private val buffer = GLAllocation.createDirectFloatBuffer(16)
    private val maxDrawTime: Long = Sharttils.config.toastTime.toLong()
    override fun draw(toastGui: GuiToast, delta: Long): IToast.Visibility {
        toastGui.mc.textureManager.bindTexture(TEXTURE)
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, 160, 32, 160f, 32f)
        toastGui.mc.fontRendererObj.drawStringWithShadow("\u00a76§lAuto-Recombob Item!", 30f, 7f, 16777215)
        GuiToast.drawSubline(toastGui, delta, 0L, maxDrawTime, buffer, "§6§k§la§r §$recombItem §r§6§k§la", false)
        RenderHelper.enableGUIStandardItemLighting()
        RenderUtil.renderItem(RECOMB, 8, 8)
        GlStateManager.disableLighting()
        return if (delta >= maxDrawTime) IToast.Visibility.HIDE else IToast.Visibility.SHOW
    }

    companion object {
        private val TEXTURE = ResourceLocation("sharttils:gui/toast.png")
        private val RECOMB = ItemUtil.setSkullTexture(
            ItemStack(Items.skull, 1, 3),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRmZjhkYmJhYjE1YmZiYjExZTIzYjFmNTBiMzRlZjU0OGFkOTgzMmMwYmQ3ZjVhMTM3OTFhZGFkMDA1N2UxYiJ9fX0K",
            "10479f18-e67f-3c86-93e2-b4df79d0457e"
        )
        val pattern: Pattern = Pattern.compile(" §r§([\\w ]+)§r§e")
    }

}