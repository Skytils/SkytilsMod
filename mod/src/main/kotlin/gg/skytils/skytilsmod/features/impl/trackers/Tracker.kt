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

package gg.skytils.skytilsmod.features.impl.trackers

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import java.io.File

abstract class Tracker(val id: String) : PersistentSave(File(File(Skytils.modDir, "trackers"), "$id.json")) {
    companion object {
        val TRACKERS = HashSet<Tracker>()

        fun getTrackerById(id: String): Tracker? {
            return TRACKERS.find { it.id == id }
        }
    }

    init {
        TRACKERS.add(this)
    }

    fun doReset() {
        resetLoot()
        markDirty(this::class)
    }

    protected abstract fun resetLoot()
}