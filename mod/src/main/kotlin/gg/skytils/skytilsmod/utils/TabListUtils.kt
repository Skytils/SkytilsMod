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

import com.google.common.collect.ComparisonChain
import com.google.common.collect.Ordering
import gg.skytils.skytilsmod.Skytils.Companion.mc
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.world.WorldSettings

val NetworkPlayerInfo.text: String
    get() = displayName?.formattedText ?: ScorePlayerTeam.formatPlayerName(
        playerTeam,
        gameProfile.name
    )


object TabListUtils {
    private val playerInfoOrdering = object : Ordering<NetworkPlayerInfo>() {
        override fun compare(p1: NetworkPlayerInfo?, p2: NetworkPlayerInfo?): Int {
            return when {
                p1 != null && p2 != null -> {
                    ComparisonChain.start().compareTrueFirst(
                        p1.gameType != WorldSettings.GameType.SPECTATOR,
                        p2.gameType != WorldSettings.GameType.SPECTATOR
                    ).compare(
                        p1.playerTeam?.registeredName ?: "",
                        p2.playerTeam?.registeredName ?: ""
                    ).compare(p1.gameProfile.name, p2.gameProfile.name).result()
                }

                p1 == null -> -1
                else -> 0
            }
        }
    }
    var tabEntries: List<Pair<NetworkPlayerInfo, String>> = emptyList()
    fun fetchTabEntries(): List<NetworkPlayerInfo> = mc.thePlayer?.let {
        playerInfoOrdering.immutableSortedCopy(
            mc.thePlayer.sendQueue.playerInfoMap
        )
    } ?: emptyList()
}