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

import com.google.common.collect.ImmutableList
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.EnumDyeColor
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.GuiContainerEvent
import skytils.skytilsmod.events.GuiContainerEvent.SlotClickEvent
import skytils.skytilsmod.utils.Utils
import java.util.*

class SelectAllColorSolver {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null || mc.theWorld == null) return
        if (!Skytils.config.selectAllColorTerminalSolver) return
        if (mc.currentScreen is GuiChest) {
            val chest = mc.thePlayer.openContainer as ContainerChest
            val invSlots = (mc.currentScreen as GuiChest).inventorySlots.inventorySlots
            val chestName = chest.lowerChestInventory.displayName.unformattedText.trim { it <= ' ' }
            if (chestName.startsWith("Select all the")) {
                var promptColor: String? = null
                for (color in EnumDyeColor.values()) {
                    val unlocalized = color.getName().replace("_".toRegex(), " ").toUpperCase(Locale.ENGLISH)
                    if (chestName.contains(unlocalized)) {
                        promptColor = color.unlocalizedName
                        break
                    }
                }
                if (promptColor != colorNeeded) {
                    colorNeeded = promptColor
                    shouldClick.clear()
                } else if (shouldClick.size == 0) {
                    for (slot in invSlots) {
                        if (slot.inventory === mc.thePlayer.inventory || !slot.hasStack) continue
                        val item = slot.stack ?: continue
                        if (item.isItemEnchanted) continue
                        if (slot.slotNumber < 9 || slot.slotNumber > 44 || slot.slotNumber % 9 == 0 || slot.slotNumber % 9 == 8) continue
                        if (item.unlocalizedName.contains(colorNeeded!!)) {
                            shouldClick.add(slot.slotNumber)
                        }
                    }
                } else {
                    for (slotNum in ImmutableList.copyOf(shouldClick)) {
                        val slot = chest.getSlot(slotNum)
                        if (slot.hasStack && slot.stack.isItemEnchanted) {
                            shouldClick.remove(slotNum as Int)
                        }
                    }
                }
            } else {
                shouldClick.clear()
                colorNeeded = null
            }
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!Utils.inDungeons) return
        if (event.container is ContainerChest) {
            val chest = event.container as ContainerChest
            val chestName = chest.lowerChestInventory.displayName.unformattedText.trim { it <= ' ' }
            if (chestName.startsWith("Select all the")) {
                event.isCanceled = true
                if (Skytils.config.blockIncorrectTerminalClicks && event.slot != null) {
                    if (shouldClick.size > 0) {
                        if (shouldClick.stream().noneMatch { slotNum: Int -> slotNum == event.slot!!.slotNumber }) {
                            return
                        }
                    }
                }
                mc.playerController.windowClick(event.container.windowId, event.slotId, 2, 0, mc.thePlayer)
            }
        }
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.selectAllColorTerminalSolver) return
        if (event.container is ContainerChest) {
            val slot = event.slot
            val chest = event.container as ContainerChest
            val chestName = chest.lowerChestInventory.displayName.unformattedText.trim { it <= ' ' }
            if (chestName.startsWith("Select all the")) {
                if (shouldClick.size > 0 && !shouldClick.contains(slot.slotNumber) && slot.inventory !== mc.thePlayer.inventory) {
                    event.isCanceled = true
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.selectAllColorTerminalSolver) return
        if (event.toolTip == null) return
        val mc = Minecraft.getMinecraft()
        val player = mc.thePlayer
        if (mc.currentScreen is GuiChest) {
            val chest = player.openContainer as ContainerChest
            val inv = chest.lowerChestInventory
            val chestName = inv.displayName.unformattedText
            if (chestName.startsWith("Select all the")) {
                event.toolTip.clear()
            }
        }
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        @JvmField
        val shouldClick = ArrayList<Int>()
        private var colorNeeded: String? = null
    }
}