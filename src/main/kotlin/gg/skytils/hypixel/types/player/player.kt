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

package gg.skytils.hypixel.types.player

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Player(
    @SerialName("achievementsOneTime")
    val achievements_one_time: List<JsonElement> = emptyList(),
    val achievements: Map<String, Double> = emptyMap(),
    @SerialName("socialMedia")
    val social_media: SocialMediaData = SocialMediaData(),
    internal val newPackageRank: String = "NONE",
    internal val monthlyPackageRank: String = "NONE",
    @SerialName("rank")
    internal val special_rank: String? = null,
    @SerialName("rankPlusColor")
    val plus_color: String = "RED",
    @SerialName("monthlyRankColor")
    val mvp_plus_plus_color: String = "AQUA",
    @SerialName("displayname")
    val display_name: String
) {
    val rank: String
        get() = special_rank ?: if (newPackageRank == "MVP_PLUS" && monthlyPackageRank == "SUPERSTAR") "MVP_PLUS_PLUS" else newPackageRank
}

@Serializable
data class SocialMediaData(
    val links: Map<String, String> = emptyMap(),
    val prompt: Boolean = false
)