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
import gg.skytils.skytilsmod.features.impl.handlers.PotionEffectTimers
import gg.skytils.skytilsmod.gui.ReopenableGUI
import gg.skytils.skytilsmod.gui.components.HelpComponent
import gg.skytils.skytilsmod.gui.components.SimpleButton
import java.awt.Color

class PotionNotificationsGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent
    private val components = HashMap<UIContainer, Entry>()

    private data class Entry(
        val container: UIContainer,
        val potionId: UITextInput,
        val remainingTicks: UITextInput
    )

    init {
        UIText("Potion Notifications").childOf(window).constrain {
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

        SimpleButton("Add Potion").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            addNewNotification()
        }

        HelpComponent(
            window,
            "Enter the potion ID and the ticks for the potion to be considered low"
        )

        for (notif in PotionEffectTimers.notifications.toSortedMap()) {
            addNewNotification(notif.key, notif.value)
        }
    }

    private fun addNewNotification(potionName: String = "", ticks: Long = 20 * 60L) {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = 9.5.percent()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val potion = UITextInput("Potion Name").childOf(container).constrain {
            x = 5.pixels()
            y = CenterConstraint()
            width = 70.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(potionName)
        }

        val remainingTicks = UITextInput("Ticks").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 8.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(ticks.toString())
        }
        potion.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) remainingTicks.grabWindowFocus()
            }
        }
        remainingTicks.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) potion.grabWindowFocus()
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

        components[container] = Entry(container, potion, remainingTicks)
    }

    override fun onScreenClose() {
        super.onScreenClose()
        PotionEffectTimers.notifications.clear()

        for ((_, potion, remainingTicks) in components.values) {
            if (potion.getText().isBlank() || remainingTicks.getText()
                    .isBlank()
            ) continue
            runCatching {
                PotionEffectTimers.notifications[potion.getText()] = remainingTicks.getText().toLong()
            }.onFailure {
                it.printStackTrace()
                EssentialAPI.getNotifications().push("Invalid notification", potion.getText(), 3f)
            }
        }

        PersistentSave.markDirty<PotionEffectTimers>()
    }
}