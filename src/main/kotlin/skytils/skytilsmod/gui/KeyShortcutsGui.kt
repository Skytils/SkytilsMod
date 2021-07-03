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

package skytils.skytilsmod.gui

import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UKeyboard
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.ChatAllowedCharacters
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.KeyShortcuts
import skytils.skytilsmod.gui.components.SimpleButton
import java.awt.Color

class KeyShortcutsGui : WindowScreen(newGuiScale = 2), ReopenableGUI {

    val scrollComponent: ScrollComponent
    var clickedButton: Entry? = null

    val components = HashMap<UIContainer, Entry>()

    init {
        UIText("Key Shortcuts").childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            height = 14.pixels()
        }

        scrollComponent = ScrollComponent().childOf(window).constrain {
            x = CenterConstraint()
            y = 15.percent()
            width = 90.percent()
            height = 70.percent() + 2.pixels()
        }

        val bottomButtons = UIContainer().childOf(window).constrain {
            x = CenterConstraint()
            y = 90.percent()
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        SimpleButton("Save and Exit").childOf(bottomButtons).constrain {
            x = 0.pixels()
            y = 0.pixels()
        }.onLeftClick {
            if (mc.thePlayer != null) mc.thePlayer.closeScreen() else mc.displayGuiScreen(null)
        }

        SimpleButton("Add Shortcut").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            addNewShortcut()
        }

        KeyShortcuts.shortcuts.onEach {
            addNewShortcut(it.key, it.value)
        }
    }

    private fun addNewShortcut(command: String = "", keyCode: Int = 0) {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = 9.5.percent()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val commandToRun = (UITextInput("Executed Command").childOf(container).constrain {
            x = 5.pixels()
            y = CenterConstraint()
            width = 75.percent()
        }.onLeftClick {
            if (clickedButton == null) grabWindowFocus()
        } as UITextInput).also {
            it.setText(command)
            it.onKeyType { typedChar, keyCode ->
                it.setText(it.getText().filter(ChatAllowedCharacters::isAllowedCharacter).take(255))
            }
        }

        val keybindButton = SimpleButton(GameSettings.getKeyDisplayString(keyCode)).childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            height = 75.percent()
        }

        SimpleButton("Remove").childOf(container).constrain {
            x = 85.percent()
            y = CenterConstraint()
            height = 75.percent()
        }.onLeftClick {
            container.parent.removeChild(container)
            components.remove(container)
        }
        val entry = Entry(container, commandToRun, keybindButton, keyCode)

        keybindButton.onLeftClick {
            clickedButton = entry
        }

        components[container] = entry
    }

    override fun onScreenClose() {
        super.onScreenClose()
        KeyShortcuts.shortcuts.clear()

        for (container in scrollComponent.allChildren) {
            val triple = components[container] ?: throw IllegalStateException("Missing container in map")

            val command = triple.input.getText()
            val keyCode = triple.keyCode
            if (command.isBlank() || keyCode == 0) continue

            KeyShortcuts.shortcuts[command] = keyCode
        }

        PersistentSave.markDirty<KeyShortcuts>()
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        if (clickedButton != null) {
            when {
                keyCode == 1 -> clickedButton!!.keyCode = 0
                keyCode != 0 -> clickedButton!!.keyCode = keyCode
                typedChar.code > 0 -> clickedButton!!.keyCode = typedChar.code + 256
            }
            clickedButton = null
        } else super.onKeyPressed(keyCode, typedChar, modifiers)
    }

    override fun onMouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int) {
        if (clickedButton != null) {
            clickedButton!!.keyCode = -100 + mouseButton
            clickedButton = null
        } else super.onMouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun onTick() {
        super.onTick()
        for (item in components) {
            val button = item.value.button
            val keyCode = item.value.keyCode
            button.text.setText(GameSettings.getKeyDisplayString(keyCode))
            val pressed = clickedButton === item.value
            val reused =
                keyCode != 0 && (mc.gameSettings.keyBindings.any { it.keyCode == keyCode } || components.any { it.value.keyCode != 0 && it !== item && it.value.keyCode == keyCode })
            if (pressed) {
                button.text.setText("§f> §e${button.text.getText()}§f <")
            } else if (reused) {
                button.text.setText("§c${button.text.getText()}")
            }
        }
    }

    data class Entry(val container: UIContainer, val input: UITextInput, val button: SimpleButton, var keyCode: Int)
}