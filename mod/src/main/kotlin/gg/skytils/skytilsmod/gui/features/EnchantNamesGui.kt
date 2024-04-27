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
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.EnchantNames
import gg.skytils.skytilsmod.gui.ReopenableGUI
import gg.skytils.skytilsmod.gui.components.HelpComponent
import gg.skytils.skytilsmod.gui.components.SimpleButton
import net.minecraft.util.ChatAllowedCharacters
import java.awt.Color

class EnchantNamesGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent

    init {
        UIText("Enchant Names").childOf(window).constrain {
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
            mc.displayGuiScreen(null)
        }

        SimpleButton("Add Enchant").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            addNewName()
        }

        HelpComponent(window, "What are enchant names? Enchant names simply rename enchants to be anything you want!")

        for (replacement in EnchantNames.replacements) {
            addNewName(replacement.key, replacement.value)
        }
    }

    private fun addNewName(name: String = "", replacement: String = "") {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = 9.5.percent()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))


        val enchantName = UITextInput("Enchant Name").childOf(container).constrain {
            x = 5.pixels()
            y = CenterConstraint()
            width = 40.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(name)
        }

        val replacement = UITextInput("Replacement").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 40.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(replacement)
        }

        enchantName.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) replacement.grabWindowFocus()
                setText(getText().filter(ChatAllowedCharacters::isAllowedCharacter).take(255))
            }
        }
        replacement.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) enchantName.grabWindowFocus()
                setText(getText().filter(ChatAllowedCharacters::isAllowedCharacter).take(255))
            }
        }

        SimpleButton("Remove").childOf(container).constrain {
            x = 85.percent()
            y = CenterConstraint()
            height = 75.percent()
        }.onLeftClick {
            scrollComponent.removeChild(container)
        }
    }

    override fun onScreenClose() {
        super.onScreenClose()
        EnchantNames.replacements.clear()


        for (container in scrollComponent.allChildren) {
            val text = container.childrenOfType<UITextInput>()
            if (text.size != 2) throw IllegalStateException("${container.componentName} does not have 2 UITextInput's! Available children ${container.children.map { it.componentName }}")
            val name = (text.find { it.placeholder == "Enchant Name" }
                ?: throw IllegalStateException("${container.componentName} does not have the enchant UITextInput! Available children ${container.children.map { it.componentName }}")).getText()
            val replacement = (text.find { it.placeholder == "Replacement" }
                ?: throw IllegalStateException("${container.componentName} does not have the replacement UITextInput! Available children ${container.children.map { it.componentName }}")).getText()
            if (name.isBlank() || replacement.isBlank()) continue
            println("$name $replacement")
            EnchantNames.replacements[name] = replacement
        }

        PersistentSave.markDirty<EnchantNames>()
    }
}