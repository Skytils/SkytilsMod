/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2023 Skytils
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

import com.google.gson.JsonObject
import gg.skytils.skytilsmod.Skytils
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.runBlocking
import skytils.hylin.request.ConnectionHandler
import skytils.hylin.request.HypixelAPIException

object HylinConnectionHandler : ConnectionHandler() {
    val client = HttpClient(CIO) {
        install(ContentEncoding) {
            deflate(1.0F)
            gzip(0.9F)
        }
        install(ContentNegotiation) {
            gson()
        }
        install(HttpCache)
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 1)
            exponentialDelay()
        }
        install(UserAgent) {
            agent = "Skytils/${Skytils.VERSION}"
        }

        engine {
            endpoint {
                connectTimeout = 10000
                keepAliveTime = 5000
                requestTimeout = 10000
                socketTimeout = 10000
            }
        }
    }

    override fun readJSON(endpoint: String): JsonObject = runBlocking {
        try {
            client.get(endpoint).body()
        } catch (e: NoTransformationFoundException) {
            throw IllegalStateException("Error caught during JSON parsing from $endpoint", e)
        }
    }

    override fun hypixelJSON(endpoint: String): JsonObject = runBlocking {
        readJSON(endpoint).also {
            if (!it.has("success") || !it["success"].asBoolean) {
                throw HypixelAPIException(endpoint, it["cause"].asString)
            }
        }
    }
}