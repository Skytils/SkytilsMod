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
import gg.essential.universal.UChat
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.events.SendChatMessageEvent
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class CommandAliases : PersistentSave(File(Skytils.modDir, "commandaliases.json")) {

    @SubscribeEvent
    fun onSendChatMessage(event: SendChatMessageEvent) {
        if (event.message.startsWith("/")) {
            val args = event.message.substring(1).trim().split(" ").toMutableList()
            val command = args.removeAt(0)
            if (aliases.containsKey(command)) {
                event.isCanceled = true
                try {
                    val msg =
                        if (Skytils.config.commandAliasMode == 0) "/" + aliases[command] + " " + args.joinToString(" ") else "/${
                            aliases[command]!!.format(
                                *args.toTypedArray()
                            )
                        }"
                    if (event.addToChat) {
                        mc.ingameGUI.chatGUI.addToSentMessages(msg)
                    }
                    if (ClientCommandHandler.instance.executeCommand(mc.thePlayer, msg) != 0) return
                    Skytils.sendMessageQueue.add(msg)
                } catch (ignored: IllegalFormatException) {
                    if (event.addToChat) mc.ingameGUI.chatGUI.addToSentMessages(event.message)
                    UChat.chat("§cYou did not specify the correct amount of arguments for this alias!")
                }
            }
        }
    }

    override fun read(reader: FileReader) {
        aliases.clear()
        val obj = gson.fromJson(reader, JsonObject::class.java)
        for ((key, value) in obj.entrySet()) {
            aliases[key] = value.asString
        }
    }

    override fun write(writer: FileWriter) {
        val obj = JsonObject()
        for ((key, value) in aliases) {
            obj.addProperty(key, value)
        }
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: FileWriter) {
        gson.toJson(JsonObject(), writer)
    }

    companion object {
        val aliases = HashMap<String, String>()
    }
}