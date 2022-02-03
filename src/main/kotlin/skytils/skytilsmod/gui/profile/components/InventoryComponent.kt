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
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.State
import gg.essential.universal.ChatColor
import gg.essential.universal.UMatrixStack
import net.minecraft.item.ItemStack
import skytils.hylin.skyblock.item.Inventory
import skytils.skytilsmod.utils.addTooltip
import java.awt.Color

class InventoryComponent(val inv: State<Inventory?>) : UIComponent() {

    var needsSetup = true

    init {
        inv.onSetValue {
            needsSetup = true
        }
    }

    override fun draw(matrixStack: UMatrixStack) {
        if (needsSetup) {
            clearChildren()
            inv.get()?.run {
                for ((i, item) in items.withIndex()) {
                    addChild(SlotComponent(item?.asMinecraft).constrain {
                        x = ((i % 9) * (16 + 2)).pixels
                        y = ((i / 9) * (16 + 2)).pixels
                    })
                }
            } ?: kotlin.run {
                UIRoundedRectangle(5f).constrain {
                    width = 100.percent
                    height = 100.percent
                    color = ChatColor.GRAY.color!!.constraint
                } childOf this
                UIText("Inventory api not enabled!").constrain {
                    x = CenterConstraint()
                    y = CenterConstraint()
                } childOf this
            }
            needsSetup = false
        }
        super.draw(matrixStack)
    }

    class SlotComponent(val item: ItemStack?, radius: Float = 2f) : UIRoundedRectangle(radius) {

        init {
            if (item != null) addChild(ItemComponent(item).constrain {
                x = 0.pixels
                y = 0.pixels
                width = 16.pixels
                height = 16.pixels
            })
            constrain {
                width = 16.pixels
                height = 16.pixels
                color = Color(65, 102, 245).toConstraint()
            }

            item?.run {
                val tooltip = TooltipComponent(this)
                this@SlotComponent.addTooltip(tooltip)
            }

        }
    }
}