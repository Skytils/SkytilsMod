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
enum class ItemRarity(val baseColor: ChatColor, val color: Color = baseColor.color!!) {
    NONE(ChatColor.GRAY),
    COMMON(ChatColor.WHITE, Color(255, 255, 255)),
    UNCOMMON(ChatColor.GREEN, Color(77, 231, 77)),
    RARE(ChatColor.BLUE, Color(85, 85, 255)),
    EPIC(ChatColor.DARK_PURPLE, Color(151, 0, 151)),
    LEGENDARY(ChatColor.GOLD, Color(255, 170, 0)),
    MYTHIC(ChatColor.LIGHT_PURPLE, Color(255, 85, 255)),
    DIVINE(ChatColor.AQUA, Color(85, 255, 255)),
    SUPREME(ChatColor.DARK_RED, Color(170, 0, 0)),
    ULTIMATE(ChatColor.DARK_RED, Color(170, 0, 0)),
    SPECIAL(ChatColor.RED, Color(255, 85, 85)),
    VERY_SPECIAL(ChatColor.RED, Color(170, 0, 0));

    val rarityName by lazy {
        name.replace("_", " ").uppercase()
    }

    companion object {
        val RARITY_PATTERN by lazy {
            Regex("(?:§[\\da-f]§l§ka§r )?(?<rarity>${entries.joinToString("|") { "(?:${it.baseColor}§l)+(?:SHINY )?${it.rarityName}" }})")
        }

        fun byBaseColor(color: String) = entries.find { rarity -> rarity.baseColor.toString() == color }

    }

    val nextRarity: ItemRarity
        get() = entries[(ordinal + 1) % entries.size]
}