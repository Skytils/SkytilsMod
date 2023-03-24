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

package gg.skytils.skytilsmod.gui.profile.states

import gg.essential.elementa.state.State

open class alwaysUpdateState<T>(protected var valueBacker: T) : State<T>() {
    override fun get() = valueBacker

    override fun set(value: T) {
        valueBacker = value
        super.set(value)
    }
}

open class alwaysUpdateMappedState<T, U>(initialState: State<T>, private val mapper: (T) -> U) :
    alwaysUpdateState<U>(mapper(initialState.get())) {
    private var removeListener = initialState.onSetValue {
        set(mapper(it))
    }

    /**
     * Changes the state that this state maps from.
     *
     * This method calls [State.set], and will trigger
     * all of its listeners.
     */
    fun rebind(newState: State<T>) {
        removeListener()
        removeListener = newState.onSetValue {
            set(mapper(it))
        }
        set(mapper(newState.get()))
    }
}

fun <T, U> State<T>.alwaysMap(mapper: (T) -> U) = alwaysUpdateMappedState(this, mapper)