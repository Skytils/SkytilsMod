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

package gg.skytils.skytilsmod.gui.editing.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.MousePositionConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import java.awt.Color

class LocationComponent(val element: GuiElement) : UIComponent() {
    init {
        constrain {
            x = RelativeConstraint(element.x)
            y = RelativeConstraint(element.y)
            width = element.scaleWidth.pixels
            height = element.scaleHeight.pixels
        }
        onMouseClick { event ->
            when (event.mouseButton) {
                1 -> {
                    constrain {
                        x = 10.pixels
                        y = 10.pixels
                        element.setPos(10, 10)

                        width = element.scaleWidth.pixels
                        height = element.scaleHeight.pixels
                        element.scale = 1f
                    }
                }
                2 -> {
                    element.textShadow = SmartFontRenderer.TextShadow.entries[(element.textShadow.ordinal + 1) % SmartFontRenderer.TextShadow.entries.size]
                }
                else -> {
                    constrain {
                        x = MousePositionConstraint() - event.relativeX.pixels
                        y = MousePositionConstraint() - event.relativeY.pixels
                    }
                }
            }
        }
        onMouseRelease {
            constrain {
                // convert back to relative constraint
                val scaleX = getLeft() / UResolution.scaledWidth
                val scaleY = getTop() / UResolution.scaledHeight
                x = RelativeConstraint(scaleX)
                y = RelativeConstraint(scaleY)
                element.setPos(scaleX, scaleY)
            }
        }
        onMouseScroll { event ->
            element.scale = (element.scale + event.delta).coerceAtLeast(0.01).toFloat()
            constrain {
                width = element.scaleWidth.pixels
                height = element.scaleHeight.pixels
            }
            event.stopImmediatePropagation()
        }
    }

    val background by UIBlock().constrain {
        color = Color.WHITE.withAlpha(40).toConstraint()
        width = RelativeConstraint(1f) + 8.pixels
        height = RelativeConstraint(1f) + 8.pixels
        x = CenterConstraint()
        y = CenterConstraint()
    }.also {
        onMouseEnter {
            it.setColor(Color.WHITE.withAlpha(100))
        }
        onMouseLeave {
            it.setColor(Color.WHITE.withAlpha(40))
        }
    } childOf this

    override fun draw(matrixStack: UMatrixStack) {
        // apply effects
        beforeDraw(matrixStack)
        // draw children
        super.draw(matrixStack)
        matrixStack.push()
        matrixStack.translate(getLeft(), getTop(), 0f)
        matrixStack.scale(element.scale, element.scale, 1f)
        matrixStack.runWithGlobalState(element::demoRender)
        matrixStack.pop()
    }
}