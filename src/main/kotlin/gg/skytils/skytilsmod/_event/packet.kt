package gg.skytils.skytilsmod._event

import gg.skytils.event.CancellableEvent
import net.minecraft.network.packet.Packet


class PacketReceiveEvent<T : Packet<*>>(val packet: T) : CancellableEvent()

class MainThreadPacketReceiveEvent<T : Packet<*>>(val packet: T) : CancellableEvent()