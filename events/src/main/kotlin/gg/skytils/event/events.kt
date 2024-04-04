/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

suspend fun <T : Event> post(event: T) =
    EventPriority.Highest.post(event)

fun <T : Event> postSync(event: T) =
    runBlocking {
        post(event)
    }

suspend fun <T : CancellableEvent> postCancellable(event: T) =
    coroutineScope {
        EventPriority.Highest.post(event)
        return@coroutineScope event.cancelled
    }

fun <T : CancellableEvent> postCancellableSync(event: T) =
    runBlocking {
        postCancellable(event)
    }

suspend inline fun <reified T : Event> on(priority: EventPriority = EventPriority.Normal, noinline block: suspend (T) -> Unit) =
    priority.subscribe<T>(block)

suspend inline fun <reified T : Event> await(priority: EventPriority = EventPriority.Normal) =
    priority.flow.filterIsInstance<T>().first()

suspend inline fun <reified T : Event> await(repetitions: Int, priority: EventPriority = EventPriority.Normal) = run {
    assert(repetitions >= 1) { "Expected repetitions to be at least 1 but received $repetitions" }
    var counter = 0
    priority.flow.filterIsInstance<T>().first { counter++ == repetitions }
}