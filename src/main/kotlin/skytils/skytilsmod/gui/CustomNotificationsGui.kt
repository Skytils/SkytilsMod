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
import gg.essential.elementa.components.*
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UKeyboard
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.util.ChatAllowedCharacters
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.CustomNotifications
import skytils.skytilsmod.gui.components.SimpleButton
import java.awt.Color

class CustomNotificationsGui : WindowScreen(newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent

    init {
        UIText("Custom Notifications").childOf(window).constrain {
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

        SimpleButton("Add Notification").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            addNewNotification()
        }

        val tooltipBlock = UIBlock().constrain {
            x = 5.pixels(); y = basicYConstraint { it.parent.getTop() - it.getHeight() - 6 }; height =
            ChildBasedSizeConstraint(4f); width = ChildBasedSizeConstraint(4f); color =
            ConstantColorConstraint(Color(224, 224, 224, 100))
        }.effect(OutlineEffect(Color(0, 243, 255), 1f)).also { it.hide() }
            .addChild(UIWrappedText("What are custom notifications? Custom Notifications allow you to configure popups for when certain chat messages are entered, using regex.").constrain {
                x = 2.pixels(); y = 0.pixels(); width = 90.percentOfWindow()
            })
        UICircle(7f).childOf(window).constrain {
            x = 9.pixels()
            y = basicYConstraint { it.parent.getBottom() - it.getHeight() - 2 }
        }.addChildren(
            UIText("?", true).constrain { x = CenterConstraint(); y = CenterConstraint() },
            tooltipBlock
        ).onMouseEnter {
            tooltipBlock.unhide()
        }.onMouseLeave {
            tooltipBlock.hide()
        }

        for (name in CustomNotifications.notifications) {
            addNewNotification(name.key.pattern, name.value)
        }
    }

    private fun addNewNotification(alias: String = "", replacement: String = "") {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = 9.5.percent()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val triggerMessage = UITextInput("Trigger Regex").childOf(container).constrain {
            x = 5.pixels()
            y = CenterConstraint()
            width = 40.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(alias)
        }

        val displayText = UITextInput("Display Text").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 40.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(replacement)
        }
        triggerMessage.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) displayText.grabWindowFocus()
            }
        }
        displayText.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) triggerMessage.grabWindowFocus()
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
        CustomNotifications.notifications.clear()

        for (container in scrollComponent.allChildren) {
            val text = container.childrenOfType<UITextInput>()
            if (text.size != 2) throw IllegalStateException("${container.componentName} does not have 2 UITextInput's! Available children ${container.children.map { it.componentName }}")
            val triggerRegex = (text.find { it.placeholder == "Trigger Regex" }
                ?: throw IllegalStateException("${container.componentName} does not have the trigger UITextInput! Available children ${container.children.map { it.componentName }}")).getText()
            val displayText = (text.find { it.placeholder == "Display Text" }
                ?: throw IllegalStateException("${container.componentName} does not have the display text UITextInput! Available children ${container.children.map { it.componentName }}")).getText()
            if (triggerRegex.isBlank() || displayText.isBlank()) continue
            CustomNotifications.notifications[triggerRegex.toRegex()] = displayText
        }

        PersistentSave.markDirty<CustomNotifications>()
    }
}