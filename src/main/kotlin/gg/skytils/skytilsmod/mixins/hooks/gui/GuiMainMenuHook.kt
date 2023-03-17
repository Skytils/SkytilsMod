/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2023 Skytils
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

package gg.skytils.skytilsmod.mixins.hooks.gui

import gg.essential.universal.ChatColor
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiMainMenu
import gg.skytils.skytilsmod.utils.NumberUtil.addSuffix
import net.minecraft.client.gui.GuiMainMenu
import java.util.*

fun setSplashText(gui: GuiMainMenu, cal: Calendar) {
    gui as AccessorGuiMainMenu
    if (cal.get(Calendar.MONTH) + 1 == 2 && cal.get(Calendar.DATE) == 6) {
        val numBirthday = cal.get(Calendar.YEAR) - 2021
        gui.splashText = "§zHappy ${numBirthday.addSuffix()} Birthday Skytils!"
        if (!Skytils.usingSBA) gui.splashText = addColor("Happy ${numBirthday.addSuffix()} Birthday Skytils!", 0)
    }
}

val colors = listOf(
    ChatColor.RED,
    ChatColor.GOLD,
    ChatColor.YELLOW,
    ChatColor.GREEN,
    ChatColor.AQUA,
    ChatColor.BLUE,
    ChatColor.DARK_PURPLE,
    ChatColor.LIGHT_PURPLE
)

fun addColor(str: String, seed: Int): String {
    var offset = 0
    return str.split(' ').joinToString(separator = " ") { s ->
        val a = s.mapIndexed { i, c ->
            "${colors[(i + seed + offset) % colors.size]}$c"
        }.joinToString(separator = "")
        offset += s.length
        a
    }
}