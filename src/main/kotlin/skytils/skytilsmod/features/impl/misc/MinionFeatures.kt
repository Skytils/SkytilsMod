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
package skytils.skytilsmod.features.impl.misc

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.GuiContainerEvent.SlotClickEvent
import skytils.skytilsmod.events.GuiRenderItemEvent
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import skytils.skytilsmod.utils.StringUtils.stripControlCodes
import skytils.skytilsmod.utils.Utils

class MinionFeatures {
    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui is GuiChest) {
            val chest = (event.gui as GuiChest).inventorySlots as ContainerChest
            val chestName = chest.lowerChestInventory.displayName.unformattedText.trim { it <= ' ' }
            if (chestName == "Minion Chest") return
        }
        blockUnenchanted = false
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inSkyblock) return
        if (event.packet is S29PacketSoundEffect) {
            val packet = event.packet
            if (packet.soundName == "random.chestopen" && packet.pitch == 1f && packet.volume == 1f) {
                blockUnenchanted = false
            }
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!Utils.inSkyblock) return
        if (event.container is ContainerChest) {
            val chest = event.container
            val inventory = chest.lowerChestInventory
            val slot = event.slot ?: return
            val item = slot.stack
            val inventoryName = inventory.displayName.unformattedText
            if (Skytils.config.onlyCollectEnchantedItems && inventoryName.contains("Minion") && item != null) {
                if (!item.isItemEnchanted && item.item != Items.skull) {
                    if (inventoryName == "Minion Chest") {
                        if (!blockUnenchanted) {
                            for (i in 0 until inventory.sizeInventory) {
                                val stack = inventory.getStackInSlot(i) ?: continue
                                if (stack.isItemEnchanted || stack.item == Items.skull) {
                                    blockUnenchanted = true
                                    break
                                }
                            }
                        }
                        if (blockUnenchanted && slot.inventory !== mc.thePlayer.inventory) event.isCanceled = true
                    } else {
                        val minionType = inventory.getStackInSlot(4)
                        if (minionType != null) {
                            if (stripControlCodes(minionType.displayName).contains("Minion")) {
                                if (!blockUnenchanted) {
                                    val firstUpgrade = inventory.getStackInSlot(37)
                                    val secondUpgrade = inventory.getStackInSlot(46)
                                    if (firstUpgrade != null) {
                                        if (stripControlCodes(firstUpgrade.displayName).contains("Super Compactor 3000")) {
                                            blockUnenchanted = true
                                        }
                                    }
                                    if (secondUpgrade != null) {
                                        if (stripControlCodes(secondUpgrade.displayName).contains("Super Compactor 3000")) {
                                            blockUnenchanted = true
                                        }
                                    }
                                }
                                val index = slot.slotIndex
                                if (blockUnenchanted && slot.inventory !== mc.thePlayer.inventory && index >= 21 && index <= 43 && index % 9 >= 3 && index % 9 <= 7) {
                                    event.isCanceled = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderItemOverlayPost(event: GuiRenderItemEvent.RenderOverlayEvent.Post) {
        val item = event.stack
        if (!Utils.inSkyblock || item == null || item.stackSize != 1) return
        val extraAttributes = getExtraAttributes(item)
        if (Skytils.config.showMinionTier && extraAttributes != null && extraAttributes.hasKey("generator_tier")) {
            val s = extraAttributes.getInteger("generator_tier").toString()
            GlStateManager.disableLighting()
            GlStateManager.disableDepth()
            GlStateManager.disableBlend()
            event.fr.drawStringWithShadow(
                s,
                (event.x + 17 - event.fr.getStringWidth(s)).toFloat(),
                (event.y + 9).toFloat(),
                16777215
            )
            GlStateManager.enableLighting()
            GlStateManager.enableDepth()
        }
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        private var blockUnenchanted = false
    }
}