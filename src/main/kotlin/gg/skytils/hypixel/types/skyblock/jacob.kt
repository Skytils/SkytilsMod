/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.hypixel.types.skyblock

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class JacobData(
    /**
     * The amount of medals the player has in their "inventory"
     */
    val medals_inv: Map<String, Double> = emptyMap(),
    val perks: Map<String, JsonElement> = emptyMap(),
    val contests: Map<String, JacobContest> = emptyMap(),
    /**
     * The unique medals a player has won in a specific collection
     */
    @SerialName("unique_brackets")
    val medals: Map<String, List<String>> = emptyMap()
)

@Serializable
data class JacobContest(
    val collected: Double = 0.0,
    val claimed_rewards: Boolean = false,
    val claimed_position: Int? = null,
    val claimed_participants: Int? = null,
    val claimed_medal: String? = null
)