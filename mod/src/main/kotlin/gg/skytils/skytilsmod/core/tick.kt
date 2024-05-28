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

import gg.essential.universal.UChat
import gg.skytils.event.await
import gg.skytils.event.impl.TickEvent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
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
            await<TickEvent>(ticks)
            emit(withContext(Dispatchers.MC) {
                task()
            })
        } while (repeats)
    }.catch {e ->
        if (e is RuntimeException) {
            e.printStackTrace()
            UChat.chat("${Skytils.failPrefix} §cSkytils ${Skytils.VERSION} caught and logged an ${e::class.simpleName ?: "error"} on a tick task. Please report this on the Discord server at discord.gg/skytils.")
        } else throw e
    }