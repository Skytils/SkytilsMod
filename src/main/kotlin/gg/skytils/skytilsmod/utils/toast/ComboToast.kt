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

class ComboToast(length: String, buff: String) :
    Toast(
        length,
        when {
            buff.contains("magic find", ignoreCase = true) -> "luck"
            buff.contains("coins per kill", ignoreCase = true) -> "coin"
            buff.contains("combat exp", ignoreCase = true) -> "combat"
            buff.contains("you reached", ignoreCase = true) -> "comboFail"
            else -> null
        }?.let {
            UIImage.ofResource("/assets/skytils/toasts/combo/$it.png")
        },
        buff
    ) {
    companion object {
        private val regex = Regex("(§r§.§l)\\+(\\d+ Kill Combo) ?(§r§8.+)?")

        fun fromString(input: String) =
            regex.find(input)?.groupValues?.let { groups ->
                groups.getOrNull(3)?.let { buff ->
                    ComboToast(groups[1] + groups[2], buff)
                }
            }
    }
}