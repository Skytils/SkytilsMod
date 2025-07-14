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

import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod._event.PacketReceiveEvent
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.utils.RegexAsString
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.formattedText
import kotlinx.coroutines.launch
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import java.io.File
import java.io.Reader
import java.io.Writer

object CustomNotifications : PersistentSave(File(Skytils.modDir, "customnotifications.json")), EventSubscriber {
    val notifications = hashSetOf<Notification>()

    fun onMessage(event: PacketReceiveEvent<*>) {
        if (!Utils.inSkyblock || event.packet !is GameMessageS2CPacket || event.packet.overlay || notifications.isEmpty()) return
        Skytils.launch {
            val formatted = event.packet.content?.formattedText ?: return@launch
            for ((regex, text, displayTicks, enabled) in notifications) {
                if (!enabled) continue
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
        @SerialName("ticks") val displayTicks: Int,
        @EncodeDefault val enabled: Boolean = true
    )

    override fun setup() {
        register(::onMessage, EventPriority.Highest)
    }
}