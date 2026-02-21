package gg.skytils.skytilsmod.util

import kotlinx.serialization.Serializable

@Serializable
data class PetInfo(val type: String, val active: Boolean, val exp: Double, val tier: String, val candyUsed: Int)