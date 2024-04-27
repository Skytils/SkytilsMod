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

package gg.skytils.skytilsmod.features.impl.protectitems.strategy.impl

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import gg.skytils.skytilsmod.features.impl.protectitems.strategy.ItemProtectStrategy
import gg.skytils.skytilsmod.utils.ItemUtil
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

object StarredItemStrategy : ItemProtectStrategy() {
    override fun worthProtecting(item: ItemStack, extraAttr: NBTTagCompound?, type: ProtectType): Boolean {
        if (extraAttr == null) return false
        val isStarred = ItemUtil.getStarCount(extraAttr) > 0
        return when (type) {
            ProtectType.HOTBARDROPKEY -> {
                !DungeonFeatures.hasClearedText && isStarred
            }
            ProtectType.SELLTOAUCTION -> false
            else -> isStarred
        }
    }

    override val isToggled: Boolean
        get() = Skytils.config.protectStarredItems
}