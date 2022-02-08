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

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.toConstraint
import net.minecraft.item.ItemStack
import skytils.skytilsmod.utils.addTooltip
import java.awt.Color

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