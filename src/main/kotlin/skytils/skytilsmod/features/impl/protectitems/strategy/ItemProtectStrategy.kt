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

package skytils.skytilsmod.features.impl.protectitems.strategy

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import skytils.skytilsmod.features.impl.protectitems.strategy.impl.ItemWorthStrategy
import skytils.skytilsmod.features.impl.protectitems.strategy.impl.StarredItemStrategy

abstract class ItemProtectStrategy {
    abstract fun worthProtecting(item: ItemStack, extraAttr: NBTTagCompound?, type: ProtectType): Boolean
    abstract fun toggled(): Boolean

    companion object {
        val STRATEGIES = HashSet<ItemProtectStrategy>()

        fun isAnyToggled(): Boolean {
            for (strategy in STRATEGIES) {
                if (strategy.toggled()) {
                    return true
                }
            }
            return false
        }

        fun isAnyWorth(item: ItemStack, extraAttr: NBTTagCompound?, type: ProtectType): Boolean {
            for (strategy in STRATEGIES) {
                if (strategy.toggled() && strategy.worthProtecting(item, extraAttr, type)) {
                    return true
                }
            }
            return false
        }

        init {
            STRATEGIES.add(ItemWorthStrategy())
            STRATEGIES.add(StarredItemStrategy())
        }

    }

    enum class ProtectType {
        USERCLOSEWINDOW,
        SELLTONPC,
        SALVAGE,
        CLICKOUTOFWINDOW,
        DROPKEYININVENTORY,
        HOTBARDROPKEY
    }
}