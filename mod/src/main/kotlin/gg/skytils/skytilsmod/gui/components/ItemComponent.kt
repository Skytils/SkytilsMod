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

package gg.skytils.skytilsmod.gui.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.unstable.state.v2.State
import gg.essential.elementa.unstable.state.v2.stateOf
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft
import gg.skytils.skytilsmod.utils.RenderUtil
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class ItemComponent(val state: State<ItemStack>) : UIComponent() {

    constructor(stack: ItemStack) : this(stateOf(stack))
    constructor(item: Item) : this(ItemStack(item, 1))

    override fun draw(matrixStack: UMatrixStack) {
        beforeDraw(matrixStack)
        matrixStack.push()
        matrixStack.translate(getLeft(), getTop(), 100f)
        //matrixStack.scale(getWidth() / 16f, getHeight() / 16f, 0f)
        UGraphics.color4f(1f, 1f, 1f, 1f)
        // TODO: ensure this behaves as expected
        val vertexConsumer = UMinecraft.getMinecraft().bufferBuilders.entityVertexConsumers
        val item = state.getUntracked()
        RenderUtil.renderItem(item, 0, 0)
        vertexConsumer.draw()
        matrixStack.pop()
        UGraphics.disableLighting()
        super.draw(matrixStack)
    }
}