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
package skytils.skytilsmod.events

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

abstract class GuiContainerEvent(val gui: GuiContainer, val container: Container) : Event() {
    class BackgroundDrawnEvent(
        gui: GuiContainer,
        container: Container,
        val mouseX: Int,
        val mouseY: Int,
        val partialTicks: Float
    ) : GuiContainerEvent(gui, container)

    class CloseWindowEvent(gui: GuiContainer, container: Container) : GuiContainerEvent(gui, container)
    open class DrawSlotEvent(gui: GuiContainer, container: Container, val slot: Slot) :
        GuiContainerEvent(gui, container) {
        @Cancelable
        class Pre(gui: GuiContainer, container: Container, slot: Slot) : DrawSlotEvent(gui, container, slot)
        class Post(gui: GuiContainer, container: Container, slot: Slot) : DrawSlotEvent(gui, container, slot)
    }

    @Cancelable
    class SlotClickEvent(
        gui: GuiContainer,
        container: Container,
        val slot: Slot?,
        val slotId: Int,
        val clickedButton: Int,
        val clickType: Int
    ) : GuiContainerEvent(gui, container)
}