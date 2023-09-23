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
package gg.skytils.skytilsmod.features.impl.dungeons

import gg.essential.api.EssentialAPI
import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.features.impl.handlers.AuctionData
import gg.skytils.skytilsmod.features.impl.misc.ItemFeatures
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.NumberUtil.romanToDecimal
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import gg.skytils.skytilsmod.utils.graphics.colors.CustomColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


/**
 * Based off of chest profit from code by Quantizr
 * Licensed under GNU GPL v3, with permission given from author
 * @author Quantizr
 */
object ChestProfit {
    private val element = DungeonChestProfitElement()
    private var rerollBypass = false
    private val essenceRegex = Regex("§d(?<type>\\w+) Essence §8x(?<count>\\d+)")
    private val croesusChestRegex = Regex("^(Master Mode|The)? Catacombs - Floor (IV|V?I{0,3})$")

    @SubscribeEvent
    fun onGUIDrawnEvent(event: GuiContainerEvent.ForegroundDrawnEvent) {
        if (!Skytils.config.dungeonChestProfit) return
        if ((!Utils.inDungeons || DungeonTimer.scoreShownAt == -1L) && SBInfo.mode != SkyblockIsland.DungeonHub.mode) return
        if (event.container !is ContainerChest) return
        val inv = event.container.lowerChestInventory

        if (event.chestName == "Croesus") {
            DungeonChest.values().forEach(DungeonChest::reset)
            return
        }

        if (event.chestName.endsWith(" Chest")) {
            val chestType = DungeonChest.getFromName(event.chestName) ?: return
            val openChest = inv.getStackInSlot(31) ?: return
            if (openChest.displayName == "§aOpen Reward Chest") {
                chestType.price = getChestPrice(ItemUtil.getItemLore(openChest))
                chestType.value = 0.0
                chestType.items.clear()
                for (i in 9..17) {
                    val lootSlot = inv.getStackInSlot(i) ?: continue
                    val identifier = AuctionData.getIdentifier(lootSlot)
                    val value = if (identifier != null) {
                        AuctionData.lowestBINs[identifier] ?: 0.0
                    } else {
                        getEssenceValue(lootSlot.displayName) ?: continue
                    }

                    chestType.value += value
                    chestType.items.add(DungeonChestLootItem(lootSlot, value))
                }
            }
            drawChestProfit(chestType)
        } else if (croesusChestRegex.matches(event.chestName)) {
            for (i in 10..16) {
                val openChest = inv.getStackInSlot(i) ?: continue
                val chestType = DungeonChest.getFromName(openChest.displayName.stripControlCodes()) ?: continue
                val lore = ItemUtil.getItemLore(openChest)

                val contentIndex = lore.indexOf("§7Contents")
                if (contentIndex == -1) continue

                chestType.price = getChestPrice(lore)
                chestType.value = 0.0
                chestType.items.clear()

                lore.drop(contentIndex + 1).takeWhile { it != "" }.forEach { drop ->
                    val value = if (drop.contains("Essence")) {
                        getEssenceValue(drop) ?: return@forEach
                    } else {
                        AuctionData.lowestBINs[(getIdFromName(drop))] ?: 0.0
                    }
                    chestType.value += value
                }

                chestType.items.add(DungeonChestLootItem(openChest, chestType.value))
            }
        }
    }

    private fun getChestPrice(lore: List<String>): Double {
        lore.forEach {
            val line = it.stripControlCodes()
            if (line.contains("FREE")) {
                return 0.0
            }
            if (line.contains(" Coins")) {
                return line.substring(0, line.indexOf(" ")).replace(",", "").toDouble()
            }
        }
        return 0.0
    }

    private fun getEssenceValue(text: String): Double? {
        if (!Skytils.config.dungeonChestProfitIncludesEssence) return null
        val groups = essenceRegex.matchEntire(text)?.groups ?: return null
        val type = groups["type"]?.value?.uppercase() ?: return null
        val count = groups["count"]?.value?.toInt() ?: return null
        return (AuctionData.lowestBINs["ESSENCE_$type"] ?: 0.0) * count
    }

    private fun getIdFromName(name: String): String? {
        return if (name.startsWith("§aEnchanted Book (")) {
            val enchant = name.substring(name.indexOf("(") + 1, name.indexOf(")"))
            return enchantNameToID(enchant)
        } else {
            val unformatted = name.stripControlCodes()
            ItemFeatures.itemIdToNameLookup.entries.find {
                it.value == unformatted && !it.value.contains("STARRED")
            }?.key
        }
    }

    private fun enchantNameToID(enchant: String): String {
        val enchantName = enchant.substringBeforeLast(" ")
        val enchantId = if (enchantName.startsWith("§d§l")) {
            val name = enchantName.stripControlCodes().uppercase().replace(" ", "_")
            if (!name.contains("ULTIMATE_")) {
                "ULTIMATE_$name"
            } else name
        } else {
            enchantName.stripControlCodes().uppercase().replace(" ", "_")
        }
        val level = enchant.substringAfterLast(" ").stripControlCodes().let {
            it.toIntOrNull() ?: it.romanToDecimal()
        }
        return "ENCHANTED_BOOK-$enchantId-$level"
    }

    private fun drawChestProfit(chest: DungeonChest) {
        if (chest.items.size > 0) {
            val leftAlign = element.scaleX < UResolution.scaledWidth / 2f
            val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
            GlStateManager.color(1f, 1f, 1f, 1f)
            GlStateManager.disableLighting()
            var drawnLines = 1
            val profit = chest.profit

            ScreenRenderer.fontRenderer.drawString(
                chest.displayText + "§f: §" + (if (profit > 0) "a" else "c") + NumberUtil.nf.format(
                    profit
                ),
                if (leftAlign) element.scaleX else element.scaleX + element.width,
                element.scaleY,
                chest.displayColor,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )

            for (item in chest.items) {
                val line = item.item.displayName + "§f: §a" + NumberUtil.nf.format(item.value)
                ScreenRenderer.fontRenderer.drawString(
                    line,
                    if (leftAlign) element.scaleX else element.scaleX + element.width,
                    element.scaleY + drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT,
                    CommonColors.WHITE,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
                drawnLines++
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        DungeonChest.values().forEach(DungeonChest::reset)
        rerollBypass = false
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!Utils.inDungeons || event.container !is ContainerChest) return
        if (Skytils.config.kismetRerollThreshold != 0 && !rerollBypass && event.slotId == 50 && event.chestName.endsWith(
                " Chest"
            )
        ) {
            val chestType = DungeonChest.getFromName(event.chestName) ?: return
            if (chestType.value >= Skytils.config.kismetRerollThreshold * 1_000_000) {
                event.isCanceled = true
                EssentialAPI.getNotifications()
                    .push(
                        "Blocked Chest Reroll",
                        "The ${chestType.displayText} you are rerolling has ${chestType.profit}!\nClick me to disable this warning.",
                        4f,
                        action = {
                            rerollBypass = true
                        })
            }
        } else if (event.slotId in 9..17 && event.chestName.endsWith(" Chest") && DungeonChest.getFromName(event.chestName) != null) {
            event.isCanceled = true
        }
    }

    private enum class DungeonChest(var displayText: String, var displayColor: CustomColor) {
        WOOD("Wood Chest", CommonColors.BROWN),
        GOLD("Gold Chest", CommonColors.YELLOW),
        DIAMOND("Diamond Chest", CommonColors.LIGHT_BLUE),
        EMERALD("Emerald Chest", CommonColors.LIGHT_GREEN),
        OBSIDIAN("Obsidian Chest", CommonColors.BLACK),
        BEDROCK("Bedrock Chest", CommonColors.LIGHT_GRAY);

        var price = 0.0
        var value = 0.0
        var items = ArrayList<DungeonChestLootItem>(3)
        val profit
            get() = value - price

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
    class DungeonChestProfitElement : GuiElement("Dungeon Chest Profit", x = 200, y = 120) {
        override fun render() {
            if (toggled && (Utils.inDungeons || SBInfo.mode == SkyblockIsland.DungeonHub.mode)) {
                val leftAlign = scaleX < sr.scaledWidth / 2f
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
}