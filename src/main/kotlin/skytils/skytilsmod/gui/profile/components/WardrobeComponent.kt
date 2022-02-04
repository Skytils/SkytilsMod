/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.State
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.item.Inventory

class WardrobeComponent(val profileState: State<Member?>) : UIComponent() {

    init {
        ArmorComponent(profileState.map { it?.armor.also { it?.items?.reverse() } }, false).constrain {
            width = 400.pixels
            height = 20.pixels
        } childOf this
        profileState.map { profile ->
            profile?.wardrobe?.items?.chunked(36) {
                it.withIndex().groupBy { it.index % 9 }.values.map { it.map { it.value } }
            }?.flatten()
        }.also {
            it.onSetValue {
                it?.forEach {
                    it.forEachIndexed { i, item ->
                        println("$i ${item?.id}")
                    }
                }
            }
        }
        constrain {
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

    }

    class ArmorComponent(invState: State<Inventory?>, val vertical: Boolean) : UIComponent() {
        private var invState: State<Inventory?> = invState.also {
            it.onSetValue(::parseSlots)
        }

        fun bindInv(newState: State<Inventory?>) = apply {
            invState = newState
            invState.onSetValue(::parseSlots)
        }

        fun parseSlots(inv: Inventory?) = Window.enqueueRenderOperation {
            clearChildren()
            inv?.run {
                items.forEach { item ->
                    insertChildAt(SlotComponent(item?.asMinecraft).constrain {
                        x = if (vertical) 0.pixels else CramSiblingConstraint(2f)
                        y = if (vertical) SiblingConstraint(2f) else 0.pixels
                    }, children.size)
                }
            }
        }

        fun collapse(immediate: Boolean = false) {
            children.forEach {
                if (immediate) {
                    it.setY(0.pixels)
                } else {
                    it.animate {
                        setYAnimation(Animations.IN_OUT_CUBIC, 1f, 0.pixels)
                    }
                }
            }
        }

        fun expand(immediate: Boolean = false) {
            children.forEach {
                if (immediate) {
                    it.setY(SiblingConstraint())
                } else {
                    it.animate {
                        setYAnimation(Animations.IN_OUT_CUBIC, 1f, SiblingConstraint())
                    }
                }
            }
        }
    }
}