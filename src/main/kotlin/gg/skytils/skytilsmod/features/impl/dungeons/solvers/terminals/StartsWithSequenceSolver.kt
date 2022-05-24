/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern
import kotlin.random.Random

object StartsWithSequenceSolver {


    @JvmField
    val shouldClick = ArrayList<Int>()
    private var sequenceNeeded: String? = null
    private val titlePattern = Pattern.compile("^What starts with: ['\"](.+)['\"]\\?$")

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!Utils.inDungeons || !Skytils.config.startsWithSequenceTerminalSolver || event.container !is ContainerChest) return
        val nameMatcher = titlePattern.matcher(event.chestName)
        if (nameMatcher.find()) {
            val sequence = nameMatcher.group(1)
            if (sequence != sequenceNeeded) {
                sequenceNeeded = sequence
                shouldClick.clear()
            } else if (shouldClick.size == 0) {
                for (slot in event.container.inventorySlots) {
                    if (slot.inventory === mc.thePlayer?.inventory || !slot.hasStack) continue
                    val item = slot.stack ?: continue
                    if (item.isItemEnchanted) continue
                    if (slot.slotNumber < 9 || slot.slotNumber > 44 || slot.slotNumber % 9 == 0 || slot.slotNumber % 9 == 8) continue
                    if (SuperSecretSettings.bennettArthur) {
                        if (Random.nextInt(3) == 0) SelectAllColorSolver.shouldClick.add(slot.slotNumber)
                    } else if (item.displayName.stripControlCodes().startsWith(sequenceNeeded!!)) {
                        shouldClick.add(slot.slotNumber)
                    }
                }
            } else {
                shouldClick.removeIf {
                    val slot = event.container.getSlot(it)
                    return@removeIf slot.hasStack && slot.stack.isItemEnchanted
                }
            }
        } else {
            shouldClick.clear()
            sequenceNeeded = null
        }
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.startsWithSequenceTerminalSolver) return
        if (event.container is ContainerChest) {
            val slot = event.slot
            if (event.chestName.startsWith("What starts with:")) {
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
        val container = mc.thePlayer?.openContainer
        if (container is ContainerChest) {
            val inv = container.lowerChestInventory
            val chestName = inv.displayName.unformattedText
            if (chestName.startsWith("What starts with:")) {
                event.toolTip.clear()
            }
        }
    }
}