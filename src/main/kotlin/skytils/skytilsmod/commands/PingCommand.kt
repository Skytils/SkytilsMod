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

package skytils.skytilsmod.commands

import gg.essential.universal.UChat
import net.minecraft.command.ICommandSender
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S37PacketStatistics
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import kotlin.math.abs
import kotlin.time.ExperimentalTime

object PingCommand : BaseCommand() {
    override fun getCommandName(): String = "skytilsping"
    override fun getCommandUsage(sender: ICommandSender): String = "/$commandName"

    var pingedAt = -1L

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (pingedAt != -1L) return UChat.chat("§cAlready pinging!")
        UChat.chat("§aSkytils is sending a request...")
        mc.thePlayer.sendQueue.networkManager.sendPacket(
            C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS),
            {
                pingedAt = System.nanoTime()
            })
    }

    @OptIn(ExperimentalTime::class)
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPacket(event: PacketEvent.ReceiveEvent) {
        if (pingedAt != -1L) {
            when (event.packet) {
                is S01PacketJoinGame -> pingedAt = -1L
                is S37PacketStatistics -> {
                    val diff = (abs(System.nanoTime() - pingedAt) / 1_000_000.0).roundToPrecision(2)
                    pingedAt = -1L
                    UChat.chat(
                        "§9§lSkytils ➜ §${
                            when {
                                diff < 100 -> "a"
                                diff < 249 -> "6"
                                else -> "c"
                            }
                        }$diff §7ms"
                    )
                }
            }
        }
    }
}