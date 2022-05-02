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
package skytils.skytilsmod.features.impl.handlers

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.utils.Utils
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class CustomNotifications : PersistentSave(File(Skytils.modDir, "customnotifications.json")) {

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onMessage(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type != 0.toByte() || notifications.isEmpty()) return
        Skytils.launch {
            val formatted = event.message.formattedText
            for ((regex, text, displayTicks) in notifications) {
                val match = regex.find(formatted) ?: continue
                var title = text
                match.groupValues.forEachIndexed { i, s -> title = title.replace("%%${i}%%", s) }
                GuiManager.createTitle(title, displayTicks)
            }
        }
    }

    override fun read(reader: InputStreamReader) {
        notifications.clear()
        val obj = gson.fromJson(reader, JsonElement::class.java)
        if (obj is JsonObject) {
            for ((key, value) in obj.entrySet()) {
                notifications.add(Notification(key.toRegex(), value.asString, 20))
            }
        } else if (obj is JsonArray) {
            obj.mapTo(notifications) {
                it as JsonObject
                Notification(it["regex"].asString.toRegex(), it["text"].asString, it["ticks"].asInt)
            }
        }
    }

    override fun write(writer: OutputStreamWriter) {
        val obj = JsonArray()
        for (notif in notifications) {
            obj.add(JsonObject().apply {
                addProperty("regex", notif.regex.pattern)
                addProperty("text", notif.text)
                addProperty("ticks", notif.displayTicks)
            })
        }
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        gson.toJson(JsonArray(), writer)
    }

    companion object {
        val notifications = hashSetOf<Notification>()
    }

    data class Notification(val regex: Regex, val text: String, val displayTicks: Int)
}