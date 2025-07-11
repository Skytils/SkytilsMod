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

import com.mojang.brigadier.arguments.StringArgumentType
import gg.essential.universal.UChat
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.ChatMessageSentEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.utils.runClientCommand
import kotlinx.serialization.encodeToString
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import java.io.File
import java.io.Reader
import java.io.Writer
import java.util.Comparator
import java.util.HashMap
import java.util.IllegalFormatException
import java.util.SortedMap

object CommandAliases : PersistentSave(File(Skytils.modDir, "commandaliases.json")), EventSubscriber {
    val aliases get() = _aliases

    private var chatMessageRegister: (() -> Boolean)? = null

    private var _aliases: MutableMap<String, String> = hashMapOf()

    private val comparator = Comparator.comparingInt(String::length)
        .reversed()
        .thenComparing(Comparator.naturalOrder())

    fun recreateMap(commandAliasSpaces: Boolean) {
        _aliases =
            if (commandAliasSpaces) sortedMapOf<String, String>(
                comparator
            ).apply {
                putAll(_aliases)
            }
            else HashMap(_aliases)
    }

    fun sanityCheck() {
        if (Skytils.config.commandAliasesSpaces) {
            if (_aliases !is SortedMap) error("Command Aliases map is supposed to be sorted, but isn't!")
        } else {
            if (_aliases is SortedMap) error("Command Aliases map is supposed to be unsorted, but isn't!")
        }
    }

    init {
        recreateMap(Skytils.config.commandAliasesSpaces)
    }

    fun onSendChatMessage(event: ChatMessageSentEvent) {
        if (!event.message.startsWith("/")) return

        val candidate = event.message.substring(1).trim()

        val entry = aliases.entries
            .find { (key, _) ->
                ' ' in key && (candidate == key || candidate.startsWith("$key "))
            } ?: return

        val (key, template) = entry
        val args = candidate.removePrefix(key).trim()

        event.cancelled = true
        try {
            val msg =
                if (Skytils.config.commandAliasMode == 0) "/$template $args" else "/${
                    template.format(
                        *args.split(" ").toTypedArray()
                    )
                }"
            if (event.addToHistory) {
                mc.inGameHud.chatHud.addToMessageHistory(msg)
            }
            if (runClientCommand(msg) != 0) return
            Skytils.sendMessageQueue.add(msg)
        } catch (_: IllegalFormatException) {
            if (event.addToHistory) mc.inGameHud.chatHud.addToMessageHistory(event.message)
            UChat.chat("$failPrefix §cYou did not specify the correct amount of arguments for this alias!")
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

    override fun setup() { setup(allowSpaces = Skytils.config.commandAliasesSpaces) }

    fun setup(allowSpaces: Boolean = Skytils.config.commandAliasesSpaces, customRemovalKeys: Set<String>? = null) {
        recreateMap(allowSpaces)

        if (allowSpaces) {
            if (chatMessageRegister == null) {
                chatMessageRegister = register(::onSendChatMessage)
            }
        } else if (chatMessageRegister != null) {
            chatMessageRegister!!.invoke()
            chatMessageRegister = null
        }

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.root.children
                .removeIf { node -> aliases.containsKey(node.name) || (customRemovalKeys?.contains(node.name) == true) } // Hopefully removes things with spaces due to how literal parses aliases that have spaces

            aliases.forEach { (alias, template) ->
                val hasSpace = ' ' in alias

                if (!allowSpaces && hasSpace) return@forEach

                val literalNode = literal(alias)
                    .then(
                        argument("args", StringArgumentType.greedyString())
                            .apply {
                                if (!hasSpace) {
                                    this.executes { ctx ->
                                        val raw = StringArgumentType.getString(ctx, "args")
                                        runAlias(alias, template, raw)
                                        1
                                    }
                                }
                            }
                    )

                if (!hasSpace) {
                    literalNode.executes { ctx ->
                        runAlias(alias, template, "")
                        1
                    }
                }

                dispatcher.register(literalNode)
            }
        }
    }

    private fun runAlias(
        alias: String,
        template: String,
        rawArgs: String
    ) {
        val args = if (rawArgs.isBlank()) emptyList() else rawArgs.split(" ")
        val msg = try {
            when (Skytils.config.commandAliasMode) {
                0 -> "/" + listOf(template, *args.toTypedArray()).joinToString(" ")
                else -> "/" + template.format(*args.toTypedArray())
            }
        } catch (_: IllegalFormatException) {
            mc.inGameHud.chatHud.addToMessageHistory("/$alias $rawArgs")
            UChat.chat("$failPrefix §cWrong number of arguments for alias '$alias'!")
            return
        }

        mc.inGameHud.chatHud.addToMessageHistory(msg)
        if (runClientCommand(msg) != 0) return
        Skytils.sendMessageQueue.add(msg)
    }
}