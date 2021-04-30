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
package skytils.skytilsmod.gui.commandaliases.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.renderer.Tessellator
import net.minecraftforge.fml.client.GuiScrollingList

/**
 * Taken from ChatShortcuts under MIT License
 * https://github.com/P0keDev/ChatShortcuts/blob/master/LICENSE
 * @author P0keDev
 */
class ScrollingCommandAliasesList(
    mc: Minecraft, width: Int, height: Int, top: Int, bottom: Int, left: Int, entryHeight: Int,
    screenWidth: Int, screenHeight: Int
) : GuiScrollingList(mc, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight) {
    private val mc: Minecraft
    val aliases: ArrayList<AliasListEntry>
    fun addAlias(id: Int, key: GuiTextField, message: GuiTextField, removeButton: GuiButton) {
        aliases.add(AliasListEntry(id, key, message, removeButton))
    }

    fun removeAlias(id: Int) {
        for (e in aliases) {
            if (e.id == id) {
                aliases.remove(e)
                return
            }
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        for (e in aliases) {
            e.key.textboxKeyTyped(typedChar, keyCode)
            e.message.textboxKeyTyped(typedChar, keyCode)
        }
    }

    fun updateScreen() {
        for (e in aliases) {
            e.key.updateCursorCounter()
            e.message.updateCursorCounter()
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (e in aliases) {
            e.key.mouseClicked(mouseX, mouseY, mouseButton)
            e.message.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    private fun resetButtons() {
        for (e in aliases) {
            e.removeButton.visible = false
        }
    }

    override fun getSize(): Int {
        return aliases.size
    }

    override fun elementClicked(index: Int, doubleClick: Boolean) {}
    override fun isSelected(index: Int): Boolean {
        return false
    }

    override fun drawBackground() {}
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        resetButtons()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun drawSlot(slotIdx: Int, entryRight: Int, slotTop: Int, slotBuffer: Int, tess: Tessellator) {
        val alias = aliases[slotIdx]
        val visible = slotTop >= top && slotTop + slotHeight <= bottom
        alias.removeButton.visible = visible
        if (visible) {
            alias.key.yPosition = slotTop
            alias.message.yPosition = slotTop
            alias.removeButton.yPosition = slotTop
            alias.key.drawTextBox()
            alias.message.drawTextBox()
            mc.fontRendererObj.drawString(":", alias.key.xPosition + alias.key.width + 10, slotTop + 5, 0xFFFFFF)
        }
    }

    override fun drawGradientRect(left: Int, top: Int, right: Int, bottom: Int, color1: Int, color2: Int) {}
    class AliasListEntry(val id: Int, val key: GuiTextField, val message: GuiTextField, val removeButton: GuiButton)

    init {
        aliases = ArrayList()
        this.mc = mc
    }
}