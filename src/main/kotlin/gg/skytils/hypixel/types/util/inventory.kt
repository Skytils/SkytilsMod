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

package gg.skytils.hypixel.types.util

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import java.io.DataInput
import java.io.Reader
import java.util.zip.GZIPInputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable(with = InventorySerializer::class)
class Inventory(
    val data: String
)

object InventorySerializer : KSerializer<Inventory> {
    override val descriptor = buildClassSerialDescriptor("Inventory")

    override fun deserialize(decoder: Decoder) =
        (decoder as? JsonDecoder)?.decodeJsonElement()?.jsonObject?.get("data")?.jsonPrimitive?.content?.let { data ->
            Inventory(data)
        } ?: error("Error occurred while deserializing")

    override fun serialize(encoder: Encoder, value: Inventory) =
        error("Only deserialization supported")
}