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
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.client
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayDeque
import kotlin.concurrent.fixedRateTimer


object MojangUtil {
    private val uuidToUsername: Cache<UUID, String>
    private val usernameToUuid: Cache<String, UUID>
    private val requestCount = AtomicInteger()

    private val usernameQueue = MutableSharedFlow<Pair<String, Long>>()
    private val uuidFlow = MutableSharedFlow<Pair<String, UUID?>>()

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

    suspend fun queueUsernameForUUID(name: String): UUID? {
        val username = name.lowercase()
        usernameQueue.emit(username to System.currentTimeMillis())

        return uuidFlow.first { it.first == username }.second
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

    suspend fun getUUIDsFromUsernames(names: Collection<String>): Map<String, UUID?> {
        if (requestCount.incrementAndGet() % 6 == 0) {
            client.get("https://api.minecraftservices.com/minecraft/profile/lookup/name/SlashSlayer?ts=${System.currentTimeMillis()}")
            requestCount.getAndIncrement()
        }
        val usernames = names.mapTo(hashSetOf()) { it.lowercase() }
        val map = HashMap<String, UUID?>()
        map.putAll(usernameToUuid.getAllPresent(usernames))

        usernames.removeAll(map.keys)

        usernames.windowed(10).forEach {
            client.post("https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname") {
                contentType(ContentType.Application.Json)
                setBody(it)
            }.let {
                when (it.status) {
                    HttpStatusCode.OK -> {
                        val response = it.body<List<ProfileResponse>>()
                        for (profile in response) {
                            val username = profile.name.lowercase()
                            val uuid = profile.id
                            map[username] = uuid
                            usernameToUuid[username] = uuid
                            uuidToUsername[uuid] = username
                        }
                    }

                    else -> throw it.body<MojangException>()
                }
            }
        }

        names.forEach {
            map.getOrPut(it) { null }
        }

        return map
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

        Skytils.IO.launch {
            val usernames = ArrayDeque<Pair<String, Long>>(10)
            usernameQueue.collect { name ->
                usernames.add(name)
                if (usernames.size >= 10 || usernames.isNotEmpty() && System.currentTimeMillis() - usernames.first().second >= 3000L) {
                    getUUIDsFromUsernames(usernames.map { it.first }).forEach {
                        uuidFlow.emit(it.toPair())
                    }
                    usernames.clear()
                }
            }
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