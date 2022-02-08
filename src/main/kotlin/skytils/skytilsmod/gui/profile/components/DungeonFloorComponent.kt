/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedRangeConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.plus
import skytils.hylin.skyblock.dungeons.DungeonBase
import skytils.skytilsmod.gui.constraints.FixedChildBasedRangeConstraint
import kotlin.time.Duration

class DungeonFloorComponent(val dungeonBase: DungeonBase, val floor: Int) : UIRoundedRectangle(5f) {
    init {
        constrain {
            width = ChildBasedMaxSizeConstraint()
            height = FixedChildBasedRangeConstraint() + 10.pixels
        }
        val pairs = arrayListOf<Pair<String, Any>>()
        dungeonBase.timesPlayed?.get(floor)?.apply { pairs.add("Times Played" to this) }
        dungeonBase.watcherKills?.get(floor)?.apply { pairs.add("Watcher Kills" to this) }
        dungeonBase.completions?.get(floor)?.apply { pairs.add("Completions" to this) }
        dungeonBase.bestScores?.get(floor)?.apply { pairs.add("Best Score" to this) }
        dungeonBase.fastestTime?.get(floor)
            ?.apply { pairs.add("Fastest Time" to timeFormat()) }
        dungeonBase.fastestTimeS?.get(floor)
            ?.apply { pairs.add("Fastest Time S" to timeFormat()) }
        dungeonBase.fastestTimeSPlus?.get(floor)
            ?.apply { pairs.add("Fastest Time S+" to timeFormat()) }

        UIWrappedText(pairs.joinToString("\n") {
            "§7${it.first}: §f${it.second}"
        }).constrain {
            x = 5.pixels
            y = 5.pixels
        } childOf this
    }

    private fun Duration.timeFormat() = toComponents { minutes, seconds, nanoseconds ->
        buildString {
            if (minutes > 0) {
                append(minutes)
                append(':')
            }
            append("%02d".format(seconds))
            if (nanoseconds != 0) {
                append('.')
                append("%03f".format(nanoseconds / 1e6))
            }
        }
    }

}