/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.gui

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
import gg.essential.elementa.effects.RecursiveFadeEffect
import gg.essential.universal.UGraphics
import gg.essential.vigilance.gui.settings.CheckboxComponent
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.SpiritLeap
import gg.skytils.skytilsmod.gui.components.SimpleButton
import skytils.hylin.skyblock.dungeons.DungeonClass
import java.awt.Color

class SpiritLeapNamesGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent
    private val classCheckboxes = HashMap<DungeonClass, UIContainer>()

    init {
        UIText("Spirit Leap Names").childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            height = 14.pixels()
        }

        scrollComponent = ScrollComponent(
            innerPadding = 4f,
        ).childOf(window).constrain {
            x = RelativeConstraint(0.1f)
            y = RelativeConstraint(0.15f)
            width = 80.percent()
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
            mc.displayGuiScreen(null)
        }

        SimpleButton("Add Username").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            addNewName()
        }

        val checkboxes = UIContainer().childOf(window).constrain {
            x = 6.pixels()
            y = CenterConstraint()
            width = basicWidthConstraint { c ->
                c.children.maxOf { it.getWidth() } + 20
            }
            height = ChildBasedSizeConstraint()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))
        val longestTextConstraint =
            (DungeonClass.entries.maxOf { UGraphics.getStringWidth(it.className) } + 7).pixels()
        for ((index, dClass) in DungeonClass.entries.withIndex()) {

            val text = UIText(dClass.className).constrain {
                x = 0.pixels()
                y = CenterConstraint()
            }
            val checkbox = CheckboxComponent(SpiritLeap.classes.getOrDefault(dClass, false)).constrain {
                x = longestTextConstraint
                y = CenterConstraint()
            }

            val container = UIContainer().childOf(checkboxes).addChildren(text, checkbox).constrain {
                x = 4.pixels()
                y = RelativeConstraint(1f / DungeonClass.entries.size * index)
                width = longestTextConstraint + checkbox.getWidth().pixels()
                height = ChildBasedSizeConstraint()
            } as UIContainer

            classCheckboxes[dClass] = container
        }

        for (name in SpiritLeap.names) {
            addNewName(name.key, name.value)
        }
    }

    private fun addNewName(name: String = "", enabled: Boolean = true) {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = 9.5.percent()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val textBox = UITextInput("Add IGN Here").childOf(container).constrain {
            x = 5.pixels()
            y = CenterConstraint()
            width = 50.percent()
        }.also {
            it.setText(name)
            it.onKeyType { _, _ ->
                it.setText(it.getText().filter { c -> c.isLetterOrDigit() || c == '_' }.take(16))
            }
        }

        SimpleButton("Remove").childOf(container).constrain {
            x = 70.percent()
            y = CenterConstraint()
            height = 75.percent()
        }.onLeftClick {
            container.parent.removeChild(container)
        }

        val enabledButton = SimpleButton(if (enabled) "Enabled" else "Disabled").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            height = 75.percent()
        }.onLeftClick {
            if ((this as SimpleButton).text.getText() == "Enabled") {
                this.effect(RecursiveFadeEffect())
                this.text.setText("Disabled")
            } else {
                this.effects.clear()
                this.text.setText("Enabled")
            }
        }

        if (!enabled) enabledButton.effect(RecursiveFadeEffect())

        container.onLeftClick {
            textBox.grabWindowFocus()
        }
    }

    override fun onScreenClose() {
        super.onScreenClose()
        SpiritLeap.names.clear()
        SpiritLeap.classes.clear()

        for (entry in classCheckboxes) {
            val checkbox = entry.value.childrenOfType<CheckboxComponent>().first()
            SpiritLeap.classes[entry.key] = checkbox.checked
        }

        for (container in scrollComponent.allChildren) {
            val text = container.childrenOfType<UITextInput>().firstOrNull()
                ?: throw IllegalStateException("${container.componentName} does not have a UITextInput which cannot be missing! Available children ${container.children.map { it.componentName }}")
            val button =
                container.childrenOfType<SimpleButton>().find { it.t != "Remove" }
                    ?: throw IllegalStateException("Button cannot be missing!")
            val name = text.getText()
            if (name.isBlank()) continue
            SpiritLeap.names[name] = button.text.getText() == "Enabled"
        }

        PersistentSave.markDirty<SpiritLeap>()
    }
}