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
package gg.skytils.skytilsmod.features.impl.handlers

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.utils.RegexAsString
import gg.skytils.skytilsmod.utils.Utils
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.io.Reader
import java.io.Writer

object CustomNotifications : PersistentSave(File(Skytils.modDir, "customnotifications.json")) {
    val notifications = hashSetOf<Notification>()

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

    override fun read(reader: Reader) {
        notifications.clear()
        val obj = json.decodeFromString<JsonElement>(reader.readText())
        if (obj is JsonArray) {
            obj.mapTo(notifications) {
                json.decodeFromJsonElement(it)
            }
        } else if (obj is JsonObject) {
            for ((key, value) in obj) {
                notifications.add(Notification(key.toRegex(), value.jsonPrimitive.content, 20))
            }
        }
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(notifications))
    }

    override fun setDefault(writer: Writer) {
        writer.write("[]")
    }

    @Serializable
    data class Notification(
        @Serializable(with = RegexAsString::class)
        val regex: Regex,
        val text: String,
        @SerialName("ticks") val displayTicks: Int
    )
}