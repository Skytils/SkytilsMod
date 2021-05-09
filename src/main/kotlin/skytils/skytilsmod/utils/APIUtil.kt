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
import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.protocol.HttpContext
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import skytils.skytilsmod.utils.Utils.readTextAndClose
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import java.util.*


object APIUtil {
    private val parser = JsonParser()

    val builder =
        HttpClients.custom().setUserAgent("Skytils/" + Skytils.VERSION)
            .addInterceptorFirst { request: HttpRequest, _: HttpContext? ->
                if (!request.containsHeader("Pragma")) request.addHeader("Pragma", "no-cache")
                if (!request.containsHeader("Cache-Control")) request.addHeader("Cache-Control", "no-cache")
            }

    fun getJSONResponse(urlString: String): JsonObject {
        val client = builder.build()
        try {
            val request = HttpGet(URL(urlString).toURI())
            request.protocolVersion = HttpVersion.HTTP_1_1
            val response: HttpResponse = client.execute(request)
            val entity = response.entity
            if (response.statusLine.statusCode == 200) {
                return parser.parse(entity.content.readTextAndClose()).asJsonObject
            } else {
                if (StringUtils.startsWithAny(
                        urlString,
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
                            return parser.parse(error).asJsonObject
                        }
                    }
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "An error has occured. See logs for more details."))
        } catch (ex: URISyntaxException) {
            ex.printStackTrace()
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "An error has occured. See logs for more details."))
        } finally {
            client.close()
        }
        return JsonObject()
    }

    fun getArrayResponse(urlString: String): JsonArray {
        val client = builder.build()
        try {
            val request = HttpGet(URL(urlString).toURI())
            request.protocolVersion = HttpVersion.HTTP_1_1
            val response: HttpResponse = client.execute(request)
            val entity = response.entity
            if (response.statusLine.statusCode == 200) {
                return parser.parse(entity.content.readTextAndClose()).asJsonArray
            } else {
                mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cRequest failed. HTTP Error Code: ${response.statusLine.statusCode}"))
            }
        } catch (ex: IOException) {
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cAn error has occured. See logs for more details."))
            ex.printStackTrace()
        } catch (ex: URISyntaxException) {
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cAn error has occured. See logs for more details."))
            ex.printStackTrace()
        } finally {
            client.close()
        }
        return JsonArray()
    }

    fun getUUID(username: String): String? {
        val uuidResponse = getJSONResponse("https://api.ashcon.app/mojang/v2/user/$username")
        if (uuidResponse.has("error")) {
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cFailed with error: ${uuidResponse["reason"].asString}"))
            return null
        }
        return uuidResponse["uuid"].asString.replace("-", "")
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
}