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

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.IO
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.MC
import gg.skytils.skytilsmod.events.impl.HypixelPacketEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorHypixelModAPI
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.ifNull
import io.netty.buffer.Unpooled
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.error.ErrorReason
import net.hypixel.modapi.packet.ClientboundHypixelPacket
import net.hypixel.modapi.packet.EventPacket
import net.hypixel.modapi.packet.impl.clientbound.ClientboundHelloPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundVersionedPacket
import net.hypixel.modapi.serializer.PacketSerializer
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.reflect.KClass
import kotlin.time.Duration.Companion.minutes

object ServerPayloadInterceptor {
    private val receivedPackets = MutableSharedFlow<ClientboundHypixelPacket>()
    private var didSetPacketSender = false
    private val neededEvents = mutableSetOf<KClass<out EventPacket>>(ClientboundLocationPacket::class)

    init {
        neededEvents.forEach {
            HypixelModAPI.getInstance().subscribeToEventPacket(it.java)
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
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
                            HypixelPacketEvent.FailedEvent(id, reason).postAndCatch()
                        } else {
                            val packet = registry.createClientboundPacket(id, packetSerializer)
                            IO.launch {
                                receivedPackets.emit(packet)
                            }
                            HypixelPacketEvent.ReceiveEvent(packet).postAndCatch()
                        }
                    }.onFailure {
                        it.printStackTrace()
                    }
                    data.release()
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onSendPacket(event: PacketEvent.SendEvent) {
        if (event.packet is C17PacketCustomPayload) {
            val registry = HypixelModAPI.getInstance().registry
            val id = event.packet.channelName
            if (registry.isRegistered(id)) {
                println("Sent Hypixel packet $id")
                HypixelPacketEvent.SendEvent(id).postAndCatch()
            }
        }
    }

    @SubscribeEvent
    fun onHypixelPacket(event: HypixelPacketEvent.ReceiveEvent) {
        if (event.packet is ClientboundHelloPacket) {
            val modAPI = HypixelModAPI.getInstance()
            modAPI as AccessorHypixelModAPI
            if (modAPI.packetSender == null) {
                println("Hypixel Mod API packet sender is not set, Skytils will set the packet sender.")
                modAPI.setPacketSender {
                    return@setPacketSender getNetClientHandler()?.addToSendQueue((it as ServerboundVersionedPacket).toCustomPayload()).ifNull {
                        println("Failed to send packet ${it.identifier}")
                    } != null
                }
                didSetPacketSender = true
            }
            Skytils.launch {
                while (getNetClientHandler() == null) {
                    println("Waiting for client handler to be set.")
                    delay(50L)
                }
                withContext(Dispatchers.MC) {
                    neededEvents.forEach {
                        HypixelModAPI.getInstance().subscribeToEventPacket(it.java)
                    }
                    if (didSetPacketSender) modAPI.invokeSendRegisterPacket(true)
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
        getNetClientHandler()?.addToSendQueue(packet)
        return@withTimeout receivedPackets.filter { it.identifier == this@getResponse.identifier }.first() as T
    }

    private fun getNetClientHandler() = mc.netHandler ?: Utils.lastNHPC
}