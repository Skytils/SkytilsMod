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

package gg.skytils.skytilsmod.utils.elementa

import gg.essential.elementa.UIComponent
import gg.essential.elementa.UIConstraints
import gg.essential.elementa.constraints.*
import kotlin.reflect.KMutableProperty1

interface Constraints {
    fun apply(component: UIComponent): () -> Unit

    operator fun plus(other: Constraints) = if (other === Constraints) this else CombinedConstraints(this, other)

    companion object : Constraints {
        override fun apply(component: UIComponent) = {}
    }
}

private class CombinedConstraints(
    private val first: Constraints,
    private val second: Constraints
) : Constraints {
    override fun apply(component: UIComponent): () -> Unit {
        val undoFirst = first.apply(component)
        val undoSecond = second.apply(component)
        return {
            undoFirst()
            undoSecond()
        }
    }
}

operator fun Constraints.plus(block: UIComponent.() -> (() -> Unit)) =
    this + object : Constraints {
        override fun apply(component: UIComponent): () -> Unit {
            return component.block()
        }
    }

sealed class Constraint<T: SuperConstraint<*>>(private val field: KMutableProperty1<UIConstraints, T>) {

    fun set(receiver: UIConstraints, value: T) =
        field.set(receiver, value)

    fun get(receiver: UIConstraints) =
        field.get(receiver)

    object X: Constraint<XConstraint>(UIConstraints::x)
    object Y: Constraint<YConstraint>(UIConstraints::y)
    object Width: Constraint<WidthConstraint>(UIConstraints::width)
    object Height: Constraint<HeightConstraint>(UIConstraints::height)
}