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

import gg.essential.elementa.components.UIText
import gg.essential.elementa.layoutdsl.LayoutScope
import gg.essential.elementa.layoutdsl.Modifier
import gg.essential.elementa.state.v2.State
import gg.essential.elementa.state.v2.effect
import gg.essential.elementa.state.v2.stateOf

fun LayoutScope.text(text: State<String>, modifier: Modifier = Modifier): UIText {
    val component = UIText()
    effect(component) {
        component.setText(text())
    }
    return component(modifier)
}

fun LayoutScope.text(text: String, modifier: Modifier = Modifier): UIText = text(stateOf(text), modifier)