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

fun Constraints.xConstraint(value: XConstraint) =
    this + {
        val oldX = this.constraints.x
        setX(value);
        {
            setX(oldX)
        }
    }

fun Constraints.yConstraint(value: YConstraint) =
    this + {
        val oldY = this.constraints.y
        setY(value);
        {
            setY(oldY)
        }
    }

fun Constraints.widthConstraint(value: WidthConstraint) =
    this + {
        val oldWidth = this.constraints.width
        setWidth(value);
        {
            setWidth(oldWidth)
        }
    }

fun Constraints.heightConstraint(value: HeightConstraint) =
    this + {
        val oldHeight = this.constraints.height
        setHeight(value);
        {
            setHeight(oldHeight)
        }
    }

fun Constraints.colorConstraint(value: ColorConstraint) =
    this + {
        val oldColor = this.constraints.color
        setColor(value);
        {
            setColor(oldColor)
        }
    }