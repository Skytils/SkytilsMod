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

package gg.skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.components.GradientComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import gg.essential.elementa.utils.withAlpha
import gg.skytils.skytilsmod.utils.NumberUtil
import java.awt.Color

open class XPComponent(
    itemComponent: ItemComponent,
    text: String = "",
    percent: Float = 0f,
    overflow: Long = 0,
    var colorConstraint: ColorConstraint = Color(0x4166f5).constraint
) : UIContainer() {
    // to get around leaking `this` into ctor
    private fun that() = this

    private var textState: State<String> = BasicState(text)
    private var percentState: State<Float> = BasicState(percent)
    private var overflowState: State<Long> = BasicState(overflow)

    fun bindText(newState: State<String>) = apply {
        textState = newState
        textLabel.bindText(textState)
    }

    fun bindPercent(newState: State<Float>) = apply {
        percentState = newState
        progress.constraints.width =
            (background.constraints.width - imageContainer.constraints.width) * (percentState as State<Number>) + imageContainer.constraints.width
    }

    fun bindOverflow(newState: State<Long>) = apply {
        overflowState = newState
        overflowText.bindText(overflowState.map { NumberUtil.format(it) })
    }

    private val background = UIRoundedRectangle(5f)
        .constrain {
            x = basicXConstraint { getHeight() / 2 + getLeft() }
            y = RelativeConstraint(0.5f)
            width = FillConstraint(useSiblings = false)
            height = RelativeConstraint(0.5f)
            color = Color.WHITE.withAlpha(40).toConstraint()
        } childOf that()

    private val imageContainer = UIRoundedRectangle(5f)
        .constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = basicWidthConstraint { getHeight() }
            height = RelativeConstraint()
            color = colorConstraint
        } childOf that()

    private val progress = UIRoundedRectangle(5f)
        .constrain {
            x = 0.pixels()
            y = 0.pixels()
            width =
                (background.constraints.width - imageContainer.constraints.width) * (percentState as State<Number>) + imageContainer.constraints.width
            height = RelativeConstraint()
            color = colorConstraint
        } childOf background

    private val overflowText =
        UIText().also { it.bindText(overflowState.map { NumberUtil.format(it) }) }
            .constrain {
                x = CenterConstraint()
                y = CenterConstraint()
            } childOf background


    private val shadow by GradientComponent(
        Color(0, 0, 0, 80),
        Color(0, 0, 0, 0),
        GradientComponent.GradientDirection.LEFT_TO_RIGHT
    )
        .constrain {
            x = RelativeConstraint(0.025f)
            y = 0.pixels()
            width = RelativeConstraint(0.1f)
            height = RelativeConstraint()
        } childOf background

    private val textContainer = UIContainer()
        .constrain {
            x = basicXConstraint { imageContainer.getRight() }
            y = 0.pixels()
            width = FillConstraint()
            height = RelativeConstraint(0.5f)
        } childOf that()

    private val textLabel =
        UIText(
        ).also {
            it.bindText(textState)
        }
            .constrain {
                x = 2.pixels()
                y = CenterConstraint()
            } childOf textContainer


    init {
        itemComponent
            .constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                height = RelativeConstraint(0.9f)
                width = RelativeConstraint(0.9f)
            } childOf imageContainer
    }
}