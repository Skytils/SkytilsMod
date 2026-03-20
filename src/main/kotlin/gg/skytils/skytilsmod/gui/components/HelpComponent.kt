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

package gg.skytils.skytilsmod.gui.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CoerceAtLeastConstraint
import gg.essential.elementa.constraints.ConstantColorConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import java.awt.Color

/**
 * A component with a "?" inside of a circle that displays a block of text when hovered.
 */
class HelpComponent(parentComponent: UIComponent, tooltipText: String) : UIBlock() {
    init {
        // Tooltip Block
        constrain {
            x = 5.pixels()
            y = basicYConstraint { it.parent.getTop() - it.getHeight() - 6 }
            height = ChildBasedSizeConstraint(4f)
            width = ChildBasedSizeConstraint(4f)
            color = ConstantColorConstraint(Color(112, 112, 112, 200))
        }
        effect(OutlineEffect(Color(0, 243, 255), 1f))
        hide()

        addChild(UIWrappedText(tooltipText).constrain {
            x = 2.pixels()
            y = 0.pixels()
            width = CoerceAtLeastConstraint(50.percentOfWindow(), 200.pixels())
        })

        UICircle(7f).childOf(parentComponent).constrain {
            x = 9.pixels()
            y = basicYConstraint { it.parent.getBottom() - it.getHeight() - 2 }
        }.addChildren(
            UIText("?", true).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
            },
            this
        ).onMouseEnter {
            this@HelpComponent.unhide()
        }.onMouseLeave {
            this@HelpComponent.hide()
        }
    }
}