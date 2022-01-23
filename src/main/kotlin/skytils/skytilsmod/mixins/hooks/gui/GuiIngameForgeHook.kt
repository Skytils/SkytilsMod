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
package skytils.skytilsmod.mixins.hooks.gui

import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.invoke.arg.Args
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.features.impl.misc.MiscFeatures
import skytils.skytilsmod.utils.Utils


fun alwaysShowItemHighlight(orig: Int): Int {
    return if (Skytils.config.alwaysShowItemHighlight && Utils.inSkyblock) 1 else orig
}

fun modifyItemHighlightPosition(args: Args, highlightingItemStack: ItemStack) {
    if (Skytils.config.moveableItemNameHighlight && Utils.inSkyblock) {
        val fr = highlightingItemStack.item.getFontRenderer(highlightingItemStack) ?: mc.fontRendererObj
        val itemName = args.get<String>(0)
        val element: GuiElement = MiscFeatures.ItemNameHighlightDummy
        val x = element.actualX - fr!!.getStringWidth(itemName) / 2f
        args.set(1, x)
        args.set(2, element.actualY)
    }
}

fun modifyActionBarPosition(args: Args) {
    if (Skytils.config.moveableActionBar && Utils.inSkyblock) {
        val element: GuiElement = MiscFeatures.ActionBarDummy
        args.set(0, element.actualX)
        args.set(1, element.actualY + 4f)
    }
}

fun setAbsorptionAmount(amount: Float): Float {
    return if (Utils.inSkyblock && Skytils.config.hideAbsorption) 0f else amount
}