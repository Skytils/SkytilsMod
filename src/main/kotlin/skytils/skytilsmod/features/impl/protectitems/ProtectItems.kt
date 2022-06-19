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
package skytils.skytilsmod.features.impl.protectitems

import gg.essential.universal.UChat
import gg.essential.universal.UKeyboard
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.failPrefix
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.SoundQueue
import skytils.skytilsmod.events.impl.GuiContainerEvent
import skytils.skytilsmod.events.impl.ItemTossEvent
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
                for (slot in event.container.inventorySlots) {
                    if (slot.inventory !== mc.thePlayer.inventory || slot.hasStack || !slot.isItemValid(item)) continue
                    mc.playerController.windowClick(event.container.windowId, slot.slotNumber, 0, 0, mc.thePlayer)
                    notifyStopped(null, "dropping")
                    return
                }
                notifyStopped(null, "closing the window on")
                event.isCanceled = true
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
            notifyStopped(event, "dropping")
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!Utils.inSkyblock) return
        if (event.container is ContainerChest && ItemProtectStrategy.isAnyToggled()) {
            val inv = event.container.lowerChestInventory
            val chestName = event.chestName
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
                        notifyStopped(event, "salvaging")
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
                                notifyStopped(event, "selling")
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
                notifyStopped(event, "dropping")
                return
            }
        }
        if (event.clickType == 4 && event.slotId != -999 && event.slot != null && event.slot.hasStack) {
            val item = event.slot.stack
            val extraAttr = ItemUtil.getExtraAttributes(item)
            if (ItemProtectStrategy.isAnyWorth(item, extraAttr, ItemProtectStrategy.ProtectType.DROPKEYININVENTORY)) {
                notifyStopped(event, "dropping")
                return
            }
        }
    }

    private fun isBypassActive(): Boolean {
        return Skytils.config.protectItemBypass && UKeyboard.isAltKeyDown()
    }

    private fun notifyStopped(event: Event?, action: String) {
        SoundQueue.addToQueue("note.bass", 0.5f, 1f)
        UChat.chat("$failPrefix §cStopped you from $action that item!")
        event?.isCanceled = !isBypassActive()
    }

}