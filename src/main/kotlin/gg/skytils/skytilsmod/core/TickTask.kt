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

import gg.skytils.skytilsmod.utils.Utils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

class TickTask<T>(val ticks: Int = 0, val repeats: Boolean = false, register: Boolean = true, val task: () -> T) {
    var remainingTicks = ticks
    private var callback: (T) -> Unit = {}
    private var failure: (Throwable) -> Unit = {}

    init {
        if (register)
            register()
    }

    fun register() = Utils.checkThreadAndQueue {
        TickTaskManager.tasks.add(this)
    }

    fun unregister() = Utils.checkThreadAndQueue {
        TickTaskManager.tasks.remove(this)
    }

    internal fun complete() = try {
        callback(task())
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

object TickTaskManager {
    val tasks = mutableSetOf<TickTask<*>>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || tasks.isEmpty()) return
        tasks.removeAll {
            if (it.remainingTicks-- <= 0) {
                it.complete()
                if (it.repeats) {
                    it.remainingTicks = it.ticks
                } else return@removeAll true
            }
            return@removeAll false
        }
    }
}