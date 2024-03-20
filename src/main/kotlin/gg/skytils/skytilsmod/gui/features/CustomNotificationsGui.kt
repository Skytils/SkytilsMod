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

import gg.essential.api.EssentialAPI
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
import gg.skytils.skytilsmod.features.impl.handlers.CustomNotifications
import gg.skytils.skytilsmod.gui.ReopenableGUI
import gg.skytils.skytilsmod.gui.components.HelpComponent
import gg.skytils.skytilsmod.gui.components.SimpleButton
import java.awt.Color

class CustomNotificationsGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent
    private val components = HashMap<UIContainer, Entry>()

    private data class Entry(
        val container: UIContainer,
        val regex: UITextInput,
        val displayText: UITextInput,
        val ticks: UITextInput
    )

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

        HelpComponent(
            window,
            "What are custom notifications? Custom Notifications allow you to configure popups for when certain chat messages are entered, using regex."
        )

        for (notif in CustomNotifications.notifications.sortedBy { it.text }) {
            addNewNotification(notif.regex.pattern, notif.text, notif.displayTicks)
        }
    }

    private fun addNewNotification(regex: String = "", text: String = "", ticks: Int = 20) {
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
            setText(regex)
        }

        val displayText = UITextInput("Display Text").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 32.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(text)
        }

        val displayTicks = UITextInput("Ticks").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(ticks.toString())
        }
        triggerMessage.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) displayText.grabWindowFocus()
            }
        }
        displayText.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) displayTicks.grabWindowFocus()
            }
        }
        displayTicks.apply {
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
            components.remove(container)
        }

        components[container] = Entry(container, triggerMessage, displayText, displayTicks)
    }

    override fun onScreenClose() {
        super.onScreenClose()
        CustomNotifications.notifications.clear()

        for ((_, triggerRegex, displayText, displayTicks) in components.values) {
            if (triggerRegex.getText().isBlank() || displayText.getText().isBlank() || displayTicks.getText()
                    .isBlank()
            ) continue
            runCatching {
                CustomNotifications.notifications.add(
                    CustomNotifications.Notification(
                        triggerRegex.getText().replace("%%MC_IGN%%", mc.session.username).toRegex(),
                        displayText.getText(),
                        displayTicks.getText().toInt()
                    )
                )
            }.onFailure {
                it.printStackTrace()
                EssentialAPI.getNotifications().push("Invalid notification", triggerRegex.getText(), 3f)
            }
        }

        PersistentSave.markDirty<CustomNotifications>()
    }
}