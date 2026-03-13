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

//#if MC<11400
//$$ import com.google.common.collect.ComparisonChain
//$$ import com.google.common.collect.Ordering
//#endif
//#if MC<11400
//$$ import net.minecraft.world.level.LevelInfo
//#else
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

//#endif

val PlayerListEntry.text: String
    //#if MC<11400
    //$$ get() = displayName?.method_10865() ?: Team.method_1142(
    //$$     scoreboardTeam,
    //$$     profile.name
    //$$ )
    //#else
    get() {
       return if (gameMode != GameMode.SPECTATOR)
       (displayName ?: Team.decorateName(scoreboardTeam, Text.literal(profile.name))).formattedText
       else (displayName?.copy() ?: Team.decorateName(scoreboardTeam, Text.literal(profile.name))).formatted(Formatting.ITALIC).formattedText
    }
    //#endif

val PlayerListS2CPacket.Entry.text: String
    get() {
        return if (gameMode != GameMode.SPECTATOR)
            (displayName ?: Team.decorateName(team, Text.literal(profile?.name))).formattedText
        else (displayName?.copy() ?: Team.decorateName(team, Text.literal(profile?.name))).formatted(Formatting.ITALIC).formattedText
    }

val PlayerListS2CPacket.Entry.team
    get() = mc.world?.scoreboard?.getScoreHolderTeam(profile?.name)


object TabListUtils {
    //#if MC<11400
    //$$ private val playerInfoOrdering = object : Ordering<PlayerListEntry>() {
    //$$     override fun compare(p1: PlayerListEntry?, p2: PlayerListEntry?): Int {
    //$$         return when {
    //$$             p1 != null && p2 != null -> {
    //$$                 ComparisonChain.start().compareTrueFirst(
    //$$                     p1.method_2958() != LevelInfo.GameMode.SPECTATOR,
    //$$                     p2.method_2958() != LevelInfo.GameMode.SPECTATOR
    //$$                 ).compare(
    //$$                     p1.scoreboardTeam?.name ?: "",
    //$$                     p2.scoreboardTeam?.name ?: ""
    //$$                 ).compare(p1.profile.name, p2.profile.name).result()
    //$$             }
    //$$
    //$$             p1 == null -> -1
    //$$             else -> 0
    //$$         }
    //$$     }
    //$$ }
    //#else
    private val comparator: Comparator<PlayerListEntry> = Comparator.comparingInt<PlayerListEntry> {
       -it.listOrder
    }.thenComparingInt {
        if (it.gameMode == GameMode.SPECTATOR) 1 else 0
    }.thenComparing { o ->
       o.scoreboardTeam?.name ?: ""
    }.thenComparing { o ->
       o.profile.name.lowercase()
    }
    //#endif
    var tabEntries: List<Pair<PlayerListEntry, String>> = emptyList()
    fun fetchTabEntries(): List<PlayerListEntry> = mc.player?.let {
        //#if MC < 11400
        //$$ playerInfoOrdering.immutableSortedCopy(
        //$$     it.networkHandler.playerList
        //$$ )
        //#else
        it.networkHandler.listedPlayerListEntries.sortedWith(comparator).take(80)
        //#endif
    } ?: emptyList()
}