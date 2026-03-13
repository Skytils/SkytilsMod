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

package gg.skytils.skytilsmod.gui.view

import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.MappedState
import gg.essential.elementa.state.State
import gg.skytils.skytilsmod.core.mcScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class LateinitState<T> : State<T>() {
    internal open var backer: T? = null
    override fun get(): T =
        backer ?: error("State should be initialized before get")

    override fun set(value: T) {
        backer = value
        super.set(value)
    }
}

class AsyncMappedState<T, U>(val state: State<T>, val scope: CoroutineScope = mcScope, val mapper: suspend (T) -> U, initialValue: U? = null) : LateinitState<U>() {
    override var backer: U? = initialValue

    private var removeListener = state.onSetValue {
        scope.launch {
            set(mapper(it))
        }
    }

    init {
        if (state !is LateinitState) {
            scope.launch {
                set(mapper(state.get()))
            }
        }
    }

    fun rebind(newState: State<T>) {
        removeListener()
        removeListener = state.onSetValue {
            scope.launch {
                set(mapper(it))
            }
        }
    }
}

fun <T, U> State<T>.asyncMap(initialValue: U? = null, scope: CoroutineScope = mcScope, mapper: suspend (T) -> U) = AsyncMappedState(this, scope, mapper, initialValue)