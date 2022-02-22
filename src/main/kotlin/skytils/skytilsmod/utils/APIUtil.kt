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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import gg.essential.universal.UChat
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.message.BasicHeader
import org.apache.hc.core5.util.Timeout
import skytils.skytilsmod.Skytils
import java.awt.image.BufferedImage
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO


object APIUtil {
    private val parser = JsonParser()

    val builder: HttpClientBuilder =
        HttpClients.custom().setUserAgent("Skytils/${Skytils.VERSION}")
            .setConnectionManagerShared(true)
            .setDefaultHeaders(
                mutableListOf(
                    BasicHeader("Pragma", "no-cache"),
                    BasicHeader("Cache-Control", "no-cache")
                )
            )
            .setDefaultRequestConfig(
                RequestConfig.custom()
                    .setConnectTimeout(Timeout.ofSeconds(30))
                    .setResponseTimeout(Timeout.ofSeconds(30))
                    .build()
            )
            .useSystemProperties()

    /**
     * Taken from Elementa under MIT License
     * @link https://github.com/Sk1erLLC/Elementa/blob/master/LICENSE
     */
    fun URL.getImage(): BufferedImage {
        val connection = this.openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.useCaches = true
        connection.addRequestProperty("User-Agent", "Skytils/${Skytils.VERSION}")
        connection.doOutput = true

        return ImageIO.read(connection.inputStream)
    }

    fun getJSONResponse(urlString: String): JsonObject {
        val client = builder.build()
        try {
            client.execute(HttpGet(urlString)).use { response ->
                response.entity.use { entity ->
                    val obj = parser.parse(EntityUtils.toString(entity)).asJsonObject
                    EntityUtils.consume(entity)
                    return obj
                }
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            UChat.chat("§cSkytils ran into an ${ex::class.simpleName ?: "error"} whilst fetching a resource. See logs for more details.")
        } finally {
            client.close()
        }
        return JsonObject()
    }

    fun getArrayResponse(urlString: String): JsonArray {
        val client = builder.build()
        try {
            client.execute(HttpGet(urlString)).use { response ->
                response.entity.use { entity ->
                    val obj = parser.parse(EntityUtils.toString(entity)).asJsonArray
                    EntityUtils.consume(entity)
                    return obj
                }
            }
        } catch (ex: Throwable) {
            UChat.chat("§cSkytils ran into an ${ex::class.simpleName ?: "error"} whilst fetching a resource. See logs for more details.")
            ex.printStackTrace()
        } finally {
            client.close()
        }
        return JsonArray()
    }

    fun getResponse(urlString: String): String {
        val client = builder.build()
        try {
            client.execute(HttpGet(urlString)).use { response ->
                response.entity.use { entity ->
                    val obj = EntityUtils.toString(entity)
                    EntityUtils.consume(entity)
                    return obj
                }
            }
        } catch (ex: Throwable) {
            UChat.chat("§cSkytils ran into an ${ex::class.simpleName ?: "error"} whilst fetching a resource. See logs for more details.")
            ex.printStackTrace()
        } finally {
            client.close()
        }
        return ""
    }
}