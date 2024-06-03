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
    @JvmStatic
    fun cleanSB(scoreboard: String): String {
        return scoreboard.stripControlCodes().toCharArray().filter { it.code in 32..126 }.joinToString(separator = "")
    }

    var sidebarLines: List<String> = emptyList()

    //#if MC>=11400
    //$$ private val SCOREBOARD_ENTRY_COMPARATOR: Comparator<ScoreboardEntry> = Comparator.comparing { obj: ScoreboardEntry -> obj.value() }
    //$$    .reversed()
    //$$   .thenComparing({ obj: ScoreboardEntry -> obj.owner() }, java.lang.String.CASE_INSENSITIVE_ORDER);
    //#endif

    fun fetchScoreboardLines(): List<String> {
        val scoreboard = mc.theWorld?.scoreboard ?: return emptyList()
        //#if MC<11400
        val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return emptyList()
        //#else
        //$$ val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return emptyList()
        //#endif
        val scores = scoreboard.getSortedScores(objective).filter { input ->
            //#if MC<11400
            input?.playerName != null && !input.playerName.startsWith("#")
            //#else
            //$$ input?.owner != null && !input.hidden()
            //#endif
        //#if MC<11400
        }.take(15)
        //#else
        //$$ }.sortedWith(SCOREBOARD_ENTRY_COMPARATOR).take(15)
        //#endif
        return scores.map { e ->
            //#if MC<11400
            ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(e.playerName), e.playerName)
            //#else
            //$$ Team.decorateName(scoreboard.getScoreHolderTeam(e.owner()), e.name()).string
            //#endif
        }.asReversed()
    }
}