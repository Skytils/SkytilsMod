/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.HypixelPacketEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import io.netty.buffer.Unpooled
import net.hypixel.modapi.packet.HypixelPacket
import net.hypixel.modapi.packet.HypixelPacketType
import net.hypixel.modapi.serializer.PacketSerializer
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ServerPayloadInterceptor {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
        if (event.packet is S3FPacketCustomPayload) {
            HypixelPacketType.getByIdentifier(event.packet.channelName)?.let { pt ->
                val packetSerializer = PacketSerializer(event.packet.bufferData)
                if (!packetSerializer.readBoolean()) {
                    HypixelPacketEvent.FailedEvent(pt).postAndCatch()
                } else {
                    val packet = pt.packetFactory.apply(packetSerializer)
                    HypixelPacketEvent.ReceiveEvent(packet).postAndCatch()
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onSendPacket(event: PacketEvent.SendEvent) {
        if (event.packet is C17PacketCustomPayload) {
            HypixelPacketType.getByIdentifier(event.packet.channelName)?.let { pt ->
                HypixelPacketEvent.SendEvent(pt).postAndCatch()
            }
        }
    }

    fun HypixelPacket.send() {
        val buffer = PacketBuffer(Unpooled.buffer())
        val serializer = PacketSerializer(buffer)
        this.write(serializer)
        val packet = C17PacketCustomPayload(this.type.identifier, buffer)
        mc.thePlayer?.sendQueue?.addToSendQueue(packet)
    }
}