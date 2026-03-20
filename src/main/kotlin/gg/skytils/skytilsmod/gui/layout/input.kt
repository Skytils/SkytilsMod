/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.gui.layout

import gg.essential.elementa.dsl.width
import gg.essential.elementa.unstable.layoutdsl.*
import gg.essential.elementa.unstable.state.v2.MutableState
import gg.essential.elementa.unstable.state.v2.State
import gg.essential.universal.USound
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.gui.components.UIFilteringTextInput
import java.awt.Color

fun LayoutScope.button(text: State<String>, onClick: () -> Unit) {
    row(Modifier.hoverScope().animateColor(Color(0, 0, 0, 80)).hoverColor(Color(255, 255, 255, 80)).childBasedWidth(40f).childBasedHeight(10f)) {
        column {
            text(text, Modifier.inheritHoverScope().animateColor(Color(0xe0e0e0)).hoverColor(Color(0xffffa0)))
        }
    }.onLeftClick {
        USound.playButtonPress()
        onClick()
    }
}

fun LayoutScope.mcTextInput(text: MutableState<String>, placeholderText: String = "", modifier: Modifier = Modifier): UIFilteringTextInput {
    val input = UIFilteringTextInput(placeholderText, shadow = true)
    box(Modifier.childBasedSize(1f).color(Color(0xa0a0a0)).onLeftClick { input.grabWindowFocus() }) {
        box(Modifier.childBasedWidth(4f).childBasedHeight(4f).color(Color(0x000000)).onLeftClick { input.grabWindowFocus() }) {
            input(Modifier.width(placeholderText.width() + 32f).height(12f).then(modifier))
        }
    }
    input.onUpdate(text::set)
    return input
}