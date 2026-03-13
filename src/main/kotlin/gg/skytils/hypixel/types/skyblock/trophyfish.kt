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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

// TODO: claimed rewards
@Serializable(with = TrophyFishDataSerializer::class)
data class TrophyFishData(
    val total_caught: Int = 0,
    val fish_count: Map<String, TrophyFish> = emptyMap()
)

object TrophyFishDataSerializer : KSerializer<TrophyFishData> {
    override val descriptor = buildClassSerialDescriptor("TrophyFishData") {
        element<Int>("total_caught")
        element<Map<String, TrophyFish>>("fish_count")
    }
    val ignored = setOf("rewards", "total_caught", "last_caught")

    private val trophyFishRegex = Regex("(\\w*?(?:fish|er|vanille|horse|ray|fin|[123]))(?:_(?:bronze|silver|gold|diamond))?")

    override fun deserialize(decoder: Decoder): TrophyFishData =
        (decoder as? JsonDecoder)?.decodeJsonElement()?.jsonObject?.let { json ->
            val fish = json.entries
                .filterNot { it.key in ignored }
                .groupBy { entry ->
                    trophyFishRegex.matchEntire(entry.key)?.groupValues?.get(1) ?: ""
                }
                .map { (fish, values) ->
                    val total = values.find { it.key == fish }?.value?.jsonPrimitive?.intOrNull ?: 0
                    val bronze = values.find { it.key == "${fish}_bronze"}?.value?.jsonPrimitive?.intOrNull ?: 0
                    val silver = values.find { it.key == "${fish}_silver" }?.value?.jsonPrimitive?.intOrNull ?: 0
                    val gold = values.find { it.key == "${fish}_gold" }?.value?.jsonPrimitive?.intOrNull ?: 0
                    val diamond = values.find { it.key == "${fish}_diamond" }?.value?.jsonPrimitive?.intOrNull ?: 0
                    fish to TrophyFish(total, bronze, silver, gold, diamond)
                }.toMap()
            TrophyFishData(
                json["total_caught"]?.jsonPrimitive?.int ?: 0,
                fish
            )
        } ?: error("Only json supported")

    override fun serialize(encoder: Encoder, value: TrophyFishData) =
        error("Operation not supported")

}

@Serializable
data class TrophyFish(
    val total: Int,
    val bronze: Int,
    val silver: Int,
    val gold: Int,
    val diamond: Int
)