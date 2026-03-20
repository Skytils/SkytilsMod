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

import kotlinx.serialization.Serializable

@Serializable
data class PetsData(
    val autopet: AutopetData = AutopetData(),
    val pets: List<Pet> = emptyList()
)

@Serializable
data class AutopetData(
    val migrated: Boolean = false
)

@Serializable
data class Pet(
    // Seems to be null when the pet is:
    // a reward (rock, skeleton horse)
    // a fishing drop (megalodon, flying fish)
    // a mithril golem
    // a tarantula
    // a spirit pet
    // a rat
    // from a game system (bingo, grandma wolf)
    // not convertible into an item (kuudra)
    val uuid: String? = null,
    // Seems to not exist when on an ironman type profile (ironman, bingo)
    val uniqueId: String? = null,
    val type: String,
    val exp: Double = 0.0,
    val active: Boolean = false,
    val tier: String,
    val heldItem: String? = null,
    val candyUsed: Int = 0,
    val skin: String? = null,
    val hideInfo: Boolean = false,
)