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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*

enum class EventPriority {
    Lowest {
        override val next: EventPriority = this

        override suspend fun <T : Event> post(event: T) {
            flow.emit(event)
        }
    },
    Low {
        override val next: EventPriority = Lowest
    },
    Normal {
        override val next: EventPriority = Low
    },
    High {
        override val next: EventPriority = Normal
    },
    Highest {
        override val next: EventPriority = High
    };

    @PublishedApi
    internal val flow: MutableSharedFlow<Event> = MutableSharedFlow()
    internal abstract val next: EventPriority

    @PublishedApi
    internal suspend inline fun <reified T : Event> subscribe(noinline block: suspend (T) -> Unit) =
        flow.filterIsInstance<T>().onEach(block).launchIn(CoroutineScope(currentCoroutineContext()))

    internal open suspend fun <T : Event> post(event: T) {
        flow.emit(event)
        if (!event.continuePropagation()) return
        next.post(event)
    }
}