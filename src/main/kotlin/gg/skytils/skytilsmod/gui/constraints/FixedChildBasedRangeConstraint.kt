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

package gg.skytils.skytilsmod.gui.constraints

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.HeightConstraint
import gg.essential.elementa.constraints.WidthConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor

// TODO: REMOVE WHEN ELEMENTA UPDATED IN ESSENTIAL
class FixedChildBasedRangeConstraint : WidthConstraint, HeightConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getWidthImpl(component: UIComponent): Float {
        var leftMostPoint = Float.POSITIVE_INFINITY
        var rightMostPoint = Float.NEGATIVE_INFINITY

        component.children.takeIf { it.isNotEmpty() }?.forEach {
            if (it.getLeft() < leftMostPoint) {
                leftMostPoint = it.getLeft()
            }

            if (it.getRight() > rightMostPoint) {
                rightMostPoint = it.getRight()
            }
        } ?: run {
            leftMostPoint = component.getLeft()
            rightMostPoint = leftMostPoint
        }

        return (rightMostPoint - leftMostPoint).coerceAtLeast(0f)
    }

    override fun getHeightImpl(component: UIComponent): Float {
        var topMostPoint = Float.POSITIVE_INFINITY
        var bottomMostPoint = Float.NEGATIVE_INFINITY

        component.children.takeIf { it.isNotEmpty() }?.forEach {
            if (it.getTop() < topMostPoint) {
                topMostPoint = it.getTop()
            }

            if (it.getBottom() > bottomMostPoint) {
                bottomMostPoint = it.getBottom()
            }
        } ?: run {
            topMostPoint = component.getTop()
            bottomMostPoint = topMostPoint
        }

        return (bottomMostPoint - topMostPoint).coerceAtLeast(0f)
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        when (type) {
            ConstraintType.WIDTH -> {
                visitor.visitChildren(ConstraintType.X)
                visitor.visitChildren(ConstraintType.WIDTH)
            }
            ConstraintType.HEIGHT -> {
                visitor.visitChildren(ConstraintType.Y)
                visitor.visitChildren(ConstraintType.HEIGHT)
            }
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }
}