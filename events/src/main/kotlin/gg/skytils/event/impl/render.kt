package gg.skytils.event.impl

import gg.skytils.event.CancellableEvent
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

class InventoryDrawSlotEvent(val context: DrawContext, val slot: Slot, val mouseX: Int, val mouseY: Int): CancellableEvent()

class HotbarDrawSlotEvent(val context: DrawContext, val stack: ItemStack, val x: Int, val y: Int): CancellableEvent()