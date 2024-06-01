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

package gg.skytils.event.impl.screen

import gg.skytils.event.CancellableEvent
import gg.skytils.event.Event
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot


/**
 * [gg.skytils.event.mixins.gui.MixinGuiContainer.backgroundDrawn]
 */
class GuiContainerBackgroundDrawnEvent(
    val gui: GuiContainer,
    val container: Container,
    val mouseX: Int,
    val mouseY: Int,
    val partialTicks: Float
) : Event() {
    val chestName by lazy {
        getChestName(container)
    }
}

/**
 * [gg.skytils.event.mixins.gui.MixinGuiContainer.closeWindowPressed]
 */
class GuiContainerCloseWindowEvent(val gui: GuiContainer, val container: Container) : CancellableEvent() {
    val chestName by lazy {
        getChestName(container)
    }
}

/**
 * [gg.skytils.event.mixins.gui.MixinGuiContainer.onDrawSlot]
 */
class GuiContainerPreDrawSlotEvent(val gui: GuiContainer, val container: Container, val slot: Slot) : CancellableEvent() {
    val chestName by lazy {
        getChestName(container)
    }
}

/**
 * [gg.skytils.event.mixins.gui.MixinGuiContainer.onDrawSlotPost]
 */
class GuiContainerPostDrawSlotEvent(val gui: GuiContainer, val container: Container, val slot: Slot) : Event() {
    val chestName by lazy {
        getChestName(container)
    }
}

/**
 * [gg.skytils.event.mixins.gui.MixinGuiContainer.onForegroundDraw]
 */
class GuiContainerForegroundDrawnEvent(
    val gui: GuiContainer,
    val container: Container,
    val mouseX: Int,
    val mouseY: Int,
    val partialTicks: Float
) : Event() {
    val chestName by lazy {
        getChestName(container)
    }
}

/**
 * [gg.skytils.event.mixins.gui.MixinGuiContainer.onMouseClick]
 */
class GuiContainerSlotClickEvent(
    val gui: GuiContainer,
    val container: Container,
    val slot: Slot?,
    val slotId: Int,
    val clickedButton: Int,
    val clickType: Int
) : CancellableEvent() {
    val chestName by lazy {
        getChestName(container)
    }
}

private fun getChestName(container: Container): String {
    return (container as? ContainerChest)?.lowerChestInventory?.displayName?.unformattedText?.trim() ?: error("Container is not a chest")
}