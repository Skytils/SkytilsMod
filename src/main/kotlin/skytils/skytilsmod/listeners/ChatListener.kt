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
package skytils.skytilsmod.listeners

import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.StringUtils.stripControlCodes
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.commands.RepartyCommand
import skytils.skytilsmod.mixins.accessors.AccessorGuiNewChat
import skytils.skytilsmod.utils.Utils
import java.util.regex.Pattern

class ChatListener {
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.isOnHypixel || event.type == 2.toByte()) return
        val unformatted = stripControlCodes(event.message.unformattedText)
        if (unformatted.startsWith("Your new API key is ")) {
            val apiKey = event.message.siblings[0].chatStyle.chatClickEvent.value
            Skytils.config.apiKey = apiKey
            Skytils.config.markDirty()
            mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.GREEN.toString() + "Skytils updated your set Hypixel API key to " + EnumChatFormatting.DARK_GREEN + apiKey))
        }
        if (Skytils.config.autoReparty) {
            if (unformatted.contains("has disbanded the party!")) {
                val matcher = playerPattern.matcher(unformatted)
                if (matcher.find()) {
                    lastPartyDisbander = matcher.group(1)
                    println("Party disbanded by $lastPartyDisbander")
                    rejoinThread = Thread {
                        if (Skytils.config.autoRepartyTimeout == 0) return@Thread
                        try {
                            println("waiting for timeout")
                            Thread.sleep((Skytils.config.autoRepartyTimeout * 1000).toLong())
                            lastPartyDisbander = ""
                            println("cleared last party disbander")
                        } catch (e: Exception) {
                        }
                    }
                    rejoinThread!!.start()
                    return
                }
            }
            if (unformatted.contains("You have 60 seconds to accept") && lastPartyDisbander.isNotEmpty() && event.message.siblings.size > 0) {
                val acceptMessage = event.message.siblings[6].chatStyle
                if (acceptMessage.chatHoverEvent.value.unformattedText.contains(lastPartyDisbander)) {
                    Skytils.sendMessageQueue.add("/p accept $lastPartyDisbander")
                    rejoinThread!!.interrupt()
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
                val player = Minecraft.getMinecraft().thePlayer
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
        if (Skytils.config.firstLaunch && unformatted == "Welcome to Hypixel SkyBlock!") {
            mc.thePlayer.addChatMessage(ChatComponentText("§bThank you for downloading Skytils!"))
            ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/skytils help")
            Skytils.config.firstLaunch = false
            Skytils.config.markDirty()
            Skytils.config.writeData()
        }
    }

    fun tryRemoveLineAtIndex(index: Int) {
        val lines = (mc.ingameGUI.chatGUI as AccessorGuiNewChat).drawnChatLines
        if (lines.size > index) {
            lines.removeAt(index)
        }
    }

    companion object {
        var mc = Minecraft.getMinecraft()!!
        private var rejoinThread: Thread? = null
        private var lastPartyDisbander = ""
        private val invitePattern = Pattern.compile("(?:(?:\\[.+?] )?(?:\\w+) invited )(?:\\[.+?] )?(\\w+)")
        private val playerPattern = Pattern.compile("(?:\\[.+?] )?(\\w+)")
        private val party_start_pattern = Pattern.compile("^Party Members \\((\\d+)\\)$")
        private val leader_pattern = Pattern.compile("^Party Leader: (?:\\[.+?] )?(\\w+) ●$")
        private val members_pattern = Pattern.compile(" (?:\\[.+?] )?(\\w+) ●")
        private var awaitingDelimiter = false
    }
}