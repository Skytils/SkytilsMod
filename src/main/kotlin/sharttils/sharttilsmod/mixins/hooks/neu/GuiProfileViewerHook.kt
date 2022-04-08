/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.mixins.hooks.neu

import net.minecraft.item.ItemStack
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.utils.NEUCompatibility.drawItemStackMethod
import sharttils.sharttilsmod.utils.RenderUtil.renderRarity

fun renderRarityOnPage(stack: ItemStack, x: Int, y: Int) {
    if (Sharttils.config.showItemRarity) {
        renderRarity(stack, x, y)
    }
    drawItemStackMethod.invokeExact(stack, x, y)
}