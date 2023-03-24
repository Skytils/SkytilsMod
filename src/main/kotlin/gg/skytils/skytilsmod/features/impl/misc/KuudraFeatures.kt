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

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KuudraFeatures {

    private var awaitingJoinFrom: String? = null
    private var lastWarpAt: Long = 0

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: PacketEvent.ReceiveEvent) {
        if (!Utils.isOnHypixel || event.packet !is S02PacketChat || event.packet.type == 2.toByte()) return
        val msg = event.packet.chatComponent
        val formatted = msg.formattedText

        val kuudraPlayerName = Skytils.config.kuudraAutoRepartyPlayer
        if (kuudraPlayerName.isNotEmpty()) {
            if (formatted.endsWith("§r§e, summoned you to their server.§r")) {
                lastWarpAt = System.currentTimeMillis()
            } else if (formatted == "§e[NPC] §cElle§f: §rTalk with me to begin!§r" && System.currentTimeMillis() - lastWarpAt > 5000) {
                Skytils.sendMessageQueue.add("/party invite $kuudraPlayerName")
                awaitingJoinFrom = kuudraPlayerName
            } else if (awaitingJoinFrom != null) {
                if (formatted.endsWith("$awaitingJoinFrom §r§ejoined the party.§r")) {
                    Skytils.sendMessageQueue.add("/party warp")
                    Skytils.sendMessageQueue.add("/party transfer $awaitingJoinFrom")
                    awaitingJoinFrom = null

                    Skytils.sendMessageQueue.add("/party leave")
                } else if (formatted.startsWith("§eThe party invite to ") && formatted.endsWith("$awaitingJoinFrom §r§ehas expired§r")) {
                    UChat.chat("${Skytils.failPrefix} §f${awaitingJoinFrom} did not join in time. Repartying cancelled.")
                    awaitingJoinFrom = null
                }
            } else if (formatted.contains("$kuudraPlayerName §r§ehas invited you to join their party!")) {
                Skytils.sendMessageQueue.add("/party accept $kuudraPlayerName")
            }
        }
    }
}