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

import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
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
import java.awt.Color
import kotlin.math.roundToInt

/**
 * Provides the functionality for an overlay that is rendered on top of chests, minions, ender chest pages,
 * and backpacks that shows the top items in the container by value and the container's total value.
 * @author FluxCapacitor2
 */
object ContainerSellValue {

    val element = SellValueDisplay()

    /**
     * Represents a line in the sell value display. To conserve space, multiple items can
     * be stacked into one line if their display names are equal, so all of the original
     * `ItemStack`s must be saved to find the lowest BIN price of this display line.
     */
    private class DisplayLine(itemStack: ItemStack) {
        val stackedItems = mutableListOf(itemStack)
        val lowestBIN: Double
            get() = stackedItems.sumOf(::getItemValue)
        val amount: Int
            get() = stackedItems.sumOf { it.stackSize }

        fun shouldDisplay(): Boolean = this.lowestBIN != 0.0
    }

    /**
     * A `GuiElement` that shows the most valuable items in a container. The rendering of this element
     * takes place when the container background is drawn, so it doesn't render at the normal time.
     * Even though the GuiElement's render method isn't used, it is still worth having an instance
     * of this class so that the user can move the element around normally.
     * @see onBackgroundDrawn
     */
    class SellValueDisplay : GuiElement("Container Sell Value", FloatPair(0.258f, 0.283f)) {

        internal val rightAlign: Boolean
            get() = element.actualX > (UResolution.scaledWidth * 0.75f) ||
                    (element.actualX < UResolution.scaledWidth / 2f && element.actualX > UResolution.scaledWidth / 4f)
        internal val textPosX: Float
            get() = if(rightAlign) actualWidth else 0f
        internal val alignment: SmartFontRenderer.TextAlignment
            get() = if(rightAlign) SmartFontRenderer.TextAlignment.RIGHT_LEFT
                    else SmartFontRenderer.TextAlignment.LEFT_RIGHT

        override fun render() {
            // Rendering is handled in the BackgroundDrawnEvent to give the text proper lighting
        }

        override fun demoRender() {
            listOf(
                "§cDctr's Space Helmet§8 - §a900M",
                "§fDirt §7x64 §8 - §a1k",
                "§eTotal Value: §a900M"
            ).forEachIndexed { i, str ->
                fr.drawString(
                    str, textPosX, (i * fr.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override val toggled: Boolean
            get() = Skytils.config.containerSellValue
        override val height: Int = fr.FONT_HEIGHT * 20
        override val width: Int = fr.getStringWidth("Dctr's Space Helmet - 900M")

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    private fun shouldRenderOverlay(chestName: String): Boolean {
        return (!inDungeons && (chestName == "Chest" || chestName == "Large Chest"))
                || chestName.contains("Backpack")
                || chestName.startsWith("Ender Chest (")
                || (chestName.contains("Minion") && SBInfo.mode == SkyblockIsland.PrivateIsland.mode)
    }

    private fun getItemValue(itemStack: ItemStack): Double {
        val identifier = AuctionData.getIdentifier(itemStack)
        val priceEach = AuctionData.lowestBINs[identifier] ?: ItemFeatures.sellPrices[identifier]

        return priceEach?.times(itemStack.stackSize) ?: 0.0
    }

    private val ItemStack.prettyDisplayName: String
        get() {
            val extraAttr = ItemUtil.getExtraAttributes(this) ?: return this.displayName
            if(ItemUtil.getSkyBlockItemID(extraAttr) == "ENCHANTED_BOOK" && extraAttr.hasKey("enchantments")) {
                val enchants = extraAttr.getCompoundTag("enchantments")
                if(enchants.keySet.size == 1) {
                    return ItemUtil.getItemLore(this).first()
                }
            }
            return this.displayName
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
        val isMinion = chestName.contains(" Minion ")

        if(!Skytils.config.containerSellValue || gui !is GuiChest || !shouldRenderOverlay(chestName)) return

        // Map all of the items in the chest to their lowest BIN prices
        val slots = container.inventorySlots.filter {
            it.hasStack && it.inventory != mc.thePlayer.inventory
                    && (!isMinion || it.slotNumber % 9 != 1) // Ignore minion upgrades and fuels
        }

        // Combine items with the same name to save space in the GUI
        val distinctItems = mutableMapOf<String, DisplayLine>()
        slots.forEach {
            if (distinctItems.containsKey(it.stack.prettyDisplayName)) {
                distinctItems[it.stack.prettyDisplayName]!!.stackedItems.add(it.stack)
            } else {
                distinctItems[it.stack.prettyDisplayName] = DisplayLine(it.stack)
            }
        }

        val totalContainerValue = distinctItems.entries.sumOf { it.value.lowestBIN }

        if(distinctItems.isEmpty() || totalContainerValue == 0.0) return

        // Sort the items from most to least valuable and convert them into a readable format
        val lines: Int
        val textLines = distinctItems.entries.asSequence()
            .sortedByDescending { (_, displayItem) -> displayItem.lowestBIN }
            .filter { it.value.shouldDisplay() }
            .map { (itemName, displayItem) ->
                "$itemName§r${
                    (" §7x${displayItem.amount}").toStringIfTrue(displayItem.amount > 1)
                }§8 - §a${NumberUtil.format(displayItem.lowestBIN.roundToInt())}"
            }
            .toList()
            .also { lines = it.size }
            .take(Skytils.config.containerSellValueMaxItems)

        UMatrixStack().apply {
            // Translate and scale manually because we're not rendering inside the GuiElement#render() method
            push()
            translate(element.actualX, element.actualY, 0f)
            scale(element.scale, element.scale, 0f)

            textLines.forEachIndexed { i, str -> drawLine(this, i, str) }
            if(lines > Skytils.config.containerSellValueMaxItems) {
                drawLine(this, textLines.size, "§7and ${lines - Skytils.config.containerSellValueMaxItems} more...")
                drawLine(this, textLines.size + 1, "§eTotal Value: §a${ NumberUtil.format(totalContainerValue) }")
            } else {
                drawLine(this, textLines.size, "§eTotal Value: §a${ NumberUtil.format(totalContainerValue) }")
            }

            pop()
        }.applyToGlobalState()
    }

    private fun drawLine(matrixStack: UMatrixStack, index: Int, str: String) {
        UGraphics.drawString(matrixStack, str,
            if(element.rightAlign) element.textPosX - UGraphics.getStringWidth(str) else element.textPosX,
            (index * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
            Color.WHITE.rgb, true)
    }
}