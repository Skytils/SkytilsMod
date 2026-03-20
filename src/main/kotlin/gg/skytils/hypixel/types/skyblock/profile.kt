package gg.skytils.hypixel.types.skyblock

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Profile(
    @Contextual
    @SerialName("profile_id")
    val id: UUID,
    @SerialName("cute_name")
    val name: String,
    val selected: Boolean = false,
    val community_upgrades: UpgradeData = UpgradeData(null),
    val members: Map<String, Member>
)

@Serializable
data class UpgradeData(
    val currently_upgrading: UpgradeInProgress? = null,
    val upgrade_states: List<ProfileUpgrade> = emptyList()
)

@Serializable
data class UpgradeInProgress(
    @SerialName("upgrade")
    val name: String,
    val new_tier: Double,
    @SerialName("start_ms")
    val started: Long,
    @SerialName("who_started")
    val started_by: String
)

@Serializable
data class ProfileUpgrade(
    @SerialName("upgrade")
    val name: String,
    val tier: Double,
    @SerialName("started_ms")
    val started: Long,
    val started_by: String,
    val claimed_by: String
)

@Serializable
data class BankData(
    val balance: Double,
    val transactions: List<BankTransaction> = emptyList()
)

@Serializable
data class BankTransaction(
    val amount: Double,
    val timestamp: Long,
    val action: String,
    @SerialName("initiator_name")
    val initiator: String // for some reason this is the color formatted name, no clue why
)