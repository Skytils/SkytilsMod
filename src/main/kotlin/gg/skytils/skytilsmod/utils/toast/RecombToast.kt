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

package gg.skytils.skytilsmod.utils.toast

import gg.skytils.skytilsmod.gui.profile.components.ItemComponent
import gg.skytils.skytilsmod.utils.ItemUtil
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

class RecombToast(item: String) :
        Toast(
            "§6§lAuto-Recomb Item!",
            ItemComponent(RECOMB),
            item
        )
{
    companion object {
        private val RECOMB = ItemUtil.setSkullTexture(
            ItemStack(Items.skull, 1, 3),
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWRmZjhkYmJhYjE1YmZiYjExZTIzYjFmNTBiMzRlZjU0OGFkOTgzMmMwYmQ3ZjVhMTM3OTFhZGFkMDA1N2UxYiJ9fX0K",
            "10479f18-e67f-3c86-93e2-b4df79d0457e"
        )
        val pattern = Regex(" §r§([\\w ]+)§r§e")
    }
}