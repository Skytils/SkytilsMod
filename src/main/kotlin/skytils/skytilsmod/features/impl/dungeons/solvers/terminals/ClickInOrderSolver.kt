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
package skytils.skytilsmod.features.impl.dungeons.solvers.terminals

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.opengl.GL11
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.impl.GuiContainerEvent
import skytils.skytilsmod.utils.RenderUtil.highlight
import skytils.skytilsmod.utils.Utils

object ClickInOrderSolver {

    private val slotOrder = HashMap<Int, Int>()
    private var neededClick = 0

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null || mc.theWorld == null) return
        if (!Skytils.config.clickInOrderTerminalSolver) return
        if (mc.currentScreen is GuiChest) {
            val chest = mc.thePlayer.openContainer as ContainerChest
            val invSlots = (mc.currentScreen as GuiChest).inventorySlots.inventorySlots
            val chestName = chest.lowerChestInventory.displayName.unformattedText.trim()
            if (chestName == "Click in order!") {
                for (i in 10..25) {
                    if (i == 17 || i == 18) continue
                    val itemStack = invSlots[i].stack ?: continue
                    if (itemStack.item !== Item.getItemFromBlock(Blocks.stained_glass_pane)) continue
                    if (itemStack.itemDamage != 14 && itemStack.itemDamage != 5) continue
                    if (itemStack.itemDamage == 5) {
                        if (itemStack.stackSize > neededClick) neededClick = itemStack.stackSize
                    }
                    slotOrder[itemStack.stackSize - 1] = i
                }
            }
        }
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        neededClick = 0
        slotOrder.clear()
    }

    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!Utils.inDungeons) return
        if (!Skytils.config.clickInOrderTerminalSolver || slotOrder.size == 0) return
        val invSlots = event.container.inventorySlots
        val firstSlot = slotOrder[neededClick]
        val secondSlot = slotOrder[neededClick + 1]
        val thirdSlot = slotOrder[neededClick + 2]
        val lightingState = GL11.glIsEnabled(GL11.GL_LIGHTING)
        GlStateManager.disableLighting()
        GlStateManager.color(1f, 1f, 1f, 1f)
        if (firstSlot != null) {
            val slot = invSlots[firstSlot]
            if (slot != null) slot highlight Skytils.config.clickInOrderFirst
        }
        if (secondSlot != null) {
            val slot = invSlots[secondSlot]
            if (slot != null) slot highlight Skytils.config.clickInOrderSecond
        }
        if (thirdSlot != null) {
            val slot = invSlots[thirdSlot]
            if (slot != null) slot highlight Skytils.config.clickInOrderThird
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
            val chest = event.container
            val chestName = chest.lowerChestInventory.displayName.unformattedText.trim()
            if (chestName == "Click in order!") {
                if (slot.hasStack && slot.inventory !== mc.thePlayer.inventory) {
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
        if (!Utils.inDungeons) return
        if (!Skytils.config.clickInOrderTerminalSolver) return
        if (event.toolTip == null) return
        val mc = Minecraft.getMinecraft()
        val player = mc.thePlayer
        if (mc.currentScreen is GuiChest) {
            val chest = player.openContainer as ContainerChest
            val inv = chest.lowerChestInventory
            val chestName = inv.displayName.unformattedText
            if (chestName == "Click in order!") {
                event.toolTip.clear()
            }
        }
    }
}