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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import net.minecraft.util.BlockPos
import java.awt.Color
import java.util.*

private typealias NonDashedUUID = String
private typealias MobName = String

@Serializable
sealed class HypixelResponse {
    abstract val success: Boolean
    val cause: String? = null
}

@Serializable
data class ProfileResponse(override val success: Boolean, val profile: SkyblockProfile) : HypixelResponse()

@Serializable
data class SkyblockProfile(val members: Map<NonDashedUUID, ProfileMember>)

@Serializable
data class ProfileMember(
    val slayer: SlayerData
)

@Serializable
class SlayerData(
    @SerialName("slayer_bosses")
    val slayerBosses: Map<MobName, SlayerBoss> = emptyMap()
)

@Serializable
data class SlayerBoss(
    val xp: Long = 0L
)

@Serializable
data class PetInfo(
    val type: String,
    val exp: Double,
    val tier: String,
    val active: Boolean = false,
    val hideInfo: Boolean = false,
    val heldItem: String? = null,
    val candyUsed: Int = 0,
    val skin: String? = null,
    val uuid: String? = null
)

@Serializable
data class GithubRelease(
    @SerialName("tag_name")
    val tagName: String,
    val assets: List<GithubAsset>,
    val body: String
)

@Serializable
data class GithubAsset(
    val name: String,
    @SerialName("browser_download_url")
    val downloadUrl: String,
    val size: Long,
    @SerialName("download_count")
    val downloadCount: Long,
    val uploader: GithubUser
)

@Serializable
data class GithubUser(
    @SerialName("login")
    val username: String
)

object RegexAsString : KSerializer<Regex> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Regex", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Regex = Regex(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: Regex) = encoder.encodeString(value.pattern)
}

object BlockPosCSV : KSerializer<BlockPos> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BlockPos", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): BlockPos = decoder.decodeString().split(',').let {
        BlockPos(it[0].toInt(), it[1].toInt(), it[2].toInt())
    }

    override fun serialize(encoder: Encoder, value: BlockPos) = encoder.encodeString("${value.x},${value.y},${value.z}")
}

object IntColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)
    override fun deserialize(decoder: Decoder): Color = Color(decoder.decodeInt(), true)
    override fun serialize(encoder: Encoder, value: Color) = encoder.encodeInt(value.rgb)
}

object BlockPosArraySerializer : KSerializer<BlockPos> {
    private val delegateSerializer = IntArraySerializer()
    override val descriptor: SerialDescriptor = delegateSerializer.descriptor
    override fun deserialize(decoder: Decoder): BlockPos = decoder.decodeSerializableValue(delegateSerializer).let {
        BlockPos(it[0], it[1], it[2])
    }

    override fun serialize(encoder: Encoder, value: BlockPos) =
        encoder.encodeSerializableValue(delegateSerializer, intArrayOf(value.x, value.y, value.z))
}

object BlockPosObjectSerializer : KSerializer<BlockPos> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BlockPos") {
        element<Int>("x")
        element<Int>("y")
        element<Int>("z")
    }

    override fun serialize(encoder: Encoder, value: BlockPos) =
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.x)
            encodeIntElement(descriptor, 1, value.y)
            encodeIntElement(descriptor, 2, value.z)
        }

    override fun deserialize(decoder: Decoder): BlockPos =
        decoder.decodeStructure(descriptor) {
            var x = -1
            var y = -1
            var z = -1
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> x = decodeIntElement(descriptor, 0)
                    1 -> y = decodeIntElement(descriptor, 1)
                    2 -> z = decodeIntElement(descriptor, 2)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            BlockPos(x, y, z)
        }
}


object UUIDAsString : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString().toDashedUUID())
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}