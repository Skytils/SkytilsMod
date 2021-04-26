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
package skytils.skytilsmod.features.impl.protectitems

import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.GuiContainerEvent
import skytils.skytilsmod.events.ItemTossEvent
import skytils.skytilsmod.features.impl.protectitems.strategy.ItemProtectStrategy
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.Utils

class ProtectItems {
    @SubscribeEvent
    fun onCloseWindow(event: GuiContainerEvent.CloseWindowEvent) {
        if (!Utils.inSkyblock) return
        if (mc.thePlayer.inventory.itemStack != null) {
            val item = mc.thePlayer.inventory.itemStack
            val extraAttr = ItemUtil.getExtraAttributes(item)
            if (ItemProtectStrategy.isAnyWorth(item, extraAttr, ItemProtectStrategy.ProtectType.USERCLOSEWINDOW)) {
                mc.thePlayer.playSound("note.bass", 1f, 0.5f)
                mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Skytils has stopped you from dropping that item!"))
                for (slot in event.container.inventorySlots) {
                    if (slot.inventory !== mc.thePlayer.inventory || slot.hasStack) continue
                    mc.playerController.windowClick(event.container.windowId, slot.slotNumber, 0, 0, mc.thePlayer)
                    break
                }
            }
        }
    }

    @SubscribeEvent
    fun onDropItem(event: ItemTossEvent) {
        if (!Utils.inSkyblock) return
        if (ItemProtectStrategy.isAnyWorth(
                event.item,
                ItemUtil.getExtraAttributes(event.item),
                ItemProtectStrategy.ProtectType.HOTBARDROPKEY
            )
        ) {
            mc.thePlayer.playSound("note.bass", 1f, 0.5f)
            mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Skytils has stopped you from dropping that item!"))
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!Utils.inSkyblock) return
        if (event.container is ContainerChest && ItemProtectStrategy.isAnyToggled()) {
            val chest = event.container
            val inv = chest.lowerChestInventory
            val chestName = inv.displayName.unformattedText.trim { it <= ' ' }
            if (event.slot != null && event.slot.hasStack) {
                var item: ItemStack = event.slot.stack ?: return
                var extraAttr = ItemUtil.getExtraAttributes(item)
                if (chestName.startsWith("Salvage")) {
                    var inSalvageGui = false
                    if (item.displayName.contains("Salvage") || item.displayName.contains("Essence")) {
                        val salvageItem = inv.getStackInSlot(13) ?: return
                        item = salvageItem
                        extraAttr = ItemUtil.getExtraAttributes(item) ?: return
                        inSalvageGui = true
                    }
                    if ((inSalvageGui || event.slot.inventory === mc.thePlayer.inventory) && ItemProtectStrategy.isAnyWorth(
                            item,
                            extraAttr,
                            ItemProtectStrategy.ProtectType.SALVAGE
                        )
                    ) {
                        mc.thePlayer.playSound("note.bass", 1f, 0.5f)
                        mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Skytils has stopped you from salvaging that item!"))
                        event.isCanceled = true
                        return
                    }
                }
                if (chestName != "Large Chest" && !chestName.contains("Auction") && inv.sizeInventory == 54) {
                    val sellItem = inv.getStackInSlot(49)
                    if (sellItem != null) {
                        if (sellItem.item === Item.getItemFromBlock(Blocks.hopper) && sellItem.displayName.contains(
                                "Sell Item"
                            ) || ItemUtil.getItemLore(sellItem).any { s: String -> s.contains("buyback") }
                        ) {
                            if (event.slotId != 49 && event.slot.inventory === mc.thePlayer.inventory && ItemProtectStrategy.isAnyWorth(
                                    item,
                                    extraAttr,
                                    ItemProtectStrategy.ProtectType.SELLTONPC
                                )
                            ) {
                                mc.thePlayer.playSound("note.bass", 1f, 0.5f)
                                mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Skytils has stopped you from selling that item!"))
                                event.isCanceled = true
                                return
                            }
                        }
                    }
                }
            }
        }
        if (event.slotId == -999 && mc.thePlayer.inventory.itemStack != null && event.clickType != 5) {
            val item = mc.thePlayer.inventory.itemStack
            val extraAttr = ItemUtil.getExtraAttributes(item)
            if (ItemProtectStrategy.isAnyWorth(item, extraAttr, ItemProtectStrategy.ProtectType.CLICKOUTOFWINDOW)) {
                mc.thePlayer.playSound("note.bass", 1f, 0.5f)
                mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Skytils has stopped you from dropping that item!"))
                event.isCanceled = true
                return
            }
        }
        if (event.clickType == 4 && event.slotId != -999 && event.slot != null && event.slot.hasStack) {
            val item = event.slot.stack
            val extraAttr = ItemUtil.getExtraAttributes(item)
            if (ItemProtectStrategy.isAnyWorth(item, extraAttr, ItemProtectStrategy.ProtectType.DROPKEYININVENTORY)) {
                mc.thePlayer.playSound("note.bass", 1f, 0.5f)
                mc.thePlayer.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "Skytils has stopped you from dropping that item!"))
                event.isCanceled = true
                return
            }
        }
    }

}