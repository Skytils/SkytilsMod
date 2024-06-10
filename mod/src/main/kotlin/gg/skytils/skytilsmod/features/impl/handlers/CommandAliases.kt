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

import gg.essential.universal.UChat
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.ChatMessageSentEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.utils.runClientCommand
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.Reader
import java.io.Writer
import java.util.*

object CommandAliases : PersistentSave(File(Skytils.modDir, "commandaliases.json")), EventSubscriber {
    val aliases = hashMapOf<String, String>()

    fun onSendChatMessage(event: ChatMessageSentEvent) {
        if (event.message.startsWith("/")) {
            val args = event.message.substring(1).trim().split(" ").toMutableList()
            val command = args.removeAt(0)
            val replacement = aliases[command] ?: return
            event.cancelled = true
            try {
                val msg =
                    if (Skytils.config.commandAliasMode == 0) "/" + replacement + " " + args.joinToString(" ") else "/${
                        replacement.format(
                            *args.toTypedArray()
                        )
                    }"
                if (event.addToHistory) {
                    mc.ingameGUI.chatGUI.addToSentMessages(msg)
                }
                if (runClientCommand(msg) != 0) return
                Skytils.sendMessageQueue.add(msg)
            } catch (ignored: IllegalFormatException) {
                if (event.addToHistory) mc.ingameGUI.chatGUI.addToSentMessages(event.message)
                UChat.chat("$failPrefix §cYou did not specify the correct amount of arguments for this alias!")
            }
        }
    }

    override fun read(reader: Reader) {
        aliases.clear()
        aliases.putAll(json.decodeFromString<Map<String, String>>(reader.readText()))
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(aliases))
    }

    override fun setDefault(writer: Writer) {
        writer.write("{}")
    }

    override fun setup() {
        register(::onSendChatMessage)
    }
}