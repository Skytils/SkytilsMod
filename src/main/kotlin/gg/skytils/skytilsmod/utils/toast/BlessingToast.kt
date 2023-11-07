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

import gg.essential.elementa.components.UIImage
import gg.essential.universal.ChatColor
import net.minecraft.util.EnumChatFormatting

class BlessingToast(blessingType: String, buffs: List<BlessingBuff>) :
    Toast(
         title = when (blessingType) {
                            "life" -> ChatColor.RED
                            "power" -> ChatColor.LIGHT_PURPLE
                            "stone" -> ChatColor.GREEN
                            "wisdom" -> ChatColor.AQUA
                            "time" -> ChatColor.GOLD
                            else -> ChatColor.GOLD
                    } +
                "§l${blessingType.uppercase()} BLESSING!"
        , UIImage.ofResource("/assets/skytils/toasts/blessings/${blessingType}.png"))
{
    init {
        subtextState.set(
            buffs.joinToString(separator = " ") { buff ->
                val color: String = when (buff.symbol) {
                    "❤", "❁" -> EnumChatFormatting.RED.toString()
                    "✎" -> EnumChatFormatting.AQUA.toString()
                    "❈", "HP" -> EnumChatFormatting.GREEN.toString()
                    "✦" -> EnumChatFormatting.WHITE.toString()
                    "☠" -> EnumChatFormatting.BLUE.toString()
                    else -> EnumChatFormatting.GRAY.toString()
                }
                "$color${buff.amount}${buff.symbol}"
            }
        )
    }

    class BlessingBuff(val amount: String, val symbol: String)
}