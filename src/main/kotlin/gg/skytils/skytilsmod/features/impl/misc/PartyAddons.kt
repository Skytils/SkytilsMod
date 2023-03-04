/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2023 Skytils
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

package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.universal.utils.MCClickEventAction
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.events.impl.SendChatMessageEvent
import gg.skytils.skytilsmod.utils.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Inspired by https://www.chattriggers.com/modules/v/HypixelUtilities
 */
object PartyAddons {
    private val partyStartPattern = Regex("^§6Party Members \\((\\d+)\\)§r$")
    private val leaderPattern = Regex("^§eParty Leader: (?<rank>.+? |§7)?(?<name>\\w+) §r(?<status>§a|§c)●§r")
    private val party = mutableListOf<PartyMember>()

    //0 = not awaiting, 1 = awaiting 2nd delimiter, 2 = awaiting 1st delimiter
    private var awaitingMessage = 0

    @SubscribeEvent
    fun onCommandRun(event: SendChatMessageEvent) {
        if (!Utils.inSkyblock || !Skytils.config.partyAddons) return
        if (Utils.equalsOneOf(event.message, "/pl", "/party list", "/p list", "/party l")) {
            awaitingMessage = 2
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte() || !Skytils.config.partyAddons) return
        val message = event.message.formattedText

        if (message == "§f§r" && awaitingMessage != 0) {
            event.isCanceled = true
        } else if (message.startsWith("§6Party Members")) {
            val partyStart = partyStartPattern.find(message)
            if (partyStart != null) {
                party.clear()
                event.isCanceled = true
            }
        } else if (message.startsWith("§eParty Leader")) {
            val leader = leaderPattern.find(message)

            if (leader != null) {
                val name = leader.groups["name"]?.value ?: return
                val status = leader.groups["status"]?.value ?: return
                val rank = leader.groups["rank"]?.value ?: return

                party.add(PartyMember(name, PartyMemberType.LEADER, status, rank))
                event.isCanceled = true
            }
        } else if (message.startsWith("§eParty Moderators: ")) {
            val moderators = Regex("(?<rank>.+? |§7)?(?<name>\\w+)§r(?<status>§a|§c) ● ")

            moderators.findAll(message.substringAfter("§eParty Moderators: ")).forEach {
                val name = it.groups["name"]?.value ?: return
                val status = it.groups["status"]?.value ?: return
                val rank = it.groups["rank"]?.value ?: return

                party.add(
                    PartyMember(
                        name,
                        PartyMemberType.MODERATOR,
                        status,
                        rank
                    )
                )
            }
            event.isCanceled = true
        } else if (message.startsWith("§eParty Members: ")) {
            val members = Regex("(?<rank>.+? |§7)?(?<name>\\w+)§r(?<status>§a|§c) ● ")

            members.findAll(message.substringAfter("§eParty Members: ")).forEach {
                val name = it.groups["name"]?.value ?: return
                val status = it.groups["status"]?.value ?: return
                val rank = it.groups["rank"]?.value ?: return

                party.add(
                    PartyMember(
                        name,
                        PartyMemberType.MEMBER,
                        status,
                        rank
                    )
                )
            }
            event.isCanceled = true
        } else if (message.startsWith("§cYou are not currently in a party.") && awaitingMessage != 0) {
            party.clear()
        } else if (message == "§9§m-----------------------------------------------------§r" && awaitingMessage != 0) {
            awaitingMessage--
            if (awaitingMessage == 1 || party.isEmpty()) return

            val component = UMessage("§aParty members (${party.size})\n")

            val player = party.first { it.name == Skytils.mc.thePlayer.name }

            if (player.type == PartyMemberType.LEADER) {
                component.append(
                    createButton(
                        "§9[Party Warp] ",
                        "/p warp",
                        "§9Click to warp the party."
                    )
                ).append(
                    createButton(
                        "§e[Toggle All Invite]\n",
                        "/p settings allinvite",
                        "§eClick to toggle all invite."
                    )
                ).append(
                    createButton(
                        "§6[Toggle Mute] ",
                        "/p mute",
                        "§6Click to toggle mute."
                    )
                ).append(
                    createButton(
                        "§c[Kick Offline] ",
                        "/p kickoffline",
                        "§cClick to kick offline members."
                    )
                ).append(
                    createButton(
                        "§4[Disband]\n",
                        "/p disband",
                        "§4Click to disband the party."
                    )
                )
            }

            val partyLeader = party.first { it.type == PartyMemberType.LEADER }
            component.append(
                "\n${partyLeader.status}➡§r ${partyLeader.rank}${partyLeader.name}"
            )

            if (party.any { it.type == PartyMemberType.MODERATOR }) component.append("\n§eMods")
            party.filter { it.type == PartyMemberType.MODERATOR }.forEach {
                component.append(
                    "\n${it.status}➡§r ${it.rank}${it.name} "
                )
                if (player.type != PartyMemberType.LEADER) return@forEach
                component.append(
                    createButton(
                        "§a[⋀] ",
                        "/p promote ${it.name}",
                        "§aPromote ${it.name}"
                    )
                ).append(
                    createButton(
                        "§c[⋁] ",
                        "/p demote ${it.name}",
                        "§cDemote ${it.name}"
                    )
                ).append(
                    createButton(
                        "§4[☠]",
                        "/p kick ${it.name}",
                        "§4Kick ${it.name}"
                    )
                )
            }

            if (party.any { it.type == PartyMemberType.MEMBER }) component.append("\n§eMembers")
            party.filter { it.type == PartyMemberType.MEMBER }.forEach {
                component.append(
                    "\n${it.status}➡§r ${it.rank}${it.name} "
                )
                if (player.type != PartyMemberType.LEADER) return@forEach
                component.append(
                    createButton(
                        "§9[⋀] ",
                        "/p transfer ${it.name}",
                        "§9Transfer ${it.name}"
                    )
                ).append(
                    createButton(
                        "§a[⋀] ",
                        "/p promote ${it.name}",
                        "§aPromote ${it.name}"
                    )
                ).append(
                    createButton(
                        "§4[☠]",
                        "/p kick ${it.name}",
                        "§4Kick ${it.name}"
                    )
                )
            }
            component.chat()
        }
    }

    private fun createButton(text: String, command: String, hoverText: String): UTextComponent {
        return UTextComponent(text).setClick(MCClickEventAction.RUN_COMMAND, command).setHoverText(hoverText)
    }

    private data class PartyMember(
        val name: String,
        val type: PartyMemberType,
        val status: String,
        val rank: String
    )

    private enum class PartyMemberType {
        LEADER,
        MODERATOR,
        MEMBER
    }
}