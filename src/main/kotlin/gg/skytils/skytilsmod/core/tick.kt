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

package gg.skytils.skytilsmod.core

import gg.skytils.event.Events
import gg.skytils.event.impl.TickEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

private object Tick : CoroutineScope {
    @OptIn(DelicateCoroutinesApi::class)
    val dispatcher = newFixedThreadPoolContext(5, "Skytils Tick")
    override val coroutineContext = dispatcher + SupervisorJob()
}

fun tickTimer(ticks: Int, repeats: Boolean = false, register: Boolean = true, task: () -> Unit) =
    Tick.launch(start = if (register) CoroutineStart.DEFAULT else CoroutineStart.LAZY) {
        tickTask(ticks, repeats, task).collect()
    }

fun <T> tickTask(ticks: Int, repeats: Boolean = false, task: () -> T) =
    flow<T> {
        do {
            Events.await<TickEvent>(ticks)
            emit(task())
        } while (repeats)
    }