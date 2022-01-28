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
import gg.essential.elementa.state.MappedState
import gg.essential.universal.UMatrixStack
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.client.config.GuiUtils
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.item.Inventory
import skytils.skytilsmod.Skytils.Companion.mc
import java.awt.Color

class InventoryComponent(val inv: MappedState<Member?, Inventory?>) : UIComponent() {

    init {
        inv.onSetValue {
            if (it != null) {
                clearChildren()
                for ((i, item) in it.items.withIndex()) {
                    addChild(SlotComponent(item?.asMinecraft).constrain {
                        x = ((i % 9) * (16 + 2)).pixels
                        y = ((i / 9) * (16 + 2)).pixels
                    })
                }
            }
        }
    }

    class SlotComponent(val item: ItemStack?) : UIRoundedRectangle(2f) {

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
        }

        override fun draw(matrixStack: UMatrixStack) {
            super.draw(matrixStack)
            if (isHovered() && item != null) {
                val mouse = getMousePosition()
                val window = Window.of(this)
                GuiUtils.drawHoveringText(
                    item.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips),
                    mouse.first.toInt(),
                    mouse.second.toInt(),
                    window.getWidth().toInt(),
                    window.getHeight().toInt(),
                    -1,
                    item.item.getFontRenderer(item) ?: mc.fontRendererObj
                )
            }
        }
    }
}