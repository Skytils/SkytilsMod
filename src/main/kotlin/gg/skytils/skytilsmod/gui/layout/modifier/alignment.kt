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

package gg.skytils.skytilsmod.gui.layout.modifier

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.MousePositionConstraint
import gg.essential.elementa.constraints.PositionConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.dsl.plus
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.layoutdsl.*

class OffsetMouseAlignment(val x: Float = 0f, val y: Float = 0f) : Alignment {
    override fun applyHorizontal(component: UIComponent): () -> Unit {
        return BasicXModifier { MousePositionConstraint() + x.pixels }.applyToComponent(component)
    }

    override fun applyVertical(component: UIComponent): () -> Unit {
        return BasicYModifier { MousePositionConstraint() + y.pixels }.applyToComponent(component)
    }
}

private class BasicAlignment(private val constraintFactory: () -> PositionConstraint) : Alignment {
    override fun applyHorizontal(component: UIComponent): () -> Unit {
        return BasicXModifier(constraintFactory).applyToComponent(component)
    }

    override fun applyVertical(component: UIComponent): () -> Unit {
        return BasicYModifier(constraintFactory).applyToComponent(component)
    }
}

fun Alignment.Companion.Relative(percent: Float): Alignment = BasicAlignment { RelativeConstraint(percent) }