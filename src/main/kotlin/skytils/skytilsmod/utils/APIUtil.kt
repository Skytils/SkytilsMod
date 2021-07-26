/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.ssl.SSLContexts
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import java.awt.image.BufferedImage
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import java.util.*
import javax.imageio.ImageIO


object APIUtil {
    private val parser = JsonParser()

    val sslContext = SSLContexts.custom()
        .loadTrustMaterial { chain, authType ->
            isValidCert(chain, authType)
        }
        .build()
    val sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
        .setSslContext(sslContext)
        .build()

    val cm = PoolingHttpClientConnectionManagerBuilder.create()
        .setSSLSocketFactory(sslSocketFactory)

    val builder: HttpClientBuilder =
        HttpClients.custom().setUserAgent("Skytils/${Skytils.VERSION}")
            .addRequestInterceptorFirst { request, entity, context ->
                if (!request.containsHeader("Pragma")) request.addHeader("Pragma", "no-cache")
                if (!request.containsHeader("Cache-Control")) request.addHeader("Cache-Control", "no-cache")
            }

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
        val client = builder.setConnectionManager(cm.build()).build()
        try {
            val request = HttpGet(URL(urlString).toURI())

            val response = client.execute(request)
            val entity = response.entity
            if (response.code == 200) {
                val obj = parser.parse(EntityUtils.toString(entity)).asJsonObject
                EntityUtils.consume(entity)
                return obj
            } else {
                if (urlString.startsWithAny(
                        "https://api.ashcon.app/mojang/v2/user/",
                        "https://api.hypixel.net/",
                        MayorInfo.baseURL,
                        AuctionData.dataURL
                    )
                ) {
                    val errorStream = entity.content
                    Scanner(errorStream).use { scanner ->
                        scanner.useDelimiter("\\Z")
                        val error = scanner.next()
                        if (error.startsWith("{")) {
                            EntityUtils.consume(entity)
                            return parser.parse(error).asJsonObject
                        }
                    }
                }
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils ran into an error whilst fetching a resource. See logs for more details."))
        } finally {
            client.close()
        }
        return JsonObject()
    }

    fun getArrayResponse(urlString: String): JsonArray {
        val client = builder.setConnectionManager(cm.build()).build()
        try {
            val request = HttpGet(URL(urlString).toURI())

            val response = client.execute(request)
            val entity = response.entity
            if (response.code == 200) {
                val arr = parser.parse(EntityUtils.toString(entity)).asJsonArray
                EntityUtils.consume(entity)
                return arr
            } else {
                mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils failed to request a resource. HTTP Error Code: ${response.code}"))
            }
        } catch (ex: Throwable) {
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils ran into an error whilst fetching a resource. See logs for more details."))
            ex.printStackTrace()
        } finally {
            client.close()
        }
        return JsonArray()
    }

    /**
     * Modified from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    fun getLatestProfileID(UUID: String, key: String): String? {
        val player: EntityPlayer = Minecraft.getMinecraft().thePlayer

        val profilesResponse = getJSONResponse("https://api.hypixel.net/skyblock/profiles?uuid=$UUID&key=$key")
        if (!profilesResponse["success"].asBoolean) {
            val reason = profilesResponse["cause"].asString
            player.addChatMessage(ChatComponentText("§cFailed with reason: $reason"))
            return null
        }
        if (profilesResponse["profiles"].isJsonNull) {
            player.addChatMessage(ChatComponentText("§cThis player doesn't appear to have played SkyBlock."))
            return null
        }

        var latestProfile = ""
        var latestSave: Long = 0
        val profilesArray = profilesResponse["profiles"].asJsonArray
        for (profile in profilesArray) {
            val profileJSON = profile.asJsonObject
            var profileLastSave: Long = 1
            if (profileJSON["members"].asJsonObject[UUID].asJsonObject.has("last_save")) {
                profileLastSave = profileJSON["members"].asJsonObject[UUID].asJsonObject["last_save"].asLong
            }
            if (profileLastSave > latestSave) {
                latestProfile = profileJSON["profile_id"].asString
                latestSave = profileLastSave
            }
        }
        return latestProfile
    }

    private fun isValidCert(chain: Array<X509Certificate>, authType: String): Boolean {
        return chain.any { it.issuerDN.name == "CN=R3, O=Let's Encrypt, C=US" }
    }
}