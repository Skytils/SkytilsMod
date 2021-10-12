/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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
package skytils.skytilsmod.utils

import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam
import skytils.skytilsmod.Skytils.Companion.mc

/**
 * Taken from https://gist.github.com/aaron1998ish/33c4e1836bd5cf79501d163a1b5c8304
 */
object ScoreboardUtil {
    @JvmStatic
    fun cleanSB(scoreboard: String): String {
        return scoreboard.stripControlCodes().toCharArray().filter { it.code in 21..126 }.joinToString(separator = "")
    }

    @JvmStatic
    val sidebarLines: List<String>
        get() {
            val scoreboard = mc.theWorld?.scoreboard ?: return emptyList()
            val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return emptyList()
            var scores = scoreboard.getSortedScores(objective)
            val list = scores.filter { input: Score? ->
                input != null && input.playerName != null && !input.playerName
                    .startsWith("#")
            }
            scores = if (list.size > 15) {
                list.drop(15)
            } else {
                list
            }
            return scores.map {
                ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(it.playerName), it.playerName)
            }
        }
}