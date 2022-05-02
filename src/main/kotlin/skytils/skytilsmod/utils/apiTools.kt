/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

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
    @SerialName("slayer_bosses")
    val slayerBosses: Map<MobName, SlayerBoss> = emptyMap(),
)

@Serializable
data class SlayerBoss(
    val xp: Double
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