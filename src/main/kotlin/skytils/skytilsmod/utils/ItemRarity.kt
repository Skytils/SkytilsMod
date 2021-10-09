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
package skytils.skytilsmod.utils

import net.minecraft.util.EnumChatFormatting
import java.awt.Color

/**
 * Taken from Skyblockcatia under MIT License
 * Modified
 * https://github.com/SteveKunG/SkyBlockcatia/blob/1.8.9/LICENSE.md
 * @author SteveKunG
 */
enum class ItemRarity(val rarityName: String, val baseColor: EnumChatFormatting, val color: Color) {
    COMMON("COMMON", EnumChatFormatting.WHITE, Color(255, 255, 255)),
    UNCOMMON("UNCOMMON", EnumChatFormatting.GREEN, Color(77, 231, 77)),
    RARE("RARE", EnumChatFormatting.BLUE, Color(85, 85, 255)),
    EPIC("EPIC", EnumChatFormatting.DARK_PURPLE, Color(151, 0, 151)),
    LEGENDARY("LEGENDARY", EnumChatFormatting.GOLD, Color(255, 170, 0)),
    MYTHIC("MYTHIC", EnumChatFormatting.LIGHT_PURPLE, Color(255, 85, 255)),
    DIVINE("DIVINE", EnumChatFormatting.AQUA, Color(85, 255, 255)),
    SUPREME("SUPREME", EnumChatFormatting.DARK_RED, Color(170, 0, 0)),
    SPECIAL("SPECIAL", EnumChatFormatting.RED, Color(255, 85, 85)),
    VERY_SPECIAL("VERY SPECIAL", EnumChatFormatting.RED, Color(170, 0, 0));

    companion object {
        private val VALUES = values().sortedBy { obj: ItemRarity -> obj.ordinal }.toMutableList()
        val RARITY_PATTERN: Regex

        fun byBaseColor(color: String) = values().find { rarity -> rarity.baseColor.toString() == color }

        init {
            values().forEach { rarity -> VALUES[rarity.ordinal] = rarity }
            RARITY_PATTERN =
                Regex("(§[0-9a-f]§l§ka§r )?([§0-9a-fk-or]+)(?<rarity>${VALUES.joinToString("|") { it.rarityName }})")
        }
    }

    val nextRarity: ItemRarity
        get() = VALUES[(ordinal + 1) % VALUES.size]
}