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
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.*
import java.awt.Color

class AccordionComponent(val t: String) : UIBlock(Color(22, 22, 24)) {
    var toggle = false
    val contents = ArrayList<UIComponent>()
    var afterHeightChange: (Float) -> Unit = { }
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

    fun _hide(instantly: Boolean = false) {
        this.animate {
            setHeightAnimation(
                Animations.IN_OUT_QUAD,
                if (instantly) 0f else 1f,
                basicHeightConstraint { button.getHeight() }
            ).onComplete {
                afterHeightChange(button.getHeight())
            }
        }
    }

    fun _show() {
        this.animate {
            setHeightAnimation(
                Animations.IN_OUT_QUAD,
                1f,
                ChildBasedSizeConstraint()
            ).onComplete {
                afterHeightChange(ChildBasedSizeConstraint().cachedValue)
            }
        }
    }

    fun afterHeightChange(listener: (Float) -> Unit) {
        afterHeightChange = listener
    }

}