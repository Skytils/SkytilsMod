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
package skytils.skytilsmod.gui.keyshortcuts.elements

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.client.GuiScrollingList

/**
 * Adopted from ChatShortcuts under MIT License
 * https://github.com/P0keDev/ChatShortcuts/blob/master/LICENSE
 * @author P0keDev
 */
class KeyShortcutsList(
    private val mc: Minecraft, width: Int, height: Int, top: Int, bottom: Int, left: Int, entryHeight: Int,
    screenWidth: Int, screenHeight: Int
) : GuiScrollingList(mc, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight) {
    val keyShortcuts: ArrayList<KeyShortcutEntry> = ArrayList()
    var clickedShortcut: KeyShortcutEntry? = null
    fun addShortcut(id: Int, keyCode: Int, command: GuiTextField, keyButton: GuiButton, removeButton: GuiButton) {
        keyShortcuts.add(KeyShortcutEntry(id, keyCode, command, keyButton, removeButton))
    }

    fun removeShortcut(id: Int) {
        for (e in keyShortcuts) {
            if (e.id == id) {
                keyShortcuts.remove(e)
                return
            }
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        if (clickedShortcut != null) {
            if (keyCode == 1) {
                clickedShortcut!!.keyCode = 0
            } else if (keyCode != 0) {
                clickedShortcut!!.keyCode = keyCode
            } else if (typedChar.code > 0) {
                clickedShortcut!!.keyCode = typedChar.code + 256
            }
            clickedShortcut = null
        } else {
            for (e in keyShortcuts) {
                e.commandField.textboxKeyTyped(typedChar, keyCode)
            }
        }
    }

    fun updateScreen() {
        for (e in keyShortcuts) {
            e.commandField.updateCursorCounter()
            e.keyButton.displayString = GameSettings.getKeyDisplayString(e.keyCode)
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (clickedShortcut != null) {
            clickedShortcut!!.keyCode = -100 + mouseButton
            clickedShortcut = null
        } else {
            for (e in keyShortcuts) {
                e.commandField.mouseClicked(mouseX, mouseY, mouseButton)
                if (e.keyButton.mousePressed(mc, mouseX, mouseY)) {
                    clickedShortcut = e
                }
            }
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}
    private fun resetButtons() {
        for (e in keyShortcuts) {
            e.keyButton.visible = false
            e.removeButton.visible = false
        }
    }

    override fun getSize(): Int {
        return keyShortcuts.size
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
        val ks = keyShortcuts[slotIdx]
        val visible = slotTop >= top && slotTop + slotHeight <= bottom
        ks.keyButton.visible = visible
        ks.removeButton.visible = visible
        if (visible) {
            ks.commandField.yPosition = slotTop
            ks.keyButton.yPosition = slotTop
            ks.removeButton.yPosition = slotTop
            ks.commandField.drawTextBox()
            ks.keyButton.displayString = GameSettings.getKeyDisplayString(ks.keyCode)
            val pressed = clickedShortcut === ks
            var reused = false
            if (ks.keyCode != 0) {
                for (keybinding in mc.gameSettings.keyBindings) {
                    if (keybinding.keyCode == ks.keyCode) {
                        reused = true
                        break
                    }
                }
                if (!reused) {
                    for (entry in keyShortcuts) {
                        if (entry.keyCode != 0 && entry !== ks && entry.keyCode == ks.keyCode) {
                            reused = true
                            break
                        }
                    }
                }
            }
            if (pressed) {
                ks.keyButton.displayString =
                    EnumChatFormatting.WHITE.toString() + "> " + EnumChatFormatting.YELLOW + ks.keyButton.displayString + EnumChatFormatting.WHITE + " <"
            } else if (reused) {
                ks.keyButton.displayString = EnumChatFormatting.RED.toString() + ks.keyButton.displayString
            }
            ks.keyButton.drawButton(mc, mouseX, mouseY)
            mc.fontRendererObj.drawString(
                ":",
                ks.commandField.xPosition + ks.commandField.width + 6,
                slotTop + 5,
                0xFFFFFF
            )
        }
    }

    override fun drawGradientRect(left: Int, top: Int, right: Int, bottom: Int, color1: Int, color2: Int) {}
    class KeyShortcutEntry(
        val id: Int,
        var keyCode: Int,
        val commandField: GuiTextField,
        val keyButton: GuiButton,
        val removeButton: GuiButton
    ) {
        val command: String
            get() = commandField.text
    }

}