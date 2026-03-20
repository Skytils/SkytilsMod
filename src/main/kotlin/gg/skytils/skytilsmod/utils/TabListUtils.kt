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
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket
import net.minecraft.scoreboard.Team
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Nullables
import net.minecraft.world.GameMode
import java.util.function.Function
import java.util.function.ToIntFunction

val PlayerListEntry.text: String
    get() {
       return if (gameMode != GameMode.SPECTATOR)
       (displayName ?: Team.decorateName(scoreboardTeam, Text.literal(profile.name))).formattedText
       else (displayName?.copy() ?: Team.decorateName(scoreboardTeam, Text.literal(profile.name))).formatted(Formatting.ITALIC).formattedText
    }

val PlayerListS2CPacket.Entry.text: String
    get() {
        return if (gameMode != GameMode.SPECTATOR)
            (displayName ?: Team.decorateName(team, Text.literal(profile?.name))).formattedText
        else (displayName?.copy() ?: Team.decorateName(team, Text.literal(profile?.name))).formatted(Formatting.ITALIC).formattedText
    }

val PlayerListS2CPacket.Entry.team
    get() = mc.world?.scoreboard?.getScoreHolderTeam(profile?.name)


object TabListUtils {
    private val comparator: Comparator<PlayerListEntry> = Comparator.comparingInt<PlayerListEntry> {
       -it.listOrder
    }.thenComparingInt {
        if (it.gameMode == GameMode.SPECTATOR) 1 else 0
    }.thenComparing { o ->
       o.scoreboardTeam?.name ?: ""
    }.thenComparing { o ->
       o.profile.name.lowercase()
    }
    var tabEntries: List<Pair<PlayerListEntry, String>> = emptyList()
    fun fetchTabEntries(): List<PlayerListEntry> = mc.player?.let {
        it.networkHandler.listedPlayerListEntries.sortedWith(comparator).take(80)
    } ?: emptyList()
}