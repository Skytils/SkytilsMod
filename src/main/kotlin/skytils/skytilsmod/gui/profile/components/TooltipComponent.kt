/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.GradientComponent
import gg.essential.elementa.components.GradientComponent.Companion.drawGradientBlock
import gg.essential.elementa.components.GradientComponent.GradientDirection.TOP_TO_BOTTOM
import gg.essential.elementa.components.UIBlock.Companion.drawBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.UMatrixStack
import java.awt.Color

/**
 * Based on UITooltipComponent from ReplayMod
 * https://github.com/ReplayMod/ReplayMod/blob/develop/src/main/kotlin/com/replaymod/core/gui/common/UITooltip.kt
 * Licensed under GNU GPLv3
 */
class TooltipComponent : UIComponent() {

    init {
        constrain {
            width = ChildBasedMaxSizeConstraint() + 8.pixels
            height = ChildBasedSizeConstraint() + 8.pixels
        }
    }

    val content by UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = ChildBasedMaxSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf this

    fun addLine(text: String = "", configure: UIText.() -> Unit = {}) = apply {
        val component = UIText(text).constrain {
            y = SiblingConstraint(padding = 3f)
        } childOf content
        component.configure()
    }

    fun addSpacer() = apply {
        val component = UIContainer().constrain {
            y = SiblingConstraint(padding = 3f)
            height = 9.pixels
            width = 5.pixels
        } childOf content
    }

    override fun draw(matrixStack: UMatrixStack) {
        beforeDraw(matrixStack)

        val l = getLeft().toDouble()
        val r = getRight().toDouble()
        val t = getTop().toDouble()
        val b = getBottom().toDouble()

        // Draw background
        drawBlock(matrixStack, BACKGROUND_COLOR, l + 1, t, r - 1, b) // Top to bottom
        drawBlock(matrixStack, BACKGROUND_COLOR, l, t + 1, l + 1, b - 1) // Left pixel row
        drawBlock(matrixStack, BACKGROUND_COLOR, r - 1, t + 1, r, b - 1) // Right pixel row

        // Draw the border, it gets darker from top to bottom
        drawBlock(matrixStack, BORDER_LIGHT, l + 1, t + 1, r - 1, t + 2) // Top border
        drawBlock(matrixStack, BORDER_DARK, l + 1, b - 2, r - 1, b - 1) // Bottom border
        drawGradientBlock(matrixStack, l + 1, t + 2, l + 2, b - 2, BORDER_LIGHT, BORDER_DARK, TOP_TO_BOTTOM) // Left border
        drawGradientBlock(matrixStack, r - 2, t + 2, r - 1, b - 2, BORDER_LIGHT, BORDER_DARK, TOP_TO_BOTTOM) // Right border

        super.draw(matrixStack)
    }

    companion object {
        private val BACKGROUND_COLOR = Color(16, 0, 16, 240)
        private val BORDER_LIGHT = Color(80, 0, 255, 80)
        private val BORDER_DARK = Color(40, 0, 127, 80)
    }
}