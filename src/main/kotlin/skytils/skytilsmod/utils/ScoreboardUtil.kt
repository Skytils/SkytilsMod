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

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import net.minecraft.client.Minecraft
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam
import java.util.stream.Collectors

/**
 * Taken from Danker's Skyblock Mod under GPL 3.0 license
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
object ScoreboardUtil {
    @JvmStatic
    fun cleanSB(scoreboard: String?): String {
        val nvString = StringUtils.stripControlCodes(scoreboard).toCharArray()
        val cleaned = StringBuilder()
        for (c in nvString) {
            if (c.code in 21..126) {
                cleaned.append(c)
            }
        }
        return cleaned.toString()
    }

    @JvmStatic
    val sidebarLines: List<String>
        get() {
            val lines: MutableList<String> = ArrayList()
            if (Minecraft.getMinecraft().theWorld == null) return lines
            val scoreboard = Minecraft.getMinecraft().theWorld.scoreboard ?: return lines
            val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return lines
            var scores = scoreboard.getSortedScores(objective)
            val list = scores.filter { input: Score? ->
                input != null && input.playerName != null && !input.playerName
                    .startsWith("#")
            }
            scores = if (list.size > 15) {
                Lists.newArrayList(Iterables.skip(list, scores.size - 15))
            } else {
                list
            }
            for (score in scores) {
                val team = scoreboard.getPlayersTeam(score.playerName)
                lines.add(ScorePlayerTeam.formatPlayerName(team, score.playerName))
            }
            return lines
        }
}