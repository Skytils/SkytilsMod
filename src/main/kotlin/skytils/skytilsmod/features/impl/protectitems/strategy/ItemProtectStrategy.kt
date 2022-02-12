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

package skytils.skytilsmod.features.impl.protectitems.strategy

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import skytils.skytilsmod.features.impl.protectitems.strategy.impl.FavoriteStrategy
import skytils.skytilsmod.features.impl.protectitems.strategy.impl.ItemWorthStrategy
import skytils.skytilsmod.features.impl.protectitems.strategy.impl.StarredItemStrategy

abstract class ItemProtectStrategy {
    abstract fun worthProtecting(item: ItemStack, extraAttr: NBTTagCompound?, type: ProtectType): Boolean
    abstract val isToggled: Boolean

    companion object {
        private val STRATEGIES = HashSet<ItemProtectStrategy>()

        fun isAnyToggled(): Boolean {
            return STRATEGIES.any { it.isToggled }
        }

        fun isAnyWorth(item: ItemStack, extraAttr: NBTTagCompound?, type: ProtectType): Boolean {
            return STRATEGIES.any { it.isToggled && it.worthProtecting(item, extraAttr, type) }
        }

        init {
            STRATEGIES.add(ItemWorthStrategy)
            STRATEGIES.add(StarredItemStrategy)
            STRATEGIES.add(FavoriteStrategy)
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