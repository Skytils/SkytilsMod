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

package gg.skytils.skytilsmod.gui.features

import gg.essential.elementa.ElementaVersion
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
import gg.essential.vigilance.gui.settings.CheckboxComponent
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.KeyShortcuts
import gg.skytils.skytilsmod.gui.ReopenableGUI
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.multiplatform.isValidChar
import java.awt.Color

class KeyShortcutsGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent
    private var clickedButton: Entry? = null

    private val components = HashMap<UIContainer, Entry>()

    init {
        UIText("Key Shortcuts").childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            height = 14.pixels()
        }

        scrollComponent = ScrollComponent(
            innerPadding = 4f,
        ).childOf(window).constrain {
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
            displayScreen(null)
        }

        SimpleButton("Add Shortcut").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            addNewShortcut()
        }

        KeyShortcuts.shortcuts.sortedBy(KeyShortcuts.KeybindShortcut::message).forEach {
            addNewShortcut(it.message, it.keyCode, it.modifiers, it.enabled)
        }
    }

    private fun addNewShortcut(command: String = "", keyCode: Int = 0, modifiers: Int = 0, enabled: Boolean = true) {
        val modifiersList = KeyShortcuts.Modifiers.fromBitfield(modifiers)
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = 9.5.percent()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val toggle = CheckboxComponent(enabled).childOf(container).constrain {
            x = 5.pixels()
            y = CenterConstraint()
        }

        val commandToRun = (UITextInput("Executed Command").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 70.percent()
        }.onLeftClick {
            if (clickedButton == null) grabWindowFocus()
        } as UITextInput).also {
            it.setText(command)
            it.onKeyType { _, _ ->
                it.setText(it.getText().filter(::isValidChar).take(256))
            }
        }

        val keybindButton = SimpleButton("placeholder").childOf(container).constrain {
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
        val entry =
            Entry(container, commandToRun, keybindButton, keyCode, modifiersList, toggle)

        keybindButton.onLeftClick {
            clickedButton = entry
        }

        components[container] = entry
    }

    override fun onScreenClose() {
        super.onScreenClose()
        KeyShortcuts.shortcuts.clear()

        for ((_, entry) in components) {
            val command = entry.input.getText()
            val keyCode = entry.keyCode
            if (command.isBlank() || keyCode == 0) continue

            KeyShortcuts.shortcuts.add(KeyShortcuts.KeybindShortcut(command, keyCode, entry.modifiers, entry.toggle.checked))
        }

        PersistentSave.markDirty<KeyShortcuts>()
    }

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        if (clickedButton != null) {
            val extra =
                if (modifiers != null) KeyShortcuts.Modifiers.fromUCraft(modifiers)
                else KeyShortcuts.Modifiers.getPressed()
            when {
                keyCode == UKeyboard.KEY_ESCAPE -> {
                    clickedButton!!.keyCode = 0
                    clickedButton!!.modifiers = emptyList()
                }

                keyCode != 0 -> {
                    clickedButton!!.keyCode = keyCode
                    clickedButton!!.modifiers = extra
                }

                typedChar.code > 0 -> {
                    clickedButton!!.keyCode =
                        //#if MC==10809
                        typedChar.code + 256
                        //#else
                        typedChar.uppercaseChar().code
                        //#endif
                    clickedButton!!.modifiers = extra
                }
            }
            clickedButton = null
        } else super.onKeyPressed(keyCode, typedChar, modifiers)
    }

    override fun onMouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int) {
        if (clickedButton != null) {
            clickedButton!!.keyCode = -100 + mouseButton
            clickedButton!!.modifiers = KeyShortcuts.Modifiers.getPressed()
            clickedButton = null
        } else super.onMouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun onTick() {
        super.onTick()
        for ((_, entry) in components) {
            val button = entry.button
            val keyCode = entry.keyCode
            button.text.setText(entry.getDisplayString())
            val pressed = clickedButton === entry
            val reused =
                keyCode != 0 && (client?.options?.allKeys?.any { it.equals(entry) } == true || components.any { it.value.keyCode != 0 && it.value !== entry && it.value.keyCode == keyCode })
            if (pressed) {
                button.text.setText("§f> §e${button.text.getText()}§f <")
            } else if (reused) {
                button.text.setText("§c${button.text.getText()}")
            }
        }
    }

    data class Entry(
        val container: UIContainer,
        val input: UITextInput,
        val button: SimpleButton,
        var keyCode: Int,
        var modifiers: List<KeyShortcuts.Modifiers>,
        val toggle: CheckboxComponent
    ) {
        fun getDisplayString() =
            "${
                if (modifiers.isNotEmpty()) modifiers.joinToString(
                    "+",
                    postfix = " + "
                ) { it.shortName } else ""
            }${Utils.getKeyDisplayStringSafe(keyCode)}"
    }
}