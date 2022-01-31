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
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import gg.essential.elementa.state.State
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft
import gg.essential.universal.wrappers.UPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.client.config.GuiUtils
import skytils.hylin.skyblock.item.Inventory
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.utils.addTooltip
import skytils.skytilsmod.utils.stripControlCodes
import java.awt.Color

class InventoryComponent(val inv: State<Inventory?>) : UIComponent() {

    var needsSetup = false

    init {
        inv.onSetValue {
            if (it != null) needsSetup = true
        }
    }

    override fun draw(matrixStack: UMatrixStack) {
        if (needsSetup) {
            val inv = this.inv.get()!!
            clearChildren()
            for ((i, item) in inv.items.withIndex()) {
                addChild(SlotComponent(item?.asMinecraft).constrain {
                    x = ((i % 9) * (16 + 2)).pixels
                    y = ((i / 9) * (16 + 2)).pixels
                })
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
                val tooltip = TooltipComponent()
                getTooltip(UPlayer.getPlayer(), UMinecraft.getMinecraft().gameSettings.advancedItemTooltips).forEach {
                    if (it.stripControlCodes().isEmpty()) {
                        return@forEach
                    }
                    tooltip.addLine(it)
                }
                this@SlotComponent.addTooltip(tooltip)
            }

        }
    }
}