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
package skytils.skytilsmod.features.impl.dungeons

import gg.essential.universal.UResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.impl.GuiContainerEvent
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import skytils.skytilsmod.utils.graphics.colors.CustomColor


/**
 * Based off of chest profit from code by Quantizr
 * Licensed under GNU GPL v3, with permission given from author
 * @author Quantizr
 */
class ChestProfit {
    @SubscribeEvent
    fun onGUIDrawnEvent(event: GuiContainerEvent.ForegroundDrawnEvent) {
        if (!Utils.inDungeons || DungeonTimer.scoreShownAt == -1L) return
        if (!Skytils.config.dungeonChestProfit) return
        if (event.container is ContainerChest) {
            val inv = event.container.lowerChestInventory
            val name = inv.displayName.unformattedText
            if (name.endsWith(" Chest")) {
                val chestType = DungeonChest.getFromName(name) ?: return
                val openChest = inv.getStackInSlot(31) ?: return
                if (openChest.displayName == "§aOpen Reward Chest") {
                    for (unclean in ItemUtil.getItemLore(openChest)) {
                        val line = unclean.stripControlCodes()
                        if (line.contains("FREE")) {
                            chestType.price = 0.0
                            break
                        } else if (line.contains(" Coins")) {
                            chestType.price =
                                line.substring(0, line.indexOf(" ")).replace(",".toRegex(), "").toDouble()
                            break
                        }
                    }
                    chestType.value = 0.0
                    chestType.items.clear()
                    for (i in 9..17) {
                        val lootSlot = inv.getStackInSlot(i)
                        val identifier = AuctionData.getIdentifier(lootSlot)
                        if (identifier != null) {
                            var value = AuctionData.lowestBINs[identifier]
                            if (value == null) value = 0.0
                            chestType.value += value
                            chestType.items.add(DungeonChestLootItem(lootSlot, value))
                        }
                    }
                }
                if (chestType.items.size > 0) {
                    val sr = UResolution
                    val leftAlign = element.actualX < sr.scaledWidth / 2f
                    val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                    GlStateManager.color(1f, 1f, 1f, 1f)
                    GlStateManager.disableLighting()
                    var drawnLines = 1
                    val profit = chestType.value - chestType.price
                    ScreenRenderer.fontRenderer.drawString(
                        chestType.displayText + "§f: §" + (if (profit > 0) "a" else "c") + NumberUtil.nf.format(
                            profit
                        ),
                        if (leftAlign) element.actualX else element.actualX + element.width,
                        element.actualY,
                        chestType.displayColor,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                    for (item in chestType.items) {
                        val line = item.item.displayName + "§f: §a" + NumberUtil.nf.format(item.value)
                        ScreenRenderer.fontRenderer.drawString(
                            line,
                            if (leftAlign) element.actualX else element.actualX + element.width,
                            element.actualY + drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT,
                            CommonColors.WHITE,
                            alignment,
                            SmartFontRenderer.TextShadow.NORMAL
                        )
                        drawnLines++
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        for (chest in DungeonChest.values()) {
            chest.reset()
        }
    }

    private enum class DungeonChest(var displayText: String, var displayColor: CustomColor) {
        WOOD("Wood Chest", CommonColors.BROWN), GOLD("Gold Chest", CommonColors.YELLOW), DIAMOND(
            "Diamond Chest",
            CommonColors.LIGHT_BLUE
        ),
        EMERALD("Emerald Chest", CommonColors.LIGHT_GREEN), OBSIDIAN(
            "Obsidian Chest",
            CommonColors.BLACK
        ),
        BEDROCK("Bedrock Chest", CommonColors.LIGHT_GRAY);

        var price = 0.0
        var value = 0.0
        var items = ArrayList<DungeonChestLootItem>()
        fun reset() {
            price = 0.0
            value = 0.0
            items.clear()
        }

        companion object {
            fun getFromName(name: String?): DungeonChest? {
                if (name.isNullOrBlank()) return null
                return values().find {
                    it.displayText == name
                }
            }
        }
    }

    private class DungeonChestLootItem(var item: ItemStack, var value: Double)
    class DungeonChestProfitElement : GuiElement("Dungeon Chest Profit", FloatPair(200, 120)) {
        override fun render() {
            if (toggled && Utils.inDungeons) {
                val leftAlign = actualX < sr.scaledWidth / 2f
                GlStateManager.color(1f, 1f, 1f, 1f)
                GlStateManager.disableLighting()
                DungeonChest.values().filter { it.items.isNotEmpty() }.forEachIndexed { i, chest ->
                    val profit = chest.value - chest.price
                    ScreenRenderer.fontRenderer.drawString(
                        "${chest.displayText}§f: §${(if (profit > 0) "a" else "c")}${NumberUtil.format(profit.toLong())}",
                        if (leftAlign) 0f else width.toFloat(),
                        (i * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                        chest.displayColor,
                        if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                }
            }
        }

        override fun demoRender() {
            RenderUtil.drawAllInList(this, DungeonChest.values().map { "${it.displayText}: §a+300M" })
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * DungeonChest.values().size
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Obsidian Chest: 300M")

        override val toggled: Boolean
            get() = Skytils.config.dungeonChestProfit

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    companion object {
        private val element = DungeonChestProfitElement()
    }
}