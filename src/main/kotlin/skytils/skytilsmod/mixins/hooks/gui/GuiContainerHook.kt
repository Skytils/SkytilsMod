/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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
package skytils.skytilsmod.mixins.hooks.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import net.minecraft.util.ChatComponentText
import net.minecraftforge.common.MinecraftForge
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import skytils.skytilsmod.events.GuiContainerEvent
import skytils.skytilsmod.events.GuiContainerEvent.CloseWindowEvent
import skytils.skytilsmod.events.GuiContainerEvent.SlotClickEvent

class GuiContainerHook(guiAny: Any) {

    val gui: GuiContainer

    init {
        gui = guiAny as GuiContainer
    }

    fun closeWindowPressed(ci: CallbackInfo) {
        try {
            MinecraftForge.EVENT_BUS.post(CloseWindowEvent(gui, gui.inventorySlots))
        } catch (e: Throwable) {
            Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils caught and logged an exception at GuiContainerEvent.CloseWindowEvent. Please report this on the Discord server."))
            e.printStackTrace()
        }
    }

    fun backgroundDrawn(mouseX: Int, mouseY: Int, partialTicks: Float, ci: CallbackInfo) {
        try {
            MinecraftForge.EVENT_BUS.post(
                GuiContainerEvent.BackgroundDrawnEvent(
                    gui,
                    gui.inventorySlots,
                    mouseX,
                    mouseY,
                    partialTicks
                )
            )
        } catch (e: Throwable) {
            Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils caught and logged an exception at GuiContainerEvent.BackgroundDrawnEvent. Please report this on the Discord server."))
            e.printStackTrace()
        }
    }

    fun onDrawSlot(slot: Slot, ci: CallbackInfo) {
        try {
            if (MinecraftForge.EVENT_BUS.post(
                    GuiContainerEvent.DrawSlotEvent.Pre(
                        gui,
                        gui.inventorySlots,
                        slot
                    )
                )
            ) ci.cancel()
        } catch (e: Throwable) {
            Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils caught and logged an exception at GuiContainerEvent.DrawSlotEvent.Pre. Please report this on the Discord server."))
            e.printStackTrace()
        }
    }

    fun onDrawSlotPost(slot: Slot, ci: CallbackInfo) {
        try {
            MinecraftForge.EVENT_BUS.post(GuiContainerEvent.DrawSlotEvent.Post(gui, gui.inventorySlots, slot))
        } catch (e: Throwable) {
            Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils caught and logged an exception at GuiContainerEvent.DrawSlotEvent.Post. Please report this on the Discord server."))
            e.printStackTrace()
        }
    }

    fun onMouseClick(slot: Slot?, slotId: Int, clickedButton: Int, clickType: Int, ci: CallbackInfo) {
        try {
            if (MinecraftForge.EVENT_BUS.post(
                    SlotClickEvent(
                        gui,
                        gui.inventorySlots,
                        slot,
                        slotId,
                        clickedButton,
                        clickType
                    )
                )
            ) ci.cancel()
        } catch (e: Throwable) {
            Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText("§cSkytils caught and logged an exception at GuiContainerEvent.SlotClickEvent. Please report this on the Discord server."))
            e.printStackTrace()
        }
    }
}