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
package skytils.skytilsmod.gui.commandaliases

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import skytils.skytilsmod.features.impl.handlers.CommandAliases
import skytils.skytilsmod.gui.commandaliases.elements.CleanButton
import skytils.skytilsmod.gui.commandaliases.elements.ScrollingCommandAliasesList
import java.awt.Color
import java.io.IOException

/**
 * Taken from ChatShortcuts under MIT License
 * https://github.com/P0keDev/ChatShortcuts/blob/master/LICENSE
 * @author P0keDev
 */
class CommandAliasesGui : GuiScreen() {
    private var scrollingCommandAliasesList: ScrollingCommandAliasesList? = null
    private var id = 0
    override fun initGui() {
        id = 0
        scrollingCommandAliasesList =
            ScrollingCommandAliasesList(mc, width, height - 80, 20, height - 60, 0, 25, width, height)
        buttonList.clear()
        for ((key, value) in CommandAliases.aliases) {
            addAlias(key, value)
        }
        buttonList.add(CleanButton(9000, width / 2 - 220, height - 40, "Save & Exit"))
        buttonList.add(CleanButton(9001, width / 2 + 20, height - 40, "Add Alias"))
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id < 1000) {
            scrollingCommandAliasesList!!.removeAlias(button.id)
            buttonList.remove(button)
        }
        if (button.id == 9000) {
            if (mc.thePlayer != null) mc.thePlayer.closeScreen() else mc.displayGuiScreen(null)
        }
        if (button.id == 9001) {
            addAlias()
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawGradientRect(0, 0, width, height, Color(117, 115, 115, 25).rgb, Color(0, 0, 0, 200).rgb)
        scrollingCommandAliasesList!!.drawScreen(mouseX, mouseY, partialTicks)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
        scrollingCommandAliasesList!!.keyTyped(typedChar, keyCode)
    }

    override fun updateScreen() {
        super.updateScreen()
        scrollingCommandAliasesList!!.updateScreen()
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        scrollingCommandAliasesList!!.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun onGuiClosed() {
        CommandAliases.aliases.clear()
        for (e in scrollingCommandAliasesList!!.aliases) {
            if (e.key.text.isNotEmpty()) {
                CommandAliases.aliases[e.key.text] = e.message.text
            }
        }
        CommandAliases.saveAliases()
    }

    private fun addAlias(key: String = "", message: String = "") {
        val keyField = GuiTextField(1000 + id, fontRendererObj, width / 2 - 220, 0, 100, 20)
        keyField.text = key
        val messageField = GuiTextField(2000 + id, fontRendererObj, width / 2 - 100, 0, 270, 20)
        messageField.maxStringLength = 255
        messageField.text = message
        val removeButton: GuiButton = CleanButton(id, width / 2 + 175, 0, 50, 20, "Remove")
        buttonList.add(removeButton)
        scrollingCommandAliasesList!!.addAlias(id, keyField, messageField, removeButton)
        id++
    }
}