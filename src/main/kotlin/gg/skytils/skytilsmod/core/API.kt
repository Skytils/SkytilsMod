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

package gg.skytils.skytilsmod.core

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.utils.PlayerResponse
import gg.skytils.skytilsmod.utils.TypesProfileResponse
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.util.UUID

object API {
    private val client = Skytils.client
    private const val baseUrl = "hypixel.skytils.gg"

    suspend fun getSkyblockProfiles(uuid: UUID) =
        client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = baseUrl
                path("v2", "skyblock", "profiles")
                parameter("uuid", uuid.toString())
            }
        }.body<TypesProfileResponse>().profiles

    suspend fun getSelectedSkyblockProfile(uuid: UUID) =
        getSkyblockProfiles(uuid)?.find { it.selected }

    fun getSelectedSkyblockProfileSync(uuid: UUID) =
        runBlocking {
            getSelectedSkyblockProfile(uuid)
        }

    suspend fun getPlayer(uuid: UUID) =
        client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = baseUrl
                path("v2", "player")
                parameter("uuid", uuid.toString())
            }
        }.body<PlayerResponse>().player

    fun getPlayerSync(uuid: UUID) =
        runBlocking {
            getPlayer(uuid)
        }
}