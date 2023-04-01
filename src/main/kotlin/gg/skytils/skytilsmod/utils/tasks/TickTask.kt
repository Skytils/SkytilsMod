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

package gg.skytils.skytilsmod.utils.tasks

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class TickTask<T>(
    val ticks: Int = 0,
    val repeats: Boolean = false,
    register: Boolean = true,
    val task: (CoroutineContext) -> T
) {
    private var callback: (T) -> Unit = {}
    private var failure: (Throwable) -> Unit = {}
    private var job: Job? = null
    private val delayDuration = ticks * 50L


    init {
        if (register) {
            register()
        }
    }

    fun register() {
        if (job != null) error("Task is already registered!")
        job = mcScope.launch {
            while (isActive) {
                delay(delayDuration)
                complete(coroutineContext)
                if (!repeats) break
            }
        }
    }

    fun unregister() {
        job?.cancel() ?: error("Task is not registered!")
        job = null
    }

    private fun complete(ctx: CoroutineContext) = try {
        callback(task(ctx))
    } catch (t: Throwable) {
        failure(t)
    }

    fun onComplete(block: (T) -> Unit) = apply {
        callback = block
    }

    fun onFailure(block: (Throwable) -> Unit) = apply {
        failure = block
    }
}