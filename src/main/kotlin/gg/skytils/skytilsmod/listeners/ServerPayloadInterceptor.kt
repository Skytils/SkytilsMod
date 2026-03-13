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

import gg.skytils.event.EventSubscriber
import gg.skytils.event.postSync
import gg.skytils.skytilsmod.Skytils.IO
import gg.skytils.skytilsmod._event.HypixelPacketReceiveEvent
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorHypixelPacketRegistry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.ClientboundHypixelPacket
import net.hypixel.modapi.packet.EventPacket
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundVersionedPacket
import kotlin.time.Duration.Companion.minutes

object ServerPayloadInterceptor : EventSubscriber {
    private val receivedPackets = MutableSharedFlow<ClientboundHypixelPacket>()
    private val neededEvents = mutableSetOf<Class<out EventPacket>>(ClientboundLocationPacket::class.java)

    init {
        neededEvents.forEach {
            runCatching {
                HypixelModAPI.getInstance().subscribeToEventPacket(it)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    override fun setup() {
        (HypixelModAPI.getInstance().registry as AccessorHypixelPacketRegistry).classToIdentifier.forEach { clazz, _ ->
            if (ClientboundHypixelPacket::class.java.isAssignableFrom(clazz)) {
                HypixelModAPI.getInstance().createHandler(clazz as Class<ClientboundHypixelPacket>) {
                    IO.launch {
                        receivedPackets.emit(it)
                    }
                    postSync(HypixelPacketReceiveEvent(it))
                }
            }
        }
    }

    suspend fun <T : ClientboundHypixelPacket> ServerboundVersionedPacket.getResponse(): T = withTimeout(1.minutes) {
        HypixelModAPI.getInstance().sendPacket(this@getResponse)
        return@withTimeout receivedPackets.filter { it.identifier == this@getResponse.identifier }.first() as T
    }
}