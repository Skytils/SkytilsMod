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
package skytils.skytilsmod.features.impl.dungeons.solvers.terminals

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.GuiContainerEvent.SlotClickEvent
import skytils.skytilsmod.utils.stripControlCodes
import skytils.skytilsmod.utils.Utils

class TerminalFeatures {
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    fun onGUIMouseInput(event: GuiScreenEvent.MouseInputEvent.Pre) {
        if (!Utils.inDungeons) return
        // Skytils doesn't use this event, so it must be another mod that cancelled it
        if (event.isCanceled && Skytils.config.blockIncorrectTerminalClicks) {
            if (mc.thePlayer.openContainer != null && mc.thePlayer.openContainer is ContainerChest) {
                val chest = mc.thePlayer.openContainer as ContainerChest
                val chestName = chest.lowerChestInventory.displayName.unformattedText.trim { it <= ' ' }
                if (chestName == "Navigate the maze!" || chestName == "Correct all the panes!" || chestName.startsWith("Select all the") && Skytils.config.selectAllColorTerminalSolver || chestName.startsWith(
                        "What starts with"
                    ) && Skytils.config.startsWithSequenceTerminalSolver || chestName == "Click in order!" && Skytils.config.clickInOrderTerminalSolver
                ) {
                    event.isCanceled = false
                }
            }
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.middleClickTerminals) return
        if (event.container is ContainerChest) {
            val chest = event.container
            val chestName = chest.lowerChestInventory.displayName.unformattedText.trim { it <= ' ' }
            if (chestName == "Navigate the maze!" || chestName == "Correct all the panes!") {
                event.isCanceled = true
                mc.playerController.windowClick(event.container.windowId, event.slotId, 2, 0, mc.thePlayer)
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inDungeons) return
        if (event.toolTip == null) return
        val mc = Minecraft.getMinecraft()
        val player = mc.thePlayer
        if (mc.currentScreen is GuiChest) {
            val chest = player.openContainer as ContainerChest
            val inv = chest.lowerChestInventory
            val chestName = inv.displayName.unformattedText
            if (chestName == "Navigate the maze!" || chestName == "Correct all the panes!") {
                event.toolTip.clear()
            }
        }
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
    }
}