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
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.ResourceLocation
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.utils.RenderUtil

class BlessingToast(blessing: String, private val buffs: List<BlessingBuff>) : IToast<BlessingToast> {
    private val buffer = GLAllocation.createDirectFloatBuffer(16)
    private val maxDrawTime: Long = Sharttils.config.toastTime.toLong()
    private val blessing: Blessing? = Blessing.fromName(blessing)
    override fun draw(toastGui: GuiToast, delta: Long): IToast.Visibility {
        toastGui.mc.textureManager.bindTexture(TEXTURE)
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0f, 0f, 160, 32, 160f, 32f)
        RenderHelper.disableStandardItemLighting()
        toastGui.mc.fontRendererObj.drawStringWithShadow(blessing!!.formattedName, 30f, 7f, 16777215)
        val buffStats = StringBuilder()
        for (buff in buffs) {
            val color: String = when (buff.symbol) {
                "❤", "❁" -> EnumChatFormatting.RED.toString()
                "✎" -> EnumChatFormatting.AQUA.toString()
                "❈", "HP" -> EnumChatFormatting.GREEN.toString()
                "✦" -> EnumChatFormatting.WHITE.toString()
                "☠" -> EnumChatFormatting.BLUE.toString()
                else -> EnumChatFormatting.GRAY.toString()
            }
            buffStats.append(color).append(buff.amount).append(buff.symbol).append(" ")
        }
        GuiToast.drawSubline(toastGui, delta, 0L, maxDrawTime, buffer, buffStats.toString(), false)
        RenderHelper.enableGUIStandardItemLighting()
        RenderUtil.renderTexture(blessing.texture, 8, 8)
        GlStateManager.disableLighting()
        return if (delta >= maxDrawTime) IToast.Visibility.HIDE else IToast.Visibility.SHOW
    }

    private enum class Blessing(val blessingName: String, location: String, val formattedName: String) {
        LIFE("life", "sharttils:toasts/blessings/life.png", "§c§lLIFE BLESSING!"), POWER(
            "power",
            "sharttils:toasts/blessings/power.png",
            "§5§lPOWER BLESSING!"
        ),
        STONE("stone", "sharttils:toasts/blessings/stone.png", "§a§lSTONE BLESSING!"), WISDOM(
            "wisdom",
            "sharttils:toasts/blessings/wisdom.png",
            "§b§lWISDOM BLESSING!"
        ),
        TIME("time", "sharttils:toasts/blessings/time.png", "§6§lTIME BLESSING!");

        val texture: ResourceLocation = ResourceLocation(location)

        companion object {
            fun fromName(name: String): Blessing? {
                for (type in values()) {
                    if (type.blessingName == name) return type
                }
                return null
            }
        }

    }

    class BlessingBuff(var amount: String, var symbol: String)
    companion object {
        private val TEXTURE = ResourceLocation("sharttils:gui/toast.png")
    }

}