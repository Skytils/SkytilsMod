/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.listeners

import gg.essential.universal.UChat
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.Sharttils.Companion.mc
import sharttils.sharttilsmod.commands.impl.RepartyCommand
import sharttils.sharttilsmod.mixins.transformers.accessors.AccessorGuiNewChat
import sharttils.sharttilsmod.utils.Utils
import sharttils.sharttilsmod.utils.stripControlCodes
import java.util.regex.Pattern

class ChatListener {
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.isOnHypixel || event.type == 2.toByte()) return
        val formatted = event.message.formattedText
        val unformatted = formatted.stripControlCodes()
        if (unformatted.startsWith("Your new API key is ") && event.message.siblings.size >= 1) {
            val apiKey = event.message.siblings[0].chatStyle.chatClickEvent.value
            Sharttils.config.apiKey = apiKey
            Sharttils.hylinAPI.key = Sharttils.config.apiKey
            Sharttils.config.markDirty()
            UChat.chat("§aSharttils updated your set Hypixel API key to §2${apiKey}")
            return
        }
        if (Sharttils.config.autoReparty) {
            if (formatted.endsWith("§r§ehas disbanded the party!§r")) {
                val matcher = playerPattern.matcher(unformatted)
                if (matcher.find()) {
                    lastPartyDisbander = matcher.group(1)
                    println("Party disbanded by $lastPartyDisbander")
                    Sharttils.threadPool.submit {
                        if (Sharttils.config.autoRepartyTimeout == 0) return@submit
                        try {
                            println("waiting for timeout")
                            Thread.sleep(Sharttils.config.autoRepartyTimeout * 1000L)
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
                    Sharttils.sendMessageQueue.add("/p accept $lastPartyDisbander")
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
                    player.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "You cannot reparty yourself."))
                    RepartyCommand.partyThread!!.interrupt()
                } else if (leader.matches() && leader.group(1) != player.name) {
                    player.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "You are not party leader."))
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
        if (Sharttils.config.firstLaunch && unformatted == "Welcome to Hypixel SkyBlock!") {
            UChat.chat("§bThank you for downloading Sharttils!")
            ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/sharttils help")
            Sharttils.config.firstLaunch = false
            Sharttils.config.markDirty()
            Sharttils.config.writeData()
        }
    }

    private fun tryRemoveLineAtIndex(index: Int) {
        val lines = (mc.ingameGUI.chatGUI as AccessorGuiNewChat).drawnChatLines
        if (lines.size > index) {
            lines.removeAt(index)
        }
    }

    companion object {
        private var lastPartyDisbander = ""
        private val invitePattern = Pattern.compile("(?:(?:\\[.+?] )?(?:\\w+) invited )(?:\\[.+?] )?(\\w+)")
        private val playerPattern = Pattern.compile("(?:\\[.+?] )?(\\w+)")
        private val party_start_pattern = Pattern.compile("^Party Members \\((\\d+)\\)$")
        private val leader_pattern = Pattern.compile("^Party Leader: (?:\\[.+?] )?(\\w+) ●$")
        private val members_pattern = Pattern.compile(" (?:\\[.+?] )?(\\w+) ●")
        private var awaitingDelimiter = false
    }
}
