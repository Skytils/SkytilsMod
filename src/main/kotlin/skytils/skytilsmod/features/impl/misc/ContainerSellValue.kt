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

package skytils.skytilsmod.features.impl.misc

import gg.essential.universal.UResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.Utils.inDungeons
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import kotlin.math.roundToInt

class ContainerSellValue {

    private data class DisplayItem(var amount: Int, val itemStack: ItemStack) {
        val lowestBIN: Double?
            get() = AuctionData.lowestBINs[AuctionData.getIdentifier(itemStack)]?.times(amount) ?:
            ItemFeatures.sellPrices[AuctionData.getIdentifier(itemStack)]?.times(amount)
        val displayName: String = itemStack.displayName

        fun shouldDisplay(): Boolean = this.lowestBIN != null

        constructor(slot: Slot) : this(slot.stack.stackSize, slot.stack)
    }

    class SellValueDisplay : GuiElement("Container Sell Value", FloatPair(0.258f, 0.283f)) {

        override fun render() {
            // Rendering is handled in the BackgroundDrawnEvent to give the text proper lighting
        }

        override fun demoRender() {
            val rightAlign = actualX < UResolution.scaledWidth / 2f
            val alignment = if(rightAlign) SmartFontRenderer.TextAlignment.RIGHT_LEFT
            else SmartFontRenderer.TextAlignment.LEFT_RIGHT
            val xPos = if(rightAlign) actualWidth else 0f

            listOf(
                "§cDctr's Space Helmet§8 - §a900M",
                "§fDirt §7x64 §8 - §a1k",
                "§eTotal Value: §a900M"
            ).forEachIndexed { i, str ->
                fr.drawString(
                    str, xPos, (i * fr.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override val toggled: Boolean = Skytils.config.containerSellValue
        override val height: Int = fr.FONT_HEIGHT * 20
        override val width: Int = fr.getStringWidth("Dctr's Space Helmet - 900M")

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    companion object {
        val element = SellValueDisplay()
        // The max amount of BIN prices to show in the list. If there are more items, the list will be truncated to this size.
        const val MAX_DISPLAYED_ITEMS = 20

        /**
         * Check if the overlay should be rendered in a container based on its name.
         * Island chests, backpacks, ender chest pages, and minions are included.
         */
        private fun isValidContainer(chestName: String): Boolean {
            return (!inDungeons && (chestName == "Chest" || chestName == "Large Chest"))
                    || chestName.contains("Backpack")
                    || chestName.startsWith("Ender Chest (")
                    || (chestName.contains("Minion") && SBInfo.mode == SkyblockIsland.PrivateIsland.mode)
        }
    }

    /**
     * Renders the sell value overlay when in a valid GUI.
     *
     * Note: It is important that Minecraft Forge's `BackgroundDrawnEvent` is used instead of Skytils's
     * GuiContainerEvent.BackgroundDrawnEvent because Skytils's event is called before the overlay rectangle is drawn,
     * causing the text to be dimmed.
     */
    @SubscribeEvent
    fun onBackgroundDrawn(event: GuiScreenEvent.BackgroundDrawnEvent) {
        val gui = mc.currentScreen
        val container = if(mc.currentScreen is GuiChest) mc.thePlayer.openContainer as ContainerChest else return
        val chestName = container.lowerChestInventory.name

        if(!Skytils.config.containerSellValue || gui !is GuiChest || !isValidContainer(chestName)) return

        val rightAlign = element.actualX < UResolution.scaledWidth / 2f
        val alignment = if(rightAlign) SmartFontRenderer.TextAlignment.RIGHT_LEFT
        else SmartFontRenderer.TextAlignment.LEFT_RIGHT
        val xPos = if(rightAlign) element.actualWidth else 0f
        val isMinion = chestName.contains("Minion")

        // Map all of the items in the chest to their lowest BIN prices
        val items = container.inventorySlots.filter {
            it.hasStack && it.inventory != mc.thePlayer.inventory
                    && (!isMinion || it.slotNumber % 9 != 1) // Ignore minion upgrades and fuels
        }.map { DisplayItem(it) }.filter { it.shouldDisplay() }

        // Combine items with the same ID to save space in the GUI
        val distinctItems = mutableMapOf<String, DisplayItem>()
        items.forEach {
            if(distinctItems.containsKey(it.displayName)) {
                distinctItems[it.displayName]!!.apply { amount += it.amount }
            } else {
                distinctItems[it.displayName] = it
            }
        }

        if(items.isEmpty()) return

        // Sort the items from most to least valuable and convert them into a readable format
        val textLines = distinctItems.entries
            .sortedByDescending { (_, displayItem) -> displayItem.lowestBIN }
            .map { (itemName, displayItem) ->
            "${itemName}${
                (" §7x${displayItem.amount}").toStringIfTrue(displayItem.amount > 1)
            }§8 - §a${NumberUtil.format(displayItem.lowestBIN!!.roundToInt())}"
        }.take(MAX_DISPLAYED_ITEMS).toMutableList()

        if(distinctItems.size > MAX_DISPLAYED_ITEMS) {
            textLines.add("§7and ${distinctItems.size - MAX_DISPLAYED_ITEMS} more...")
        }
        textLines.add("§eTotal Value: §a${
            NumberUtil.format(distinctItems.entries.sumOf { it.value.lowestBIN ?: 0.0 })
        }")

        // Translate and scale manually because we're not rendering inside the GuiElement#render() method
        GlStateManager.pushMatrix()
        GlStateManager.translate(element.actualX, element.actualY, 0f)
        GlStateManager.scale(element.scale, element.scale, 0f)

        textLines.forEachIndexed { i, str ->
            ScreenRenderer.fontRenderer.drawString(
                str,
                xPos, (i * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL
            )
        }

        GlStateManager.popMatrix()
    }
}