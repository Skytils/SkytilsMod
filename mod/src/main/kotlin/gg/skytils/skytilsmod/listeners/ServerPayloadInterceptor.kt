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

import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.postSync
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.IO
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.*
import gg.skytils.skytilsmod.core.MC
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorHypixelModAPI
import gg.skytils.skytilsmod.utils.ifNull
import io.netty.buffer.Unpooled
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.error.ErrorReason
import net.hypixel.modapi.packet.ClientboundHypixelPacket
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundVersionedPacket
import net.hypixel.modapi.serializer.PacketSerializer
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.network.play.server.S3FPacketCustomPayload
import kotlin.time.Duration.Companion.minutes

object ServerPayloadInterceptor : EventSubscriber {
    private val receivedPackets = MutableSharedFlow<ClientboundHypixelPacket>()

    override fun setup() {
        register(::onReceivePacket, EventPriority.Highest)
        register(::onSendPacket, EventPriority.Highest)
        register(::onHypixelPacket)
    }

    fun onReceivePacket(event: PacketReceiveEvent<*>) {
        if (event.packet is S3FPacketCustomPayload) {
            val registry = HypixelModAPI.getInstance().registry
            val id = event.packet.channelName
            if (registry.isRegistered(id)) {
                println("Received Hypixel packet $id")
                val data = event.packet.bufferData
                synchronized(data) {
                    data.retain()
                    runCatching {
                        val packetSerializer = PacketSerializer(data.duplicate())
                        if (!packetSerializer.readBoolean()) {
                            val reason = ErrorReason.getById(packetSerializer.readVarInt())
                            postSync(HypixelPacketFailedEvent(id, reason))
                        } else {
                            val packet = registry.createClientboundPacket(id, packetSerializer)
                            IO.launch {
                                receivedPackets.emit(packet)
                            }
                            postSync(HypixelPacketReceiveEvent(packet))
                        }
                    }.onFailure {
                        it.printStackTrace()
                    }
                    data.release()
                }
            }
        }
    }

    fun onSendPacket(event: PacketSendEvent<*>) {
        if (event.packet is C17PacketCustomPayload) {
            val registry = HypixelModAPI.getInstance().registry
            val id = event.packet.channelName
            if (registry.isRegistered(id)) {
                println("Sent Hypixel packet $id")
                postSync(HypixelPacketSendEvent(id))
            }
        }
    }


    fun onHypixelPacket(event: HypixelPacketReceiveEvent) {
        if (event.packet is ClientboundHelloPacket) {
            val modAPI = HypixelModAPI.getInstance()
            modAPI as AccessorHypixelModAPI
            if (modAPI.packetSender == null) {
                println("Hypixel Mod API packet sender is not set, Skytils will set the packet sender.")
                modAPI.setPacketSender {
                    return@setPacketSender mc.netHandler?.addToSendQueue((it as ServerboundVersionedPacket).toCustomPayload()).ifNull {
                        println("Failed to send packet ${it.identifier}")
                    } != null
                }
            }
            Skytils.launch {
                while (mc.netHandler == null) {
                    println("Waiting for client handler to be set.")
                    delay(50L)
                }
                withContext(Dispatchers.MC) {
                    modAPI.subscribeToEventPacket(ClientboundLocationPacket::class.java)
                    modAPI.invokeSendRegisterPacket(true)
                }
            }
        }
    }

    fun ServerboundVersionedPacket.toCustomPayload(): C17PacketCustomPayload {
        val buffer = PacketBuffer(Unpooled.buffer())
        val serializer = PacketSerializer(buffer)
        this.write(serializer)
        return C17PacketCustomPayload(this.identifier, buffer)
    }

    suspend fun <T : ClientboundHypixelPacket> ServerboundVersionedPacket.getResponse(): T = withTimeout(1.minutes) {
        val packet: C17PacketCustomPayload = this@getResponse.toCustomPayload()
        mc.netHandler?.addToSendQueue(packet)
        return@withTimeout receivedPackets.filter { it.identifier == this@getResponse.identifier }.first() as T
    }
}