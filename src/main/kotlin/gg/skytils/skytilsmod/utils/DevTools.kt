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

import gg.essential.elementa.state.v2.MutableState
import gg.essential.elementa.state.v2.State
import gg.essential.elementa.state.v2.memo
import gg.essential.elementa.state.v2.mutableStateOf
import gg.essential.universal.UChat

object DevTools {
    private val toggles = mutableStateOf(mapOf<String, Boolean>())
    private val _allToggleState: MutableState<Boolean> = mutableStateOf(false)
    val allToggleState: State<Boolean>
        get() = _allToggleState
    val allToggle : Boolean
        get() = allToggleState.getUntracked()

    fun getToggle(toggle: String): Boolean =
        getToggleState(toggle).getUntracked()

    fun getToggleState(toggle: String) = memo {
        allToggleState() || (toggles()[toggle] ?: false)
    }

    operator fun get(toggle: String) = memo {
        allToggleState() || toggles()[toggle] ?: false
    }

    fun toggle(toggle: String) {
        if (toggle == "all") {
            _allToggleState.set { !it }
            return
        }
        toggles.set { it + (toggle to (it[toggle]?.not() ?: true)) }
    }

}

fun printDevMessage(string: String, toggle: String) {
    if (DevTools.getToggle(toggle)) UChat.chat(string)
}

fun printDevMessage(string: String, vararg toggles: String) {
    if (toggles.any { DevTools.getToggle(it) }) UChat.chat(string)
}

fun printDevMessage(func: () -> String, toggle: String) {
    if (DevTools.getToggle(toggle)) UChat.chat(func())
}

fun printDevMessage(func: () -> String, vararg toggles: String) {
    if (toggles.any { DevTools.getToggle(it) }) UChat.chat(func())
}