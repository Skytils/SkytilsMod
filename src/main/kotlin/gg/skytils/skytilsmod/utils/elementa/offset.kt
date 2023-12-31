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
import gg.essential.elementa.constraints.SuperConstraint
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus

class OffsetConstraints<T : SuperConstraint<Float>> internal constructor(val constraint: Constraint<T>, val offset: T) : Constraints {

    @Suppress("unchecked_cast")
    override fun apply(component: UIComponent): () -> Unit {
        val oldValue = constraint.get(component.constraints)

        constraint.set(component.constraints, oldValue.plus(offset) as T)

        return {
            constraint.set(component.constraints, oldValue)
        }
    }
}


@Suppress("unchecked_cast")
infix fun <T : SuperConstraint<Float>> Constraint<T>.by(value: Float) =
    OffsetConstraints(this, value.pixels as T)

infix fun <T : SuperConstraint<Float>> Constraint<T>.by(constraint: T) =
    OffsetConstraints(this, constraint)

fun Constraints.offset(vararg offsets: OffsetConstraints<out SuperConstraint<Float>>) =
    offsets.fold(this, Constraints::plus)

fun <T : SuperConstraint<Float>> Constraints.expand(vararg offsets: OffsetConstraints<T>) =
    offsets.fold(this, Constraints::plus)