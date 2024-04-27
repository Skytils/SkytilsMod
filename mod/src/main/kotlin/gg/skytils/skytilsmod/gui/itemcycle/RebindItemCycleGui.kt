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
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.features.impl.handlers.ItemCycle
import gg.skytils.skytilsmod.features.impl.handlers.ItemCycle.getIdentifier
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.gui.profile.components.SlotComponent

class RebindItemCycleGui(cycle: ItemCycle.Cycle) : WindowScreen(ElementaVersion.V5, newGuiScale = 2) {
    init {
        UIText("Editing ${cycle.name} (${cycle.uuid})").childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            height = 14.pixels
        }

        val container = UIContainer().childOf(window).constrain {
            x = CenterConstraint()
            y = 15.percent
            width = 90.percent
            height = 65.percent + 2.pixels
        }

        val chosenItem = BasicState<ItemCycle.Cycle.ItemIdentifier?>(null)

        val itemPicker by UIContainer().childOf(container).constrain {
            x = 0.pixels
            y = SiblingConstraint(10f)
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }.apply {
            for ((i, item) in mc.thePlayer.inventory.mainInventory.withIndex()) {
                addChild(SlotComponent(item).constrain {
                    x = ((i % 9) * (16 + 2)).pixels
                    y = ((i / 9) * (16 + 2)).pixels
                }.onLeftClick {
                    chosenItem.set(item.getIdentifier())
                })
            }
        }

        val chosenText by UIText("Chosen Item: ${cycle.swapTo}").childOf(container).constrain {
            x = 0.pixels
            y = SiblingConstraint(10f)
        }

        chosenItem.onSetValue {
            chosenText.setText("Chosen Item: $it")
        }

        val bottomButtons by UIContainer().childOf(window).constrain {
            x = CenterConstraint()
            y = 85.percent
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        SimpleButton("Save and Exit").childOf(bottomButtons).constrain {
            x = 0.pixels
            y = 0.pixels
        }.onLeftClick {
            mc.displayGuiScreen(ItemCycleGui())
        }
    }
}