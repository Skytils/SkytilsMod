/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.gui.layout.modifier

import gg.essential.elementa.UIComponent
import gg.essential.elementa.events.UIClickEvent
import gg.essential.elementa.unstable.layoutdsl.Modifier
import gg.essential.elementa.unstable.layoutdsl.then

inline fun Modifier.onLeftClickEvent(crossinline callback: UIComponent.(UIClickEvent) -> Unit) = this then  {
    val listener: UIComponent.(event: UIClickEvent) -> Unit = { event ->
        if (event.mouseButton == 0) {
            callback(event)
        }
    }
    onMouseClick(listener)
    return@then { mouseClickListeners.remove(listener) }
}

inline fun Modifier.onMouseRelease(crossinline callback: UIComponent.() -> Unit) = this then {
    val listener: UIComponent.() -> Unit = {
        callback()
    }
    onMouseRelease(listener)
    return@then { mouseReleaseListeners.remove(listener) }
}
