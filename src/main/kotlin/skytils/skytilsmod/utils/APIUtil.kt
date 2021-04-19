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

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.protocol.HttpContext
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URISyntaxException
import java.net.URL
import java.util.*

/**
 * Modified from Danker's Skyblock Mod under GPL 3.0 license
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
object APIUtil {
    var client: CloseableHttpClient =
        HttpClients.custom().setUserAgent("Skytils/" + Skytils.VERSION)
            .addInterceptorFirst { request: HttpRequest, context: HttpContext? ->
                if (!request.containsHeader("Pragma")) request.addHeader("Pragma", "no-cache")
                if (!request.containsHeader("Cache-Control")) request.addHeader("Cache-Control", "no-cache")
            }.build()

    fun getJSONResponse(urlString: String): JsonObject {
        try {
            val request = HttpGet(URL(urlString).toURI())
            request.protocolVersion = HttpVersion.HTTP_1_1
            val response: HttpResponse = client.execute(request)
            val entity = response.entity
            if (response.statusLine.statusCode == 200) {
                val `in` = BufferedReader(InputStreamReader(entity.content))
                var input: String?
                val r = StringBuilder()
                while (`in`.readLine().also { input = it } != null) {
                    r.append(input)
                }
                `in`.close()
                val gson = Gson()
                return gson.fromJson(r.toString(), JsonObject::class.java)
            } else {
                if (urlString.startsWith("https://api.hypixel.net/") || urlString.startsWith(MayorInfo.baseURL) || urlString == AuctionData.dataURL) {
                    val errorStream = entity.content
                    Scanner(errorStream).use { scanner ->
                        scanner.useDelimiter("\\Z")
                        val error = scanner.next()
                        if (error.startsWith("{")) {
                            val gson = Gson()
                            return gson.fromJson(error, JsonObject::class.java)
                        }
                    }
                } else if (urlString.startsWith("https://api.mojang.com/users/profiles/minecraft/") && response.statusLine.statusCode == 204) {
                    mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Failed with reason: Player does not exist."))
                } else {
                    mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Request failed. HTTP Error Code: " + response.statusLine.statusCode))
                }
            }
        } catch (ex: IOException) {
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "An error has occured. See logs for more details."))
            ex.printStackTrace()
        } catch (ex: URISyntaxException) {
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "An error has occured. See logs for more details."))
            ex.printStackTrace()
        }
        return JsonObject()
    }

    // Only used for UUID => Username
    fun getArrayResponse(urlString: String?): JsonArray {
        try {
            val request = HttpGet(URL(urlString).toURI())
            request.protocolVersion = HttpVersion.HTTP_1_1
            val response: HttpResponse = client.execute(request)
            val entity = response.entity
            if (response.statusLine.statusCode == 200) {
                val `in` = BufferedReader(InputStreamReader(entity.content))
                var input: String?
                val r = StringBuilder()
                while (`in`.readLine().also { input = it } != null) {
                    r.append(input)
                }
                `in`.close()
                val gson = Gson()
                return gson.fromJson(r.toString(), JsonArray::class.java)
            } else {
                mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Request failed. HTTP Error Code: " + response.statusLine.statusCode))
            }
        } catch (ex: IOException) {
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "An error has occured. See logs for more details."))
            ex.printStackTrace()
        } catch (ex: URISyntaxException) {
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "An error has occured. See logs for more details."))
            ex.printStackTrace()
        }
        return JsonArray()
    }

    fun getUUID(username: String): String {
        val uuidResponse = getJSONResponse("https://api.mojang.com/users/profiles/minecraft/$username")
        return uuidResponse["id"].asString
    }

    fun getLatestProfileID(UUID: String, key: String): String? {
        val player: EntityPlayer = Minecraft.getMinecraft().thePlayer

        // Get profiles
        println("Fetching profiles...")
        val profilesResponse = getJSONResponse("https://api.hypixel.net/skyblock/profiles?uuid=$UUID&key=$key")
        if (!profilesResponse["success"].asBoolean) {
            val reason = profilesResponse["cause"].asString
            player.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Failed with reason: " + reason))
            return null
        }
        if (profilesResponse["profiles"].isJsonNull) {
            player.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "This player doesn't appear to have played SkyBlock."))
            return null
        }

        // Loop through profiles to find latest
        println("Looping through profiles...")
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
}