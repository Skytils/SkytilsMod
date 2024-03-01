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

package gg.skytils.skytilsmod.utils.toast

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.skytils.skytilsmod.Skytils

open class Toast(title: String, image: UIComponent? = null, subtext: String = "") : UIContainer() {
    val titleState = BasicState(title)
    val subtextState = BasicState(subtext)

    val background by UIImage.ofResource("/assets/skytils/gui/toast.png").constrain {
        width = ImageAspectConstraint()
        height = 32.pixels
    } childOf this

    init {
        image?.constrain {
            width = 16.pixels
            height = 16.pixels
            y = CenterConstraint()
            x = SiblingConstraint() + 8.pixels
        }?.childOf(background)
    }

    val textContainer by UIContainer().constrain {
        width = (160 - 16 - (if (image != null) 24 else 0)).pixels
        height = 27.pixels
        y = CenterConstraint()
        x = SiblingConstraint() + 8.pixels
    } childOf background

    val title by UIText().bindText(titleState).constrain {
        x = 0.pixels
        y = 0.pixels
    } childOf textContainer

    val subText by UIWrappedText().bindText(subtextState).constrain {
        x = 0.pixels
        y = SiblingConstraint(2f)
        width = RelativeConstraint()
    } childOf textContainer


    init {
        constrain {
            x = 0.pixels(alignOpposite = true, alignOutside = true)
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedMaxSizeConstraint()
        }
    }

    fun animateIn() {
        animate {
            setXAnimation(
                Animations.OUT_EXP,
                0.6f,
                0.pixels(alignOpposite = true)
            )
            onComplete {
                animateOut()
            }
        }
    }

    fun animateOut() {
        animate {
            setXAnimation(
                Animations.IN_EXP,
                0.6f,
                0.pixels(alignOpposite = true, alignOutside = true),
                Skytils.config.toastTime / 1000f
            )
            onComplete {
                hide()
            }
        }
    }
}