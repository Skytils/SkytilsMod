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

import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.pixels

fun Constraints.width(width: Number) =
    widthConstraint(width.pixels)

fun Constraints.height(height: Number) =
    heightConstraint(height.pixels)

fun Constraints.squareSize(size: Number) =
    width(size).height(size)

fun Constraints.fillWidth(fraction: Float = 1f) = widthConstraint(RelativeConstraint(fraction))

fun Constraints.fillHeight(fraction: Float = 1f) = heightConstraint(RelativeConstraint(fraction))

fun Constraints.fillParent(fraction: Float = 1f) = fillWidth(fraction).fillHeight(fraction)

fun Constraints.fillScreenWidth(fraction: Float = 1f) = widthConstraint(RelativeWindowConstraint(fraction))

fun Constraints.fillScreenHeight(fraction: Float = 1f) = heightConstraint(RelativeWindowConstraint(fraction))



fun Constraints.fillRemainingWidth() = widthConstraint(FillConstraint())

fun Constraints.fillRemainingHeight() = heightConstraint(FillConstraint())

fun Constraints.childBasedWidth() = widthConstraint(ChildBasedRangeConstraint())

fun Constraints.childBasedHeight() = heightConstraint(ChildBasedRangeConstraint())

fun Constraints.childBasedSize() = childBasedWidth().childBasedHeight()