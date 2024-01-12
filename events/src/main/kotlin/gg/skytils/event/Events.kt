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

package gg.skytils.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

object Events {
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    suspend fun <T : Event> post(event: T) =
        _events.emit(event)

    fun <T : Event> postSync(event: T) =
        runBlocking {
            post(event)
        }

    suspend inline fun <reified T : Event> on(noinline block: suspend (T) -> Unit) =
        events.filterIsInstance<T>().onEach(block).launchIn(CoroutineScope(currentCoroutineContext()))

    suspend inline fun <reified T : Event> await() =
        events.filterIsInstance<T>().first()

    suspend inline fun <reified T : Event> await(repetitions: Int) = run {
        assert(repetitions >= 1) { "Expected repetitions to be at least 1 but received $repetitions" }
        var counter = 0
        events.filterIsInstance<T>().first { counter++ == repetitions }
    }
}