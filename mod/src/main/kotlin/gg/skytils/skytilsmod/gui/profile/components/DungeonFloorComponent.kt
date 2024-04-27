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

package gg.skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.CopyConstraintFloat
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.vigilance.gui.VigilancePalette
import gg.skytils.hypixel.types.skyblock.DungeonModeData
import gg.skytils.skytilsmod.gui.constraints.FixedChildBasedRangeConstraint
import gg.skytils.skytilsmod.utils.toStringIfTrue
import java.awt.Color
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DungeonFloorComponent(val dungeonBase: DungeonModeData, val floorNum: Int, val isMaster: Boolean) : UIRoundedRectangle(5f) {

    val floor: String = floorNum.toString()

    init {
        constrain {
            width = ChildBasedMaxSizeConstraint() + 10.pixels
            height = FixedChildBasedRangeConstraint() + 10.pixels
            color = VigilancePalette.getDarkBackground().constraint
        }

        val title = UIText("${"Master ".toStringIfTrue(isMaster)}Floor ${if (floorNum == 0) "Entrance" else floor}").constrain {
            x = CenterConstraint()
            y = 5.pixels
            textScale = 1.5f.pixels
            color = VigilancePalette.getBrightText().constraint
        } childOf this

        val pairs = ArrayList<Pair<String, Any>>(7)
        dungeonBase.times_played[floor]?.apply { pairs.add("Times Played" to this) }
        dungeonBase.watcher_kills[floor]?.apply { pairs.add("Watcher Kills" to this) }
        dungeonBase.tier_completions[floor]?.apply { pairs.add("Completions" to this) }
        dungeonBase.best_score[floor]?.apply { pairs.add("Best Score" to this) }
        dungeonBase.fastest_time[floor]
            ?.apply { pairs.add("Fastest Time" to milliseconds.timeFormat()) }
        dungeonBase.fastest_time_s[floor]
            ?.apply { pairs.add("Fastest Time S" to milliseconds.timeFormat()) }
        dungeonBase.fastest_time_s_plus[floor]
            ?.apply { pairs.add("Fastest Time S+" to milliseconds.timeFormat()) }

        val text by UIWrappedText(pairs.joinToString("\n") {
            "§7${it.first}: §f${it.second}"
        }.ifEmpty { "Maybe get better??" }).constrain {
            x = 5.pixels
            y = SiblingConstraint(5f)
            width = (pairs.maxOfOrNull {
                fontProvider.getStringWidth("§7${it.first}: §f${it.second}", 10f)
            } ?: fontProvider.getStringWidth("Maybe get better??", 10f)).pixels
        } childOf this

        children.add(children.size - 1, UIBlock(Color(0x4166f5).constraint).constrain {
            x = 5.pixels()
            y = SiblingConstraint(5f)
            width = CopyConstraintFloat() boundTo text
            height = 2.pixels
        }.also { it.parent = this })
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
                append("%03d".format((nanoseconds / 1e6).toInt()))
            }
        }
    }

}