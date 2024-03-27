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
package gg.skytils.skytilsmod.mixins.hooks.gui

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.features.impl.misc.MiscFeatures
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.invoke.arg.Args


fun alwaysShowItemHighlight(orig: Int): Int {
    return if (Skytils.config.alwaysShowItemHighlight && Utils.inSkyblock) 10 else orig
}

fun modifyItemHighlightPosition(args: Args, highlightingItemStack: ItemStack) {
    if (Skytils.config.moveableItemNameHighlight && Utils.inSkyblock) {
        val fr = highlightingItemStack.item.getFontRenderer(highlightingItemStack) ?: mc.fontRendererObj
        val itemName = args.get<String>(0)
        val element: GuiElement = MiscFeatures.ItemNameHighlightDummy
        val x = element.scaleX - fr!!.getStringWidth(itemName) / 2f
        args.set(1, x)
        args.set(2, element.scaleY)
    }
}

fun modifyActionBarPosition(args: Args) {
    if (Skytils.config.moveableActionBar && Utils.inSkyblock) {
        val element: GuiElement = MiscFeatures.ActionBarDummy
        args.set(0, element.scaleX)
        args.set(1, element.scaleY + 4f)
    }
}

fun setAbsorptionAmount(amount: Float): Float {
    return if (Utils.inSkyblock && Skytils.config.hideAbsorption) 0f else amount
}