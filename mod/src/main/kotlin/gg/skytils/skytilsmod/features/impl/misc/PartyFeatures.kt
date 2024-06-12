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

import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.ChatMessageSentEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.utils.Utils

object PartyFeatures : EventSubscriber {
    private val subCommands =
        setOf(
            "help",
            "list",
            "leave",
            "warp",
            "disband",
            "promote",
            "demote",
            "transfer",
            "kick",
            "kickoffline",
            "settings",
            "poll",
            "mute",
            "challenge",
            "answer",
            "join"
        )

    override fun setup() {
        register(::onChatSend)
    }

    fun onChatSend(event: ChatMessageSentEvent) {
        if (!Skytils.config.multiplePartyInviteFix || !Utils.isOnHypixel || !event.addToHistory) return

        val m = event.message
        if (!m.startsWith("/p ") && !m.startsWith("/party ")) return
        val args = m.split(" ") as ArrayList<String>
        args.removeFirst()
        if (args.isEmpty()) return

        val subcommand = args.removeFirstOrNull() ?: return
        if (subCommands.contains(subcommand)) return
        if (subcommand != "invite")
            args.add(0, subcommand)

        args.forEach {
            Skytils.sendMessageQueue.add("/p invite $it")
        }

        event.cancelled = true
        mc.ingameGUI.chatGUI.addToSentMessages(m)

    }
}