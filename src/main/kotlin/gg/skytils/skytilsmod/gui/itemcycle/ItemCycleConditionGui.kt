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

package gg.skytils.skytilsmod.gui.itemcycle

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.gui.settings.DropDown
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.features.impl.handlers.ItemCycle
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.gui.itemcycle.condition.*
import net.minecraft.client.gui.GuiScreen
import java.awt.Color

class ItemCycleConditionGui(cycle: ItemCycle.Cycle) : WindowScreen(ElementaVersion.V5, newGuiScale = 2) {
    val scrollComponent: ScrollComponent

    init {
        UIText("Editing ${cycle.name} (${cycle.uuid})").childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            height = 14.pixels
        }

        scrollComponent = ScrollComponent(
            innerPadding = 4f,
        ).childOf(window).constrain {
            x = CenterConstraint()
            y = 15.percent
            width = 90.percent
            height = 70.percent + 2.pixels
        }

        val bottomButtons by UIContainer().childOf(window).constrain {
            x = CenterConstraint()
            y = 90.percent
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        SimpleButton("Save and Exit").childOf(bottomButtons).constrain {
            x = 0.pixels
            y = 0.pixels
        }.onLeftClick {
            mc.displayGuiScreen(ItemCycleGui())
        }

        DropDown(0, listOf("Add New Condition", "Click", "Island", "Item")).childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels
        }.onValueChange {
            when (it) {
                1 -> mc.displayGuiScreen(ClickConditionGui(cycle))
                2 -> mc.displayGuiScreen(IslandConditionGui(cycle))
                3 -> mc.displayGuiScreen(ItemConditionGui(cycle))
            }
        }

        cycle.conditions.forEach {
            addNewCondition(cycle, it)
        }
    }

    private fun addNewCondition(cycle: ItemCycle.Cycle, condition: ItemCycle.Cycle.Condition) {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent
            height = 9.5.percent
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        UIText(condition::class.simpleName ?: "Error").childOf(container).constrain {
            x = 5.pixels
            y = CenterConstraint()
            width = 30.percent
        }

        UIText(condition.displayText()).childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 30.percent
            height = 10.percent
        }

        SimpleButton("Edit").childOf(container).constrain {
            x = 70.percent
            y = CenterConstraint()
            height = 75.percent
        }.onLeftClick {
            mc.displayGuiScreen(getConditionGui(cycle, condition))
        }

        SimpleButton("Remove").childOf(container).constrain {
            x = 85.percent
            y = CenterConstraint()
            height = 75.percent
        }.onLeftClick {
            scrollComponent.removeChild(container)
            cycle.conditions.remove(condition)
        }
    }

    private fun getConditionGui(cycle: ItemCycle.Cycle, condition: ItemCycle.Cycle.Condition): GuiScreen {
        return when (condition) {
            is ItemCycle.Cycle.Condition.ClickCondition -> ClickConditionGui(cycle, condition)
            is ItemCycle.Cycle.Condition.IslandCondition -> IslandConditionGui(cycle, condition)
            is ItemCycle.Cycle.Condition.ItemCondition -> ItemConditionGui(cycle, condition)
        }
    }
}