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
package gg.skytils.skytilsmod.features.impl.dungeons.solvers.terminals

import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.screen.GuiContainerSlotClickEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures.dungeonFloorNumber
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.startsWithAny
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraft.inventory.ContainerChest

object TerminalFeatures : EventSubscriber {

    fun isInPhase3(): Boolean {
        return (SuperSecretSettings.azooPuzzoo || DungeonTimer.phase2ClearTime != -1L) && DungeonTimer.terminalClearTime == -1L && dungeonFloorNumber == 7
    }

    override fun setup() {
        register(::onSlotClickHigh, EventPriority.High)
        register(::onSlotClick)
        register(::onTooltip, EventPriority.Lowest)
    }

    fun onSlotClickHigh(event: GuiContainerSlotClickEvent) {
        if (!Utils.inDungeons || !Skytils.config.blockIncorrectTerminalClicks || event.container !is ContainerChest) return
        if (event.chestName == "Correct all the panes!") {
            if (event.slot?.stack?.displayName?.stripControlCodes()?.startsWith("On") == true) {
                event.cancelled = true
            }
        }
    }

    fun onSlotClick(event: GuiContainerSlotClickEvent) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.middleClickTerminals) return
        if (event.container is ContainerChest) {
            val chestName = event.chestName
            if (Utils.equalsOneOf(
                    chestName,
                    "Navigate the maze!",
                    "Correct all the panes!",
                    "Click in order!",
                    "Click the button on time!"
                ) || chestName.startsWithAny(
                    "What starts with:",
                    "Select all the"
                ) || (chestName == "Change all to same color!" && event.clickedButton != 1 && Utils.equalsOneOf(
                    event.clickType,
                    0,
                    1,
                    6
                ))
            ) {
                event.cancelled = true
                mc.playerController.windowClick(event.container.windowId, event.slotId, 0, 4, mc.thePlayer)
            }
        }
    }

    fun onTooltip(event: gg.skytils.event.impl.item.ItemTooltipEvent) {
        if (!Utils.inDungeons) return
        val chest = mc.thePlayer?.openContainer as? ContainerChest ?: return

        val inv = chest.lowerChestInventory
        val chestName = inv.displayName.unformattedText
        if (chestName == "Click the button on time!" || chestName == "Correct all the panes!") {
            event.tooltip.clear()
        }
    }
}