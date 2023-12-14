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

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.features.impl.misc.Funny
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.multAlpha
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11

object ClickInOrderSolver {

    private val slotOrder = HashMap<Int, Int>()
    private var neededClick = 0
    private val menuSlots = (10..16) + (19..25)

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        neededClick = 0
        slotOrder.clear()
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!Utils.inDungeons || !Skytils.config.clickInOrderTerminalSolver || event.container !is ContainerChest) return
        val invSlots = event.container.inventorySlots
        if (event.chestName == "Click in order!") {
            for (i in menuSlots) {
                val itemStack = invSlots[i].stack ?: continue
                if (itemStack.item != Item.getItemFromBlock(Blocks.stained_glass_pane) || (itemStack.itemDamage != 14 && itemStack.itemDamage != 5)) continue
                if (itemStack.itemDamage == 5 && itemStack.stackSize > neededClick) {
                    neededClick = itemStack.stackSize
                }
                slotOrder[itemStack.stackSize - 1] = i
            }
        }
        if (slotOrder.size == 0) return
        val firstSlot = slotOrder[neededClick]
        val secondSlot = slotOrder[neededClick + 1]
        val thirdSlot = slotOrder[neededClick + 2]
        val lightingState = GL11.glIsEnabled(GL11.GL_LIGHTING)
        GlStateManager.disableLighting()
        GlStateManager.color(1f, 1f, 1f, 1f)
        if (firstSlot != null) {
            val slot = invSlots[firstSlot]
            if (slot != null) slot highlight Skytils.config.clickInOrderFirst.multAlpha(Funny.alphaMult)
        }
        if (secondSlot != null) {
            val slot = invSlots[secondSlot]
            if (slot != null) slot highlight Skytils.config.clickInOrderSecond.multAlpha(Funny.alphaMult)
        }
        if (thirdSlot != null) {
            val slot = invSlots[thirdSlot]
            if (slot != null) slot highlight Skytils.config.clickInOrderThird.multAlpha(Funny.alphaMult)
        }
        if (lightingState) GlStateManager.enableLighting()
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onDrawSlotLow(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.clickInOrderTerminalSolver) return
        if (event.container is ContainerChest) {
            val fr = mc.fontRendererObj
            val slot = event.slot
            if (event.chestName == "Click in order!") {
                if (slot.hasStack && slot.inventory !== mc.thePlayer?.inventory) {
                    val item = slot.stack
                    if (item.item === Item.getItemFromBlock(Blocks.stained_glass_pane) && item.itemDamage == 14) {
                        GlStateManager.disableLighting()
                        GlStateManager.disableDepth()
                        GlStateManager.disableBlend()
                        fr.drawStringWithShadow(
                            item.stackSize.toString(),
                            (slot.xDisplayPosition + 9 - fr.getStringWidth(item.stackSize.toString()) / 2).toFloat(),
                            (slot.yDisplayPosition + 4).toFloat(),
                            16777215
                        )
                        GlStateManager.enableLighting()
                        GlStateManager.enableDepth()
                        event.isCanceled = true
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: ItemTooltipEvent) {
        if (event.toolTip == null || !Utils.inDungeons || !Skytils.config.clickInOrderTerminalSolver) return
        val chest = mc.thePlayer.openContainer
        if (chest is ContainerChest) {
            val chestName = chest.lowerChestInventory.displayName.unformattedText
            if (chestName == "Click in order!") {
                event.toolTip.clear()
            }
        }
    }
}