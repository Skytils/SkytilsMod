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
package gg.skytils.skytilsmod.listeners

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.commands.impl.RepartyCommand
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiNewChat
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.stripControlCodes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object ChatListener {
    private var lastPartyDisbander = ""
    private val invitePattern = Pattern.compile("(?:(?:\\[.+?] )?(?:\\w+) invited )(?:\\[.+?] )?(\\w+)")
    private val playerPattern = Pattern.compile("(?:\\[.+?] )?(\\w+)")
    private val party_start_pattern = Pattern.compile("^Party Members \\((\\d+)\\)$")
    private val leader_pattern = Pattern.compile("^Party Leader: (?:\\[.+?] )?(\\w+) ●$")
    private val members_pattern = Pattern.compile(" (?:\\[.+?] )?(\\w+) ●")
    private var awaitingDelimiter = false

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.isOnHypixel || event.type == 2.toByte()) return
        val formatted = event.message.formattedText
        val unformatted = formatted.stripControlCodes()
        if (Skytils.config.autoReparty) {
            if (formatted.endsWith("§r§ehas disbanded the party!§r")) {
                val matcher = playerPattern.matcher(unformatted)
                if (matcher.find()) {
                    lastPartyDisbander = matcher.group(1)
                    println("Party disbanded by $lastPartyDisbander")
                    Skytils.launch {
                        if (Skytils.config.autoRepartyTimeout == 0) return@launch
                        try {
                            println("waiting for timeout")
                            delay(Skytils.config.autoRepartyTimeout * 1000L)
                            lastPartyDisbander = ""
                            println("cleared last party disbander")
                        } catch (_: Exception) {
                        }
                    }
                    return
                }
            }
            if (unformatted.contains("You have 60 seconds to accept") && lastPartyDisbander.isNotEmpty() && event.message.siblings.size >= 7) {
                val acceptMessage = event.message.siblings[6].chatStyle
                if (acceptMessage.chatHoverEvent.value.unformattedText.contains(lastPartyDisbander)) {
                    Skytils.sendMessageQueue.add("/p accept $lastPartyDisbander")
                    lastPartyDisbander = ""
                    return
                }
            }
        }

        // Await delimiter
        if (awaitingDelimiter && unformatted.startsWith("---")) {
            event.isCanceled = true
            awaitingDelimiter = false
            return
        }

        // Reparty command
        // Getting party
        if (RepartyCommand.gettingParty) {
            if (unformatted.contains("-----")) {
                when (RepartyCommand.Delimiter) {
                    0 -> {
                        println("Get Party Delimiter Cancelled")
                        RepartyCommand.Delimiter++
                        event.isCanceled = true
                        return
                    }

                    1 -> {
                        println("Done querying party")
                        RepartyCommand.gettingParty = false
                        RepartyCommand.Delimiter = 0
                        event.isCanceled = true
                        return
                    }
                }
            } else if (unformatted.startsWith("Party M") || unformatted.startsWith("Party Leader")) {
                val player = mc.thePlayer
                val partyStart = party_start_pattern.matcher(unformatted)
                val leader = leader_pattern.matcher(unformatted)
                val members = members_pattern.matcher(unformatted)
                if (partyStart.matches() && partyStart.group(1).toInt() == 1) {
                    UChat.chat("$failPrefix §cYou cannot reparty yourself.")
                    RepartyCommand.partyThread!!.interrupt()
                } else if (leader.matches() && leader.group(1) != player.name) {
                    UChat.chat("$failPrefix §cYou are not party leader.")
                    RepartyCommand.partyThread!!.interrupt()
                } else {
                    while (members.find()) {
                        val partyMember = members.group(1)
                        if (partyMember != player.name) {
                            RepartyCommand.party.add(partyMember)
                            println(partyMember)
                        }
                    }
                }
                event.isCanceled = true
                return
            }
        }
        // Disbanding party
        if (RepartyCommand.disbanding) {
            if (unformatted.contains("-----")) {
                when (RepartyCommand.Delimiter) {
                    0 -> {
                        println("Disband Delimiter Cancelled")
                        RepartyCommand.Delimiter++
                        event.isCanceled = true
                        return
                    }

                    1 -> {
                        println("Done disbanding")
                        RepartyCommand.disbanding = false
                        RepartyCommand.Delimiter = 0
                        event.isCanceled = true
                        return
                    }
                }
            } else if (unformatted.endsWith("has disbanded the party!")) {
                event.isCanceled = true
                return
            }
        }
        // Inviting
        if (RepartyCommand.inviting) {
            if (unformatted.endsWith(" to the party! They have 60 seconds to accept.")) {
                val invitee = invitePattern.matcher(unformatted)
                if (invitee.find()) {
                    println("" + invitee.group(1) + ": " + RepartyCommand.repartyFailList.remove(invitee.group(1)))
                }
                tryRemoveLineAtIndex(1)
                awaitingDelimiter = true
                event.isCanceled = true
                println("Player Invited!")
                RepartyCommand.inviting = false
                return
            } else if (unformatted.contains("Couldn't find a player") || unformatted.contains("You cannot invite that player")) {
                tryRemoveLineAtIndex(1)
                event.isCanceled = true
                println("Player Invited!")
                RepartyCommand.inviting = false
                return
            }
        }
        // Fail Inviting
        if (RepartyCommand.failInviting) {
            if (unformatted.endsWith(" to the party! They have 60 seconds to accept.")) {
                val invitee = invitePattern.matcher(unformatted)
                if (invitee.find()) {
                    println("" + invitee.group(1) + ": " + RepartyCommand.repartyFailList.remove(invitee.group(1)))
                }
                tryRemoveLineAtIndex(1)
                event.isCanceled = true
                RepartyCommand.inviting = false
                return
            } else if (unformatted.contains("Couldn't find a player") || unformatted.contains("You cannot invite that player")) {
                tryRemoveLineAtIndex(1)
                event.isCanceled = true
                println("Player Invited!")
                RepartyCommand.inviting = false
                return
            }
        }
        if (Skytils.config.firstLaunch && unformatted == "Welcome to Hypixel SkyBlock!") {
            UChat.chat("$prefix §bThank you for downloading Skytils!")
            ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/skytils help")
            Skytils.config.firstLaunch = false
            Skytils.config.markDirty()
            Skytils.config.writeData()
        }
    }

    private fun tryRemoveLineAtIndex(index: Int) {
        val lines =
            (mc.ingameGUI.chatGUI as AccessorGuiNewChat).drawnChatLines
        if (lines.size > index) {
            lines.removeAt(index)
        }
    }
}
