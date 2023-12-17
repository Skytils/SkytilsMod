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
package gg.skytils.skytilsmod.utils

import gg.essential.universal.ChatColor
import java.awt.Color

/**
 * Taken from Skyblockcatia under MIT License
 * Modified
 * https://github.com/SteveKunG/SkyBlockcatia/blob/1.8.9/LICENSE.md
 * @author SteveKunG
 */
enum class ItemRarity(val rarityName: String, val baseColor: ChatColor, val color: Color = baseColor.color!!) {
    NONE("NONE", ChatColor.GRAY),
    COMMON("COMMON", ChatColor.WHITE, Color(255, 255, 255)),
    UNCOMMON("UNCOMMON", ChatColor.GREEN, Color(77, 231, 77)),
    RARE("RARE", ChatColor.BLUE, Color(85, 85, 255)),
    EPIC("EPIC", ChatColor.DARK_PURPLE, Color(151, 0, 151)),
    LEGENDARY("LEGENDARY", ChatColor.GOLD, Color(255, 170, 0)),
    MYTHIC("MYTHIC", ChatColor.LIGHT_PURPLE, Color(255, 85, 255)),
    DIVINE("DIVINE", ChatColor.AQUA, Color(85, 255, 255)),
    SUPREME("SUPREME", ChatColor.DARK_RED, Color(170, 0, 0)),
    SPECIAL("SPECIAL", ChatColor.RED, Color(255, 85, 85)),
    VERY_SPECIAL("VERY SPECIAL", ChatColor.RED, Color(170, 0, 0));

    companion object {
        private val VALUES = entries.sortedBy { obj: ItemRarity -> obj.ordinal }.toMutableList()
        val RARITY_PATTERN: Regex

        fun byBaseColor(color: String) = entries.find { rarity -> rarity.baseColor.toString() == color }

        init {
            entries.forEach { rarity -> VALUES[rarity.ordinal] = rarity }
            RARITY_PATTERN =
                Regex("(?:§[\\da-f]§l§ka§r )?(?<rarity>${VALUES.joinToString("|") { "(?:${it.baseColor}§l)+(?:SHINY )?${it.rarityName}" }})")
        }
    }

    val nextRarity: ItemRarity
        get() = VALUES[(ordinal + 1) % VALUES.size]
}