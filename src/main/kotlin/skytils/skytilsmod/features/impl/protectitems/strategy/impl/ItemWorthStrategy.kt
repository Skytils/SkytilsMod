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

package skytils.skytilsmod.features.impl.protectitems.strategy.impl

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.features.impl.protectitems.strategy.ItemProtectStrategy
import skytils.skytilsmod.utils.SBInfo

class ItemWorthStrategy : ItemProtectStrategy() {
    override fun worthProtecting(item: ItemStack, extraAttr: NBTTagCompound?, type: ProtectType): Boolean {
        val id = AuctionData.getIdentifier(item) ?: return false
        if (AuctionData.lowestBINs.size == 0) return true
        val value = AuctionData.lowestBINs.getOrDefault(id, 0.0)
        val threshold = Skytils.config.protectItemBINThreshold.toInt()
        when (type) {
            ProtectType.CLICKOUTOFWINDOW, ProtectType.DROPKEYININVENTORY, ProtectType.SALVAGE, ProtectType.SELLTONPC, ProtectType.USERCLOSEWINDOW -> {
                return value >= threshold
            }
            ProtectType.HOTBARDROPKEY -> {
                if (SBInfo.instance.mode != SBInfo.SkyblockIslands.DUNGEON.mode) {
                    return value >= threshold
                }
            }
        }
        return false
    }

    override fun toggled(): Boolean {
        return Skytils.config.protectItemBINThreshold.toInt() > 0
    }
}