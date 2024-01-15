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
import gg.skytils.skytilsmod.Skytils.Companion.mc
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.concurrent.Executor


val mcDispatcher = (mc as Executor).asCoroutineDispatcher()
val mcScope = CoroutineScope(mcDispatcher) + SupervisorJob() + CoroutineName("Skytils MC")

val Dispatchers.MC
    get() = mcDispatcher

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
    flow {
        do {
            Events.await<TickEvent>(ticks)
            emit(withContext(Dispatchers.MC) {
                task()
            })
        } while (repeats)
    }.flowOn(Tick.dispatcher)