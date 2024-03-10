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

import gg.essential.elementa.UIComponent
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.utils.RenderUtil
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class ItemComponent(val state: State<ItemStack>) : UIComponent() {

    constructor(stack: ItemStack) : this(BasicState(stack))
    constructor(item: Item, metadata: Int = 0) : this(ItemStack(item, 1, metadata))

    override fun draw(matrixStack: UMatrixStack) {
        beforeDraw(matrixStack)
        super.draw(matrixStack)
        matrixStack.push()
        matrixStack.translate(getLeft(), getTop(), 100f)
        //matrixStack.scale(getWidth() / 16f, getHeight() / 16f, 0f)
        UGraphics.color4f(1f, 1f, 1f, 1f)
        matrixStack.runWithGlobalState {
            val item = state.get()
            RenderUtil.renderItem(item, 0, 0)
//            mc.renderItem.renderItemIntoGUI(item, 0, 0)
            mc.renderItem.renderItemOverlayIntoGUI(
                item.item.getFontRenderer(item) ?: mc.fontRendererObj,
                item,
                0,
                0,
                null
            )
        }
        matrixStack.pop()
        UGraphics.disableLighting()
    }
}