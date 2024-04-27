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

import gg.essential.universal.UChat

object DevTools {
    val toggles = HashMap<String, Boolean>()
    var allToggle = false
        private set


    fun getToggle(toggle: String): Boolean {
        return if (allToggle) allToggle else toggles.getOrDefault(toggle.lowercase(), false)
    }

    fun toggle(toggle: String) {
        if (toggle.lowercase() == "all") {
            allToggle = !allToggle
            return
        }
        toggles[toggle.lowercase()]?.let {
            toggles[toggle.lowercase()] = !it
        } ?: kotlin.run {
            toggles[toggle.lowercase()] = true
        }
    }

}

fun printDevMessage(string: String, toggle: String) {
    if (DevTools.getToggle(toggle)) UChat.chat(string)
}

fun printDevMessage(string: String, vararg toggles: String) {
    if (toggles.any { DevTools.getToggle(it) }) UChat.chat(string)
}