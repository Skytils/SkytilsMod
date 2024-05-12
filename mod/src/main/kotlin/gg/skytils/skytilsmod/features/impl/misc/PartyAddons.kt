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

package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.universal.utils.MCClickEventAction
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.events.impl.SendChatMessageEvent
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.append
import gg.skytils.skytilsmod.utils.printDevMessage
import gg.skytils.skytilsmod.utils.setHoverText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Inspired by https://www.chattriggers.com/modules/v/HypixelUtilities
 */
object PartyAddons {
    private val partyStartPattern = Regex("^§6Party Members \\((\\d+)\\)§r$")
    private val playerPattern = Regex("(?<rank>§r§.(?:\\[.+?] )?)(?<name>\\w+) ?§r(?<status>§a|§c) ?● ?")
    private val party = mutableListOf<PartyMember>()
    private val partyCommands = setOf("/pl", "/party list", "/p list", "/party l")

    //0 = not awaiting, 1 = awaiting 2nd delimiter, 2 = awaiting 1st delimiter
    private var awaitingDelimiter = 0

    @SubscribeEvent
    fun onCommandRun(event: SendChatMessageEvent) {
        if (!Utils.isOnHypixel || !Skytils.config.partyAddons) return
        if (event.message in partyCommands) {
            awaitingDelimiter = 2
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.isOnHypixel || event.type == 2.toByte() || !Skytils.config.partyAddons) return
        val message = event.message.formattedText

        if (message == "§f§r" && awaitingDelimiter != 0) {
            event.isCanceled = true
        } else if (partyStartPattern.matches(message)) {
            party.clear()
            event.isCanceled = true
        } else if (message.startsWith("§eParty ")) {
            val playerType = when {
                message.startsWith("§eParty Leader: ") -> PartyMemberType.LEADER
                message.startsWith("§eParty Moderators: ") -> PartyMemberType.MODERATOR
                message.startsWith("§eParty Members: ") -> PartyMemberType.MEMBER
                else -> return
            }
            playerPattern.findAll(message.substringAfter(": ")).forEach {
                it.destructured.let { (rank, name, status) ->
                    printDevMessage("Found Party Member: rank=$rank, name=$name, status=$status", "PartyAddons")
                    party.add(
                        PartyMember(
                            name,
                            playerType,
                            status,
                            rank
                        )
                    )
                }
            }
            event.isCanceled = true
        } else if (message.startsWith("§cYou are not currently in a party.") && awaitingDelimiter != 0) {
            party.clear()
        } else if (event.message.unformattedText.startsWith("-----") && awaitingDelimiter != 0) {
            awaitingDelimiter--
            if (awaitingDelimiter == 1 || party.isEmpty()) return

            val component = UMessage("§aParty members (${party.size})\n")

            val self = party.first { it.name == mc.thePlayer.name }

            if (self.type == PartyMemberType.LEADER) {
                component.append(
                    createButton(
                        "§9[Warp] ",
                        "/p warp",
                        "§9Click to warp the party."
                    )
                ).append(
                    createButton(
                        "§e[All Invite] ",
                        "/p settings allinvite",
                        "§eClick to toggle all invite."
                    )
                ).append(
                    createButton(
                        "§6[Mute]\n",
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

            val mods = party.filter { it.type == PartyMemberType.MODERATOR }
            if (mods.isNotEmpty()) {
                component.append("\n§eMods")
                mods.forEach {
                    component.append(
                        "\n${it.status}➡§r ${it.rank}${it.name} "
                    )
                    if (self.type != PartyMemberType.LEADER) return@forEach
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
                            "§4[✖]",
                            "/p kick ${it.name}",
                            "§4Kick ${it.name}"
                        )
                    )
                }
            }

            val members = party.filter { it.type == PartyMemberType.MEMBER }
            if (members.isNotEmpty()) {
                component.append("\n§eMembers")
                members.forEach {
                    component.append(
                        "\n${it.status}➡§r ${it.rank}${it.name} "
                    )
                    if (self.type != PartyMemberType.LEADER) return@forEach
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
                            "§4[✖]",
                            "/p kick ${it.name}",
                            "§4Kick ${it.name}"
                        )
                    )
                }
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
