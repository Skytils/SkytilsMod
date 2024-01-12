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
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.State
import gg.essential.vigilance.gui.VigilancePalette
import gg.skytils.skytilsmod.gui.constraints.FixedChildBasedRangeConstraint
import gg.skytils.skytilsmod.gui.profile.states.alwaysMap
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.item.Inventory

class WardrobeComponent(val profileState: State<Member?>) : UIContainer() {

    private val armor =
        ArmorComponent(profileState.map { p ->
            p?.armor.also { it?.items?.reverse() } ?: Inventory(
                "armor",
                ArrayList(4)
            )
        }, false).constrain {
            width = FixedChildBasedRangeConstraint()
            height = ChildBasedMaxSizeConstraint()
        } childOf this
    private val wardrobeContainer = UIContainer().constrain {
        x = 0.pixels
        y = SiblingConstraint(2f)
        width = 40.percentOfWindow
        height = FixedChildBasedRangeConstraint()
    } childOf this


    init {
        profileState.onSetValue { profile ->
            Window.enqueueRenderOperation {
                wardrobeContainer.clearChildren()
                profile?.wardrobe?.items?.run {
                    (0..<size / 4).map { slot ->
                        val page = slot / 9
                        profileState.alwaysMap { prof ->
                            Inventory(
                                "Wardrobe slot $slot",
                                prof?.wardrobe?.items?.slice((0..3).map { it * 9 + slot % 9 + page * 36 })
                                    ?.toMutableList()
                                    ?: ArrayList(4)
                            )
                        }
                    }.forEach { state ->
                        if (state.get().items.any { it == null }) return@forEach
                        ArmorComponent(state, true).constrain {
                            x = CramSiblingConstraint(2f)
                            y = CramSiblingConstraint(2f)
                            width = ChildBasedMaxSizeConstraint()
                            height = FixedChildBasedRangeConstraint()
                        }.apply {
                            parseSlots(state.get())
                        } childOf wardrobeContainer
                    }
                } ?: kotlin.run {
                    val rectangle = UIRoundedRectangle(5f).constrain {
                        width = 100.percent
                        height = 70.pixels
                        color = VigilancePalette.getBackground().constraint
                    } childOf wardrobeContainer
                    UIText("Wardrobe API disabled!").constrain {
                        x = CenterConstraint()
                        y = CenterConstraint()
                    } childOf rectangle
                }
            }
        }
        constrain {
            width = ChildBasedSizeConstraint()
            height = FixedChildBasedRangeConstraint() coerceAtLeast 88.pixels
        }

    }

    class ArmorComponent(invState: State<Inventory>, val vertical: Boolean) : UIContainer() {
        private var invState: State<Inventory> = invState.also {
            it.onSetValue(::parseSlots)
        }

        fun bindInv(newState: State<Inventory>) = apply {
            invState = newState
            invState.onSetValue(::parseSlots)
        }

        fun parseSlots(inv: Inventory) = Window.enqueueRenderOperation {
            clearChildren()
            inv.run {
                items.forEach { item ->
                    insertChildAt(SlotComponent(item?.asMinecraft).constrain {
                        x = if (vertical) 0.pixels else SiblingConstraint(2f)
                        y = if (vertical) SiblingConstraint(2f) else 0.pixels
                    }, children.size)
                }
            }
        }
    }
}