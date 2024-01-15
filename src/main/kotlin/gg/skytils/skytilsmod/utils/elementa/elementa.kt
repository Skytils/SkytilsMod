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
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain

class ComponentBuilder(private val parent: UIComponent) {
    var name: String by parent::componentName

    fun constraints(block: Constraints.() -> Constraints) {
        Constraints.block().apply(parent)
    }

    fun UIComponent.build(constraints: Constraints = Constraints, block: ComponentBuilder.() -> Unit) =
        invoke(constraints, block)

    operator fun <T: UIComponent> T.invoke(constraints: Constraints = Constraints, block: ComponentBuilder.() -> Unit = {}) =
        apply {
            constrain {
                x = SiblingConstraint()
                y = SiblingConstraint()
            }
            constraints.apply(this)
            ComponentBuilder(this).block()
            this childOf this@ComponentBuilder.parent
        }
}

fun UIComponent.build(constraints: Constraints = Constraints, block: ComponentBuilder.() -> Unit) =
    apply {
        constraints.apply(this)
        ComponentBuilder(this).block()
    }