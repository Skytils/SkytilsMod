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
package skytils.skytilsmod.features.impl.handlers

import com.google.gson.JsonObject
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.events.impl.MainReceivePacketEvent
import skytils.skytilsmod.utils.Utils
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class CustomNotifications : PersistentSave(File(Skytils.modDir, "customnotifications.json")) {

    @SubscribeEvent
    fun onMessage(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inSkyblock || (event.packet !is S02PacketChat) || event.packet.type != 0.toByte() || notifications.isEmpty()) return
        Skytils.threadPool.submit {
            val formatted = event.packet.chatComponent.formattedText
            for ((regex, text) in notifications) {
                val match = regex.find(formatted) ?: continue
                var title = text
                match.groupValues.forEachIndexed { i, s -> title = title.replace("%%${i}%%", s) }
                GuiManager.createTitle(title, 20)
            }
        }
    }

    override fun read(reader: InputStreamReader) {
        notifications.clear()
        val obj = gson.fromJson(reader, JsonObject::class.java)
        for ((key, value) in obj.entrySet()) {
            notifications[key.toRegex()] = value.asString
        }
    }

    override fun write(writer: OutputStreamWriter) {
        val obj = JsonObject()
        for ((key, value) in notifications) {
            obj.addProperty(key.pattern, value)
        }
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        gson.toJson(JsonObject(), writer)
    }

    companion object {
        val notifications = HashMap<Regex, String>()
    }
}