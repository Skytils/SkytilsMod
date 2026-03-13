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
package gg.skytils.skytilsmod.utils

import gg.skytils.skytilsmod.Skytils.mc
import net.minecraft.scoreboard.*

/**
 * Source: Mojang [net.minecraft.client.gui.GuiIngame.renderScoreboard]]
 */
object ScoreboardUtil {
    private val controlCodeLike = Regex("(?i)§.")

    @JvmStatic
    fun cleanSB(scoreboard: String): String {
        return scoreboard.replace(controlCodeLike, "").toCharArray().filter { it.code in 32..126 }.joinToString(separator = "")
    }

    var sidebarLines: List<String> = emptyList()

    private val SCOREBOARD_ENTRY_COMPARATOR: Comparator<ScoreboardEntry> = Comparator.comparing { obj: ScoreboardEntry -> obj.value() }
       .reversed()
      .thenComparing({ obj: ScoreboardEntry -> obj.owner() }, java.lang.String.CASE_INSENSITIVE_ORDER);

    fun fetchScoreboardLines(): List<String> {
        val scoreboard = mc.world?.scoreboard ?: return emptyList()
        val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return emptyList()
        val scores = scoreboard.getScoreboardEntries(objective).filter { input ->
            input?.owner != null && !input.hidden()
        }.sortedWith(SCOREBOARD_ENTRY_COMPARATOR).take(15)
        return scores.map { e ->
            Team.decorateName(scoreboard.getScoreHolderTeam(e.owner()), e.name()).formattedText
        }.asReversed()
    }
}