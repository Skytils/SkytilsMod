/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.mixins.hooks.gui

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import sharttils.sharttilsmod.events.impl.GuiContainerEvent
import sharttils.sharttilsmod.events.impl.GuiContainerEvent.CloseWindowEvent
import sharttils.sharttilsmod.events.impl.GuiContainerEvent.SlotClickEvent

class GuiContainerHook(guiAny: Any) {

    val gui: GuiContainer

    init {
        gui = guiAny as GuiContainer
    }

    fun closeWindowPressed(ci: CallbackInfo) {
        if (CloseWindowEvent(gui, gui.inventorySlots).postAndCatch()) ci.cancel()
    }

    fun backgroundDrawn(mouseX: Int, mouseY: Int, partialTicks: Float, ci: CallbackInfo) {
        GuiContainerEvent.BackgroundDrawnEvent(
            gui,
            gui.inventorySlots,
            mouseX,
            mouseY,
            partialTicks
        ).postAndCatch()
    }

    fun foregroundDrawn(mouseX: Int, mouseY: Int, partialTicks: Float, ci: CallbackInfo) {
        GuiContainerEvent.ForegroundDrawnEvent(gui, gui.inventorySlots, mouseX, mouseY, partialTicks).postAndCatch()
    }

    fun onDrawSlot(slot: Slot, ci: CallbackInfo) {
        if (GuiContainerEvent.DrawSlotEvent.Pre(
                gui,
                gui.inventorySlots,
                slot
            ).postAndCatch()
        ) ci.cancel()
    }

    fun onDrawSlotPost(slot: Slot, ci: CallbackInfo) {
        GuiContainerEvent.DrawSlotEvent.Post(gui, gui.inventorySlots, slot).postAndCatch()
    }

    fun onMouseClick(slot: Slot?, slotId: Int, clickedButton: Int, clickType: Int, ci: CallbackInfo) {
        if (
            SlotClickEvent(
                gui,
                gui.inventorySlots,
                slot,
                slotId,
                clickedButton,
                clickType
            ).postAndCatch()
        ) ci.cancel()
    }
}