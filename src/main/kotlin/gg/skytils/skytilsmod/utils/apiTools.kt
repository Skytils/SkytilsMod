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

import gg.skytils.hypixel.types.player.Player
import gg.skytils.hypixel.types.skyblock.Profile
import gg.skytils.hypixel.types.util.Inventory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.util.BlockPos
import net.minecraftforge.common.util.Constants
import java.awt.Color
import java.util.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
sealed class HypixelResponse {
    abstract val success: Boolean
    val cause: String? = null
}

@Serializable
data class TypesProfileResponse(
    override val success: Boolean,
    val profiles: List<Profile>? = null
) : HypixelResponse()

@Serializable
data class PlayerResponse(override val success: Boolean, val player: Player?) : HypixelResponse()

enum class DungeonClass {
    ARCHER,
    BERSERK,
    MAGE,
    HEALER,
    TANK,
    EMPTY;

    val className = name.toTitleCase()
    val apiName = name.lowercase()

    override fun toString(): String {
        return this.className
    }

    companion object {
        fun getClassFromName(name: String): DungeonClass {
            return name.lowercase().let { entries.find { c -> c.apiName == it } }
                ?: throw IllegalArgumentException("No class could be found for the name $name")
        }
    }
}

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

@OptIn(ExperimentalEncodingApi::class)
fun Inventory.toMCItems() =
    data.let { data ->
        if (data.isEmpty()) {
            emptyList()
        } else {
            val list = CompressedStreamTools.readCompressed(Base64.decode(data).inputStream()).getTagList("i", Constants.NBT.TAG_COMPOUND)
            (0 until list.tagCount()).map { idx ->
                list.getCompoundTagAt(idx).takeUnless { it.hasNoTags() }?.let { ItemStack.loadItemStackFromNBT(it) }
            }
        }
    }

fun UUID.nonDashedString(): String {
    return this.toString().replace("-", "")
}