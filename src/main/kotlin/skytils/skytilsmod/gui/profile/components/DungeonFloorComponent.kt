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
import skytils.hylin.skyblock.dungeons.DungeonBase
import skytils.skytilsmod.gui.constraints.FixedChildBasedRangeConstraint
import java.awt.Color
import kotlin.time.Duration

class DungeonFloorComponent(val dungeonBase: DungeonBase, val floor: Int) : UIRoundedRectangle(5f) {
    init {
        constrain {
            width = ChildBasedMaxSizeConstraint() + 10.pixels
            height = FixedChildBasedRangeConstraint() + 10.pixels
            color = VigilancePalette.getDarkBackground().constraint
        }

        val title = UIText("Floor $floor").constrain {
            x = CenterConstraint()
            y = 5.pixels
            textScale = 1.5f.pixels
            color = VigilancePalette.getBrightText().constraint
        } childOf this

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

        val text by UIWrappedText(pairs.joinToString("\n") {
            "§7${it.first}: §f${it.second}"
        }.ifEmpty { "Maybe get better??" }).constrain {
            x = 5.pixels
            y = SiblingConstraint(5f)
            width = pairs.maxOf {
                fontProvider.getStringWidth("§7${it.first}: §f${it.second}", 10f)
            }.pixels
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