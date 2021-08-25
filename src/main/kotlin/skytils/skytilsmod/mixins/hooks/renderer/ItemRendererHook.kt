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

package skytils.skytilsmod.mixins.hooks.renderer

import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.utils.Utils

fun getItemInUseCountForFirstPerson(player: AbstractClientPlayer, item: ItemStack): Int {
    if (Skytils.config.disableBlockAnimation && Utils.inSkyblock && item.item is ItemSword && player.itemInUseDuration <= 7) return 0
    return player.itemInUseCount
}