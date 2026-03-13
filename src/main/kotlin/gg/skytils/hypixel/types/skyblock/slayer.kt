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

package gg.skytils.hypixel.types.skyblock

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class SlayerData(
    val slayer_quest: SlayerQuestData? = null,
    val slayer_bosses: Map<String, SlayerBossData> = emptyMap()
)

@Serializable
data class SlayerQuestData(
    val type: String,
    val tier: Double,
    val start_timestamp: Long = 0L,
    val combat_xp: Double = 0.0,
    val recent_mob_kills: List<SlayerKill> = emptyList()
)

@Serializable
data class SlayerKill(
    val xp: Double,
    val timestamp: Long
)

@Serializable(with = SlayerBossDataSerializer::class)
data class SlayerBossData(
    val claimed_levels: Map<String, Boolean> = emptyMap(),
    val boss_kills: Map<String, Long> = emptyMap(),
    val xp: Double = 0.0
)

object SlayerBossDataSerializer : KSerializer<SlayerBossData> {
    override val descriptor = buildClassSerialDescriptor(SlayerBossData::class.qualifiedName ?: "SlayerBossData") {
        element<Map<String, Boolean>>("claimed_levels")
        element<Map<String, Long>>("boss_kills")
        element<Double>("xp")
    }

    override fun deserialize(decoder: Decoder): SlayerBossData {
        val json = (decoder as? JsonDecoder ?: error("Only json supported")).decodeJsonElement().jsonObject

        return SlayerBossData(
            claimed_levels = json["claimed_levels"]?.jsonObject?.entries?.associate { (key, value) ->
                key to value.jsonPrimitive.boolean
            } ?: emptyMap(),
            boss_kills = json.entries.filter { (key, _) ->
                key.startsWith("boss_kills_tier_")
            }.associate { (key, value) ->
                key to value.jsonPrimitive.long
            },
            xp = json["xp"]?.jsonPrimitive?.double ?: 0.0
        )
    }

    override fun serialize(encoder: Encoder, value: SlayerBossData) =
        TODO("Not yet implemented")
}