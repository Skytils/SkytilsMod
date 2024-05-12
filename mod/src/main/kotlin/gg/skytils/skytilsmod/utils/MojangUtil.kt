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

import gg.essential.lib.caffeine.cache.Cache
import gg.essential.lib.caffeine.cache.Caffeine
import gg.skytils.skytilsmod.Skytils.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.fixedRateTimer


object MojangUtil {
    private val uuidToUsername: Cache<UUID, String>
    private val usernameToUuid: Cache<String, UUID>
    private val requestCount = AtomicInteger()

    init {
        Caffeine.newBuilder()
            .weakKeys()
            .weakValues()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000L)
            .apply {
                uuidToUsername = build()
                usernameToUuid = build()
            }
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        val uuid = event.entity.uniqueID
        if (event.entity is EntityOtherPlayerMP && uuid.version() == 4) {
            val name = event.entity.name.lowercase()
            uuidToUsername[uuid] = name
            usernameToUuid[name] = uuid
        }
    }

    suspend fun getUUIDFromUsername(name: String): UUID? {
        val username = name.lowercase()
        return usernameToUuid.getIfPresent(username) ?: run {
            makeMojangRequest("https://api.minecraftservices.com/minecraft/profile/lookup/name/$username").let {
                when (it.status) {
                    HttpStatusCode.OK -> {
                        val (id, _) = it.body<ProfileResponse>()
                        usernameToUuid[username] = id
                        uuidToUsername[id] = username
                        id
                    }

                    HttpStatusCode.NoContent, HttpStatusCode.NotFound -> null
                    else -> throw it.body<MojangException>()
                }
            }
        }
    }

    suspend fun getUsernameFromUUID(uuid: UUID): String? {
        return uuidToUsername.getIfPresent(uuid) ?: run {
            makeMojangRequest("https://api.minecraftservices.com/minecraft/profile/lookup/${uuid}").let {
                when (it.status) {
                    HttpStatusCode.OK -> {
                        val (_, name) = it.body<ProfileResponse>()
                        val username = name.lowercase()
                        usernameToUuid[username] = uuid
                        uuidToUsername[uuid] = username
                        username
                    }

                    HttpStatusCode.NoContent, HttpStatusCode.NotFound -> null
                    else -> throw it.body<MojangException>()
                }
            }
        }
    }

    init {
        fixedRateTimer("Mojang-Fake-Requests-Insert", startAt = Date((System.currentTimeMillis() / 60000) * 60000 + 48000), period = 60_000L) {
            requestCount.set(0)
        }
    }

    /**
     * @see <a href="https://bugs.mojang.com/browse/WEB-6830">WEB-6830</a>
     */
    private suspend fun makeMojangRequest(url: String): HttpResponse {
        if (requestCount.incrementAndGet() % 6 == 0) {
            client.get("https://api.minecraftservices.com/minecraft/profile/lookup/name/SlashSlayer?ts=${System.currentTimeMillis()}")
            requestCount.getAndIncrement()
        }
        return client.get(url)
    }

    @Serializable
    private data class ProfileResponse(
        @Serializable(with = UUIDAsString::class) val id: UUID,
        val name: String
    )

    @Serializable
    class MojangException(
        val error: String,
        val errorMessage: String
    ) : Exception(errorMessage)
}