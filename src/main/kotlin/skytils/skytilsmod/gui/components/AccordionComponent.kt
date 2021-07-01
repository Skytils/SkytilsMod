/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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

package skytils.skytilsmod.gui.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import java.awt.Color

class AccordionComponent(val t: String) : UIBlock(Color(22, 22, 24)) {
    var toggle = false
    val contents = ArrayList<UIComponent>()
    val button = SimpleButton(t)
        .constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = RelativeConstraint()
        } childOf this

    init {
        button
            .onMouseClick {
                if (toggle) {
                    toggle = false
                    _hide()
                } else {
                    toggle = true
                    _show()
                }
            }
    }

    override fun afterInitialization() {
        if (!toggle) {
            _hide(true)
        } else {
            _show()
        }
        super.afterInitialization()
    }

    fun AccordionComponent.adjustParentHeight(height: Float, instantly: Boolean = false) {
        var thing: UIComponent? = this@AccordionComponent
        var otherThing: UIComponent? = this@AccordionComponent
        var totalHeight = height
        while (true) {
            thing = thing?.parent
            if (thing != null && thing is AccordionComponent) {
                totalHeight += thing.children.filter { it != otherThing }.sumOf { it.getHeight().toDouble() }.toFloat()
                thing.animate {
                    setHeightAnimation(
                        Animations.IN_OUT_QUAD,
                        if (instantly) 0f else 1f,
                        basicHeightConstraint { totalHeight }
                    )
                }
                otherThing = thing
            } else {
                break
            }
        }
    }

    fun _hide(instantly: Boolean = false) {
        this.animate {
            setHeightAnimation(
                Animations.IN_OUT_QUAD,
                if (instantly) 0f else 1f,
                basicHeightConstraint { button.getHeight() }
            )
        }
        if (this.parent is AccordionComponent && (this.parent as AccordionComponent).toggle) {
            this.adjustParentHeight(button.getHeight(), instantly)
        }
    }

    fun _show() {
        val totalHeight = this.children.sumOf {
            it.getHeight().toDouble()
        }
        this.animate {
            setHeightAnimation(
                Animations.IN_OUT_QUAD,
                1f,
                basicHeightConstraint { totalHeight.toFloat() }
            )
        }
        if (this.parent is AccordionComponent) {
            this.adjustParentHeight(totalHeight.toFloat())
        }
    }

}