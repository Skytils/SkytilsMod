package gg.skytils.skytilsmod.util

import gg.essential.universal.ChatColor
import java.awt.Color

enum class ItemRarity(val chatColor: ChatColor, val color: Color) {
    COMMON(ChatColor.WHITE, Color(0xFFFFFF)),
    UNCOMMON(ChatColor.GREEN, Color(0x55FF55)),
    RARE(ChatColor.BLUE, Color(0x5555FF)),
    EPIC(ChatColor.DARK_PURPLE, Color(0xAA00AA)),
    LEGENDARY(ChatColor.GOLD, Color(0xFFAA00)),
    MYTHIC(ChatColor.LIGHT_PURPLE, Color(0xFF55FF)),
    DIVINE(ChatColor.AQUA, Color(0x55FFFF)),
    SPECIAL(ChatColor.RED, Color(0xFF5555)),
    VERY_SPECIAL(ChatColor.RED, Color(0xFF5555)),
    ULTIMATE(ChatColor.DARK_RED, Color(0xAA0000)),
    ADMIN(ChatColor.DARK_RED, Color(0xAA0000));

    val rarityName by lazy {
        name.replace("_", " ").uppercase()
    }

    companion object {
        val RARITY_REGEX by lazy {
            // TODO: Fix once §k (obfuscated) is shown in toFormattedString()
            Regex("^(?:§r§[\\da-f]§la§r )?§r§[\\da-f]§l(?:SHINY )?(?<rarity>${entries.joinToString("|") { it.rarityName }})")
        }
    }
}