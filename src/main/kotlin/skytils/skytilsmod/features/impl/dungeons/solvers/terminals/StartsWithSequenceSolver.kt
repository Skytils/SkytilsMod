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
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.impl.GuiContainerEvent
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.stripControlCodes
import java.util.regex.Pattern

class StartsWithSequenceSolver {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null || mc.theWorld == null) return
        if (!Skytils.config.startsWithSequenceTerminalSolver) return
        if (mc.currentScreen is GuiChest) {
            val chest = mc.thePlayer.openContainer as ContainerChest
            val invSlots = (mc.currentScreen as GuiChest).inventorySlots.inventorySlots
            val chestName = chest.lowerChestInventory.displayName.unformattedText.trim()
            val nameMatcher = titlePattern.matcher(chestName)
            if (nameMatcher.find()) {
                val sequence = nameMatcher.group(1)
                if (sequence != sequenceNeeded) {
                    sequenceNeeded = sequence
                    shouldClick.clear()
                } else if (shouldClick.size == 0) {
                    for (slot in invSlots) {
                        if (slot.inventory === mc.thePlayer.inventory || !slot.hasStack) continue
                        val item = slot.stack ?: continue
                        if (item.isItemEnchanted) continue
                        if (slot.slotNumber < 9 || slot.slotNumber > 44 || slot.slotNumber % 9 == 0 || slot.slotNumber % 9 == 8) continue
                        if (item.displayName.stripControlCodes().startsWith(sequenceNeeded!!)) {
                            shouldClick.add(slot.slotNumber)
                        }
                    }
                } else {
                    shouldClick.removeIf {
                        val slot = chest.getSlot(it)
                        return@removeIf slot.hasStack && slot.stack.isItemEnchanted
                    }
                }
            } else {
                shouldClick.clear()
                sequenceNeeded = null
            }
        }
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.startsWithSequenceTerminalSolver) return
        if (event.container is ContainerChest) {
            val slot = event.slot
            val chest = event.container
            val chestName = chest.lowerChestInventory.displayName.unformattedText.trim()
            if (chestName.startsWith("What starts with:")) {
                if (shouldClick.size > 0 && !shouldClick.contains(slot.slotNumber) && slot.inventory !== mc.thePlayer.inventory) {
                    event.isCanceled = true
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.startsWithSequenceTerminalSolver) return
        if (event.toolTip == null) return
        val mc = Minecraft.getMinecraft()
        val player = mc.thePlayer
        if (mc.currentScreen is GuiChest) {
            val chest = player.openContainer as ContainerChest
            val inv = chest.lowerChestInventory
            val chestName = inv.displayName.unformattedText
            if (chestName.startsWith("What starts with:")) {
                event.toolTip.clear()
            }
        }
    }

    companion object {
        private val mc = Minecraft.getMinecraft()

        @JvmField
        val shouldClick = ArrayList<Int>()
        private var sequenceNeeded: String? = null
        private val titlePattern = Pattern.compile("^What starts with: ['\"](.+)['\"]\\?$")
    }
}