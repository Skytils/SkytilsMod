package gg.skytils.skytilsmod._event

import gg.skytils.event.CancellableEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.network.packet.Packet
import net.minecraft.screen.slot.Slot


class PacketReceiveEvent<T : Packet<*>>(val packet: T) : CancellableEvent()

class MainThreadPacketReceiveEvent<T : Packet<*>>(val packet: T) : CancellableEvent()

class DrawSlotEvent(val context: DrawContext, val slot: Slot, val mouseX: Int, val mouseY: Int): CancellableEvent()