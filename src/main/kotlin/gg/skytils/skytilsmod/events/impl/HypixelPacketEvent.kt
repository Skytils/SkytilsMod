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
package gg.skytils.skytilsmod.events.impl

import gg.skytils.skytilsmod.events.SkytilsEvent
import net.hypixel.modapi.packet.HypixelPacket
import net.hypixel.modapi.packet.HypixelPacketType
import net.minecraftforge.fml.common.eventhandler.Cancelable

@Cancelable
abstract class HypixelPacketEvent : SkytilsEvent() {
    abstract val direction: Direction

    class ReceiveEvent(val packet: HypixelPacket) : HypixelPacketEvent() {
        override val direction: Direction = Direction.INBOUND
    }

    class SendEvent(type: HypixelPacketType) : HypixelPacketEvent() {
        override val direction: Direction = Direction.OUTBOUND
    }

    class FailedEvent(type: HypixelPacketType) : HypixelPacketEvent() {
        override val direction: Direction = Direction.OUTBOUND
    }

    enum class Direction {
        INBOUND, OUTBOUND
    }
}