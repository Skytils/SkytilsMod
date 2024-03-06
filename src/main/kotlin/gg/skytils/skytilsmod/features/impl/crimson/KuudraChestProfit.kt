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
package gg.skytils.skytilsmod.features.impl.crimson

import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.IO
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.features.impl.handlers.AuctionData
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiContainer
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import gg.skytils.skytilsmod.utils.graphics.colors.CustomColor
import kotlinx.coroutines.launch
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


/**
 * Modified version of [gg.skytils.skytilsmod.features.impl.dungeons.ChestProfit]
 */
object KuudraChestProfit {
    private val element = KuudraChestProfitElement()
    private val essenceRegex = Regex("§d(?<type>\\w+) Essence §8x(?<count>\\d+)")

    @SubscribeEvent
    fun onGUIDrawnEvent(event: GuiContainerEvent.ForegroundDrawnEvent) {
        if (!Skytils.config.kuudraChestProfit || !KuudraFeatures.kuudraOver || event.container !is ContainerChest || KuudraFeatures.myFaction == null) return
        val inv = event.container.lowerChestInventory

        if (event.chestName.endsWith(" Chest")) {
            val chestType = KuudraChest.getFromName(event.chestName) ?: return
            val openChest = inv.getStackInSlot(31) ?: return
            if (openChest.displayName == "§aOpen Reward Chest" && chestType.items.isEmpty()) {
                runCatching {
                    chestType.keyNeeded = getKeyNeeded(ItemUtil.getItemLore(openChest))
                    for (i in 9..17) {
                        val lootSlot = inv.getStackInSlot(i) ?: continue
                        chestType.addItem(lootSlot)
                    }
                }
            }
            GlStateManager.pushMatrix()
            GlStateManager.translate((-(event.gui as AccessorGuiContainer).guiLeft).toDouble(), -event.gui.guiTop.toDouble(), 299.0)
            drawChestProfit(chestType)
            GlStateManager.popMatrix()
        }
    }

    private fun getKeyNeeded(lore: List<String>): KuudraKey? {
        for (i in 0..<lore.size-1) {
            val line = lore[i]
            if (line == "§7Cost") {
                val cost = lore[i+1]
                if (cost == "§aThis Chest is Free!") return null
                return KuudraKey.entries.first { it.displayName == cost.stripControlCodes() }
            }
        }
        error("Could not find key needed for chest")
    }

    private fun getEssenceValue(text: String): Double? {
        if (!Skytils.config.kuudraChestProfitIncludesEssence) return null
        val groups = essenceRegex.matchEntire(text)?.groups ?: return null
        val type = groups["type"]?.value?.uppercase() ?: return null
        val count = groups["count"]?.value?.toInt() ?: return null
        return (AuctionData.lowestBINs["ESSENCE_$type"] ?: 0.0) * count
    }

    private fun drawChestProfit(chest: KuudraChest) {
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
                textShadow_
            )

            for (item in chest.items) {
                val line = item.item.displayName + "§f: §a" + NumberUtil.nf.format(item.value)
                ScreenRenderer.fontRenderer.drawString(
                    line,
                    if (leftAlign) element.scaleX else element.scaleX + element.width,
                    element.scaleY + drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT,
                    CommonColors.WHITE,
                    alignment,
                    textShadow_
                )
                drawnLines++
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        KuudraChest.entries.forEach(KuudraChest::reset)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (SBInfo.mode == SkyblockIsland.KuudraHollow.mode || event.container !is ContainerChest) return
        if (event.slotId in 9..17 && event.chestName.endsWith(" Chest") && KuudraChest.getFromName(event.chestName) != null) {
            event.isCanceled = true
        }
    }

    private enum class KuudraChest(var displayText: String, var displayColor: CustomColor) {
        FREE("Free Chest", CommonColors.RED),
        PAID("Paid Chest", CommonColors.GREEN);

        var keyNeeded: KuudraKey? = null
        var value = 0.0
        val items = ArrayList<KuudraChestLootItem>(3)
        val profit
            get() = value - (keyNeeded?.getPrice(KuudraFeatures.myFaction ?: error("Failed to get Crimson Faction")) ?: 0.0)

        fun reset() {
            keyNeeded = null
            value = 0.0
            items.clear()
        }

        fun addItem(item: ItemStack) {
            IO.launch {
                val identifier = AuctionData.getIdentifier(item)
                val extraAttr = ItemUtil.getExtraAttributes(item)

                val itemValue = if (identifier == null) {
                    getEssenceValue(item.displayName) ?: return@launch
                } else if (extraAttr?.hasKey("attributes") == true) {
                    KuudraFeatures.getAttributePricedItem(item)?.price ?: 0.0
                } else {
                    AuctionData.lowestBINs[identifier] ?: 0.0
                }
                items.add(KuudraChestLootItem(item, itemValue))

                value += itemValue
            }
        }

        companion object {
            fun getFromName(name: String?): KuudraChest? {
                if (name.isNullOrBlank()) return null
                return entries.find {
                    it.displayText == name
                }
            }
        }
    }

    private var textShadow_ = SmartFontRenderer.TextShadow.NORMAL
    private class KuudraChestLootItem(var item: ItemStack, var value: Double)
    class KuudraChestProfitElement : GuiElement("Kuudra Chest Profit", x = 200, y = 120) {
        override fun render() {
            if (toggled && SBInfo.mode == SkyblockIsland.KuudraHollow.mode) {
                val leftAlign = scaleX < sr.scaledWidth / 2f
                textShadow_ = textShadow
                GlStateManager.color(1f, 1f, 1f, 1f)
                GlStateManager.disableLighting()
                KuudraChest.entries.filter { it.items.isNotEmpty() }.forEachIndexed { i, chest ->
                    val profit = chest.profit
                    ScreenRenderer.fontRenderer.drawString(
                        "${chest.displayText}§f: §${(if (profit > 0) "a" else "c")}${NumberUtil.format(profit.toLong())}",
                        if (leftAlign) 0f else width.toFloat(),
                        (i * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                        chest.displayColor,
                        if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT,
                        textShadow
                    )
                }
            }
        }

        override fun demoRender() {
            RenderUtil.drawAllInList(this, KuudraChest.entries.map { "${it.displayText}: §a+300M" })
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * KuudraChest.entries.size
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Paid Chest: +300M")

        override val toggled: Boolean
            get() = Skytils.config.kuudraChestProfit

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    enum class KuudraKey(val displayName: String, val coinCost: Int, val materialCost: Int) {
        BASIC("Kuudra Key", 200000, 2),
        HOT("Hot Kuudra Key", 400000, 6),
        BURNING("Burning Kuudra Key", 750000, 20),
        FIERY("Fiery Kuudra Key", 1500000, 60),
        INFERNAL("Infernal Kuudra Key", 3000000, 120);

        companion object {
            // all keys cost 2 CORRUPTED_NETHER_STAR but nether stars are coop-soulbound
            const val starConstant = 2
        }

        // treat NPC discounts as negligible
        fun getPrice(faction: CrimsonFaction): Double {
            val keyMaterialCost = AuctionData.lowestBINs[faction.keyMaterial] ?: 0.0
            val starPrice = AuctionData.lowestBINs[""]

            return coinCost + keyMaterialCost * materialCost
        }
    }
}