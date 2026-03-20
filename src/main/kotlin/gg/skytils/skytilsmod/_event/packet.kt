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

package gg.skytils.skytilsmod._event

import gg.skytils.event.CancellableEvent
import net.minecraft.network.packet.Packet

/**
 * [gg.skytils.skytilsmod.mixins.transformers.events.MixinNetHandlerPlayClient.addToSendQueue]
 */
class PacketSendEvent<T : Packet<*>>(val packet: T) : CancellableEvent()

/**
 * [gg.skytils.event.mixins.network.MixinNetworkManager.channelRead0]
 */
class PacketReceiveEvent<T : Packet<*>>(val packet: T) : CancellableEvent()

/**
 * This is run on the main thread rather than a netty thread.
 * This event will be posted after [PacketReceiveEvent]
 *
 * [gg.skytils.event.mixins.network.MixinPacketThreadUtil.processPacket]
 */
class MainThreadPacketReceiveEvent<T : Packet<*>>(val packet: T) : CancellableEvent()