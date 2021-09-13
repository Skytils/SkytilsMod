/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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

package skytils.skytilsmod.gui.components

import gg.essential.elementa.UIComponent
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import skytils.skytilsmod.utils.RenderUtil

class ItemComponent(val item: ItemStack) : UIComponent() {

    constructor(item: Item, metadata: Int = 0) : this(ItemStack(item, 1, metadata))

    override fun draw() {
        GlStateManager.pushMatrix()
        GlStateManager.translate(getLeft(), getTop(), 100f)
        GlStateManager.scale(getWidth() / 16f, getHeight() / 16f, 0f)
        GlStateManager.color(1f, 1f, 1f, 1f)
        RenderUtil.renderItem(item, 0, 0)
        GlStateManager.popMatrix()
        GlStateManager.disableLighting()
        super.draw()
    }
}