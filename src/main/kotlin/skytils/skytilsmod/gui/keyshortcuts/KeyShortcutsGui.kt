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
package skytils.skytilsmod.gui.keyshortcuts

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.settings.GameSettings
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.KeyShortcuts
import skytils.skytilsmod.gui.commandaliases.elements.CleanButton
import skytils.skytilsmod.gui.keyshortcuts.elements.KeyShortcutsList
import java.awt.Color

/**
 * Adopted from ChatShortcuts under MIT License
 * https://github.com/P0keDev/ChatShortcuts/blob/master/LICENSE
 * @author P0keDev
 */
class KeyShortcutsGui : GuiScreen() {
    private var keyShortcutsList: KeyShortcutsList? = null
    private var id = 0
    override fun initGui() {
        id = 0
        keyShortcutsList = KeyShortcutsList(mc, width, height - 80, 20, height - 60, 0, 25, width, height)
        buttonList.clear()
        KeyShortcuts.shortcuts.onEach { addShortcut(it.key, it.value) }
        buttonList.add(CleanButton(9000, width / 2 - 220, height - 40, "Save & Exit"))
        buttonList.add(CleanButton(9001, width / 2 + 20, height - 40, "Add Shortcut"))
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id < 1000) {
            keyShortcutsList!!.removeShortcut(button.id)
            buttonList.remove(button)
        }
        if (button.id == 9000) {
            if (mc.thePlayer != null) mc.thePlayer.closeScreen() else mc.displayGuiScreen(null)
        }
        if (button.id == 9001) {
            addBlankShortcut()
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawGradientRect(0, 0, width, height, Color(117, 115, 115, 25).rgb, Color(0, 0, 0, 200).rgb)
        keyShortcutsList!!.drawScreen(mouseX, mouseY, partialTicks)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }


    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyShortcutsList!!.clickedShortcut == null) super.keyTyped(typedChar, keyCode)
        keyShortcutsList!!.keyTyped(typedChar, keyCode)
    }

    override fun updateScreen() {
        super.updateScreen()
        keyShortcutsList!!.updateScreen()
    }


    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        keyShortcutsList!!.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)
        keyShortcutsList!!.mouseReleased(mouseX, mouseY, state)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun onGuiClosed() {
        KeyShortcuts.shortcuts.clear()
        for (e in keyShortcutsList!!.keyShortcuts) {
            if (e.command.isNotEmpty()) {
                KeyShortcuts.shortcuts[e.command] = e.keyCode
            }
        }
        PersistentSave.markDirty<KeyShortcuts>()
    }

    private fun addBlankShortcut() {
        addShortcut("", 0)
    }

    private fun addShortcut(command: String, keyCode: Int) {
        val keyField = GuiTextField(1000 + id, fontRendererObj, width / 2 - 220, 0, 270, 20)
        keyField.text = command
        keyField.maxStringLength = 255
        val keybindingButton = GuiButton(id, width / 2 + 65, 0, 100, 20, GameSettings.getKeyDisplayString(keyCode))
        val removeButton: GuiButton = CleanButton(id, width / 2 + 175, 0, 50, 20, "Remove")
        buttonList.add(removeButton)
        keyShortcutsList!!.addShortcut(id, keyCode, keyField, keybindingButton, removeButton)
        id++
    }
}