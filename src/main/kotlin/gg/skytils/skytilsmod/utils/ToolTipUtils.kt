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

package gg.skytils.skytilsmod.utils

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.MousePositionConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.events.UIScrollEvent
import gg.essential.elementa.utils.ObservableClearEvent
import gg.essential.elementa.utils.ObservableRemoveEvent
import java.util.Observer

/**
 * All of this is from Replaymod - https://github.com/ReplayMod/ReplayMod
 * Licensed under GNU GPLv3
 * Modified by Skytils
 * <3 love you johni
 */

// Elementa has no unmount event, so instead we listen for changes to the children list of all our parents.
fun UIComponent.onRemoved(listener: () -> Unit): () -> Unit {
    if (parent == this) {
        return {}
    }
    val parentUnregister = parent.onRemoved(listener)

    val observer = Observer { _, event ->
        if (event is ObservableClearEvent<*> || event is ObservableRemoveEvent<*> && event.element.value == this) {
            listener()
        }
    }
    parent.children.addObserver(observer)
    return {
        parent.children.deleteObserver(observer)
        parentUnregister()
    }
}

fun <T : UIComponent> T.addTooltip(tooltip: UIComponent) = apply {
    fun <U : UIComponent> U.resetTooltip() = apply {
        constrain {
            // Slightly right of the cursor but never off-screen
            x = (MousePositionConstraint() + 8.pixels)
                .coerceAtMost(100.percentOfWindow - basicXConstraint { tooltip.getWidth() })
            // Slightly below the cursor except when there is insufficient space, then slightly above it
            y = basicYConstraint {
                val mouseY = MousePositionConstraint().getYPosition(it)
                val idealY = mouseY + 8
                val height = tooltip.getHeight()
                val fallbackY = mouseY - 8 - height
                if (idealY + height <= 100.percentOfWindow.getYPosition(it) || fallbackY < 0) {
                    idealY
                } else {
                    fallbackY
                }
            }
        }
    }
    tooltip.resetTooltip()

    var unregister: (() -> Unit)? = null

    onMouseEnter {
        Window.enqueueRenderOperation {
            tooltip.resetTooltip()
            tooltip childOf Window.of(this)
            tooltip.setFloating(true)
        }
        val scroll: UIComponent.(UIScrollEvent) -> Unit = { event ->
            Window.enqueueRenderOperation {
                tooltip.constraints.y += (if (event.delta > 0) 10 else -10).pixels
            }
        }
        Window.of(this).onMouseScroll(scroll)
        val unregisterOnRemoved = this.onRemoved {
            unregister?.invoke()
            unregister = null
        }
        unregister = {
            unregisterOnRemoved.invoke()
            Window.enqueueRenderOperation {
                Window.of(this).mouseScrollListeners.remove(scroll)
                tooltip.setFloating(false)
                tooltip.hide(true)
            }
        }
    }
    onMouseLeave {
        unregister?.invoke()
        unregister = null
    }
}