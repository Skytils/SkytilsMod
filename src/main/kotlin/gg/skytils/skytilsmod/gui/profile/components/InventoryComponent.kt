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

package gg.skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.State
import gg.essential.vigilance.gui.VigilancePalette
import net.minecraft.item.ItemStack

class InventoryComponent(val inv: State<List<ItemStack?>?>) : UIContainer() {
    init {
        inv.onSetValue(::parseInv)
        UIRoundedRectangle(5f).constrain {
            width = 100.percent
            height = 100.percent
            color = VigilancePalette.getBackground().constraint
        } childOf this
        UIText("Loading...").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf this
    }

    fun parseInv(inventory: List<ItemStack?>?) = Window.enqueueRenderOperation {
        clearChildren()
        inventory?.run {
            for ((i, item) in withIndex()) {
                addChild(SlotComponent(item).constrain {
                    x = ((i % 9) * (16 + 2)).pixels
                    y = ((i / 9) * (16 + 2)).pixels
                })
            }
        } ?: kotlin.run {
            UIRoundedRectangle(5f).constrain {
                width = 100.percent
                height = 100.percent
                color = VigilancePalette.getBackground().constraint
            } childOf this
            UIText("Inventory api not enabled!").constrain {
                x = CenterConstraint()
                y = CenterConstraint()
            } childOf this
        }
    }
}