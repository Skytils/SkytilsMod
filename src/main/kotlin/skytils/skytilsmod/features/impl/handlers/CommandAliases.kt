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

import com.google.common.collect.Lists
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.SendChatMessageEvent
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class CommandAliases {
    @SubscribeEvent
    fun onSendChatMessage(event: SendChatMessageEvent) {
        if (event.message.startsWith("/")) {
            val args =
                Lists.newArrayList(*event.message.substring(1).trim { it <= ' ' }.split(" +".toRegex()).toTypedArray())
            val command = args.removeAt(0)
            if (aliases.containsKey(command)) {
                event.isCanceled = true
                try {
                    val msg =
                        if (Skytils.config.commandAliasMode == 0) "/" + aliases[command] + " " + java.lang.String.join(
                            " ",
                            args
                        ) else "/" + String.format(
                            aliases[command]!!, *args.toTypedArray()
                        )
                    if (event.addToChat) {
                        mc.ingameGUI.chatGUI.addToSentMessages(msg)
                    }
                    if (ClientCommandHandler.instance.executeCommand(mc.thePlayer, msg) != 0) return
                    Skytils.sendMessageQueue.add(msg)
                } catch (ignored: IllegalFormatException) {
                    if (event.addToChat) mc.ingameGUI.chatGUI.addToSentMessages(event.message)
                    mc.thePlayer.addChatMessage(ChatComponentText("§cYou did not specify the correct amount of arguments for this alias!"))
                }
            }
        }
    }

    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()
        private val mc = Minecraft.getMinecraft()
        private var aliasesFile = File(Skytils.modDir, "commandaliases.json")
        val aliases = HashMap<String, String>()
        fun reloadAliases() {
            aliases.clear()
            var aliasesObject: JsonObject
            try {
                FileReader(aliasesFile).use { `in` -> aliasesObject = gson.fromJson(`in`, JsonObject::class.java) }
            } catch (e: Exception) {
                aliasesObject = JsonObject()
                try {
                    FileWriter(aliasesFile).use { writer -> gson.toJson(aliasesObject, writer) }
                } catch (ignored: Exception) {
                }
            }
            for ((key, value) in aliasesObject.entrySet()) {
                aliases[key] = value.asString
            }
        }

        fun saveAliases() {
            try {
                FileWriter(aliasesFile).use { writer ->
                    val obj = JsonObject()
                    for ((key, value) in aliases) {
                        obj.addProperty(key, value)
                    }
                    gson.toJson(obj, writer)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    init {
        reloadAliases()
    }
}