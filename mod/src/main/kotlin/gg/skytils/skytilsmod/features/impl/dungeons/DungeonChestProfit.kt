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

import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.screen.GuiContainerForegroundDrawnEvent
import gg.skytils.event.impl.screen.GuiContainerPreDrawSlotEvent
import gg.skytils.event.impl.screen.GuiContainerSlotClickEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.features.impl.handlers.AuctionData
import gg.skytils.skytilsmod.features.impl.misc.ItemFeatures
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiContainer
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.NumberUtil.romanToDecimal
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.SkyblockIsland
import com.mojang.blaze3d.systems.RenderSystem
import gg.essential.elementa.unstable.layoutdsl.LayoutScope
import gg.essential.elementa.unstable.layoutdsl.Modifier
import gg.essential.elementa.unstable.layoutdsl.color
import gg.essential.elementa.unstable.layoutdsl.column
import gg.essential.elementa.unstable.state.v2.State
import gg.essential.elementa.unstable.state.v2.combinators.or
import gg.essential.elementa.unstable.state.v2.mutableStateOf
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft
import gg.skytils.skytilsmod.core.Notifications
import gg.skytils.skytilsmod.core.structure.v2.HudElement
import gg.skytils.skytilsmod.gui.layout.text
import net.minecraft.client.font.TextRenderer
import net.minecraft.item.Items
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.item.ItemStack
import java.awt.Color
import java.util.TreeSet


/**
 * Based off of chest profit from code by Quantizr
 * Licensed under GNU GPL v3, with permission given from author
 * @author Quantizr
 */
object DungeonChestProfit : EventSubscriber {
    private val element = DungeonChestProfitHud.also { Skytils.guiManager.registerElement(it) }
    private var rerollBypass = false
    private val essenceRegex = Regex("§d(?<type>\\w+) Essence §8x(?<count>\\d+)")
    private val croesusChestRegex = Regex("^(Master Mode )?The Catacombs - Flo(or (IV|V?I{0,3}))?$")

    override fun setup() {
        register(::onGUIDrawnEvent)
        register(::onDrawSlot)
        register(::onSlotClick, EventPriority.Highest)
        register(::onWorldChange)
    }

    fun onGUIDrawnEvent(event: GuiContainerForegroundDrawnEvent) {
        if (!Skytils.config.dungeonChestProfit.getUntracked()) return
        if ((!Utils.inDungeons || DungeonTimer.scoreShownAt == null) && SBInfo.mode != SkyblockIsland.DungeonHub.mode) return
        val inv = (event.container as? GenericContainerScreenHandler ?: return).inventory

        if (event.chestName == "Croesus") {
            DungeonChest.entries.forEach(DungeonChest::reset)
            return
        }

        if (event.chestName.endsWith(" Chest")) {
            val chestType = DungeonChest.getFromName(event.chestName) ?: return
            val openChest = inv.getStack(31) ?: return
            if (openChest.displayNameStr == "§aOpen Reward Chest") {
                chestType.price = getChestPrice(ItemUtil.getItemLore(openChest))
                chestType.value = 0.0
                chestType.items.clear()
                for (i in 9..17) {
                    val lootSlot = inv.getStack(i) ?: continue
                    val identifier = AuctionData.getIdentifier(lootSlot)
                    val value = if (identifier != null) {
                        AuctionData.lowestBINs[identifier] ?: 0.0
                    } else {
                        getEssenceValue(lootSlot.displayNameStr) ?: continue
                    }

                    chestType.value += value
                    chestType.items.add(DungeonChestLootItem(lootSlot, value))
                }
            }
            val matrixStack = UMatrixStack.Compat.get()
            matrixStack.push()
            matrixStack.translate(
                (-(event.gui as AccessorGuiContainer).guiLeft).toDouble(),
                -(event.gui as AccessorGuiContainer).guiTop.toDouble(),
                299.0
            )
            matrixStack.runWithGlobalState {
                drawChestProfit(chestType)
            }
            matrixStack.pop()
        } else if (croesusChestRegex.matches(event.chestName)) {
            for (i in 10..16) {
                val openChest = inv.getStack(i) ?: continue
                val chestType = DungeonChest.getFromName(openChest.displayNameStr.stripControlCodes()) ?: continue
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

    fun onDrawSlot(event: GuiContainerPreDrawSlotEvent) {
        if (!Skytils.config.croesusChestHighlight) return
        if (SBInfo.mode != SkyblockIsland.DungeonHub.mode) return
        if (event.container !is GenericContainerScreenHandler || event.slot.inventory == mc.player?.inventory) return
        val stack = event.slot.stack ?: return
        if (stack.item == Items.PLAYER_HEAD) {
            val name = stack.displayNameStr
            if (!(name == "§cThe Catacombs" || name == "§cMaster Mode The Catacombs")) return
            val lore = ItemUtil.getItemLore(stack)
            event.slot highlight when {
                lore.any { line -> line == "§aNo more Chests to open!" } -> {
                    if (Skytils.config.croesusHideOpened) {
                        event.cancelled = true
                        return
                    } else Color(255, 0, 0, 100)
                }
                lore.any { line -> line == "§8No Chests Opened!" } -> Color(0, 255, 0, 100)
                lore.any { line -> line.startsWith("§8Opened Chest: ") } -> Color(255, 255, 0, 100)
                else -> return
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
            val unformatted = name.stripControlCodes().replace("Shiny ", "")
            ItemFeatures.itemIdToNameLookup.entries.find {
                it.value == unformatted && !it.key.contains("STARRED")
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
        if (chest.items.isNotEmpty()) {
            // TODO: probably can just directly call draw on component
//            val leftAlign = element.component.getLeft() < UResolution.scaledWidth / 2f
            // disable lighting
            var drawnLines = 1
            val profit = chest.profit

            val vertexConsumer = UMinecraft.getMinecraft().bufferBuilders.entityVertexConsumers
            val matrixStack = UMatrixStack.Compat.get()
            mc.textRenderer.draw(
                chest.displayText + "§f: §" + (if (profit > 0) "a" else "c") + NumberUtil.nf.format(
                    profit
                ),
                element.component.getLeft(),
                element.component.getTop(),
                chest.displayColor.rgb,
                false,
                matrixStack.peek().model,
                vertexConsumer,
                TextRenderer.TextLayerType.NORMAL,
                0, 15728880
            )

            for (item in chest.items) {
                val itemName = item.item.displayNameStr
                val line = itemName + "§f: §a" + NumberUtil.nf.format(item.value)
                mc.textRenderer.draw(
                    line,
                    element.component.getLeft(),
                    element.component.getTop() + drawnLines * mc.textRenderer.fontHeight,
                    0xFFFFFF,
                    false,
                    matrixStack.peek().model,
                    vertexConsumer,
                    TextRenderer.TextLayerType.NORMAL,
                    0, 15728880
                )
                drawnLines++
            }
        }
    }

    fun onWorldChange(event: WorldUnloadEvent) {
        DungeonChest.entries.forEach(DungeonChest::reset)
        rerollBypass = false
    }

    fun onSlotClick(event: GuiContainerSlotClickEvent) {
        if ((!Utils.inDungeons && SBInfo.mode != SkyblockIsland.DungeonHub.mode) || event.container !is GenericContainerScreenHandler) return
        if (Skytils.config.kismetRerollThreshold != 0 && !rerollBypass && event.slotId == 50 && event.chestName.endsWith(
                " Chest"
            )
        ) {
            val chestType = DungeonChest.getFromName(event.chestName) ?: return
            if (chestType.value >= Skytils.config.kismetRerollThreshold * 1_000_000) {
                event.cancelled = true
                Notifications
                    .push(
                        "Blocked Chest Reroll",
                        "The ${chestType.displayText} you are rerolling has ${chestType.profit}!\nClick me to disable this warning.",
                        4f,
                        action = {
                            rerollBypass = true
                        })
            }
        } else if (event.slotId in 9..17 && event.chestName.endsWith(" Chest") && DungeonChest.getFromName(event.chestName) != null) {
            event.cancelled = true
        }
    }

    private enum class DungeonChest(var displayText: String, var displayColor: Color) {
        WOOD("Wood Chest", Color(0x563100)),
        GOLD("Gold Chest", Color.YELLOW),
        DIAMOND("Diamond Chest", Color(0x00e9ff)),
        EMERALD("Emerald Chest", Color(0x49ff59)),
        OBSIDIAN("Obsidian Chest", Color.BLACK),
        BEDROCK("Bedrock Chest", Color(0xadadad));

        val notifier = mutableStateOf(false)

        var price = 0.0
            set(value) {
                notifier.set { !it }
                field = value
            }
        var value = 0.0
            set(value) {
                notifier.set { !it }
                field = value
            }
        var items = TreeSet<DungeonChestLootItem>().descendingSet()
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
                return entries.find {
                    it.displayText == name
                }
            }
        }
    }

    private data class DungeonChestLootItem(var item: ItemStack, var value: Double) : Comparable<DungeonChestLootItem> {
        override fun compareTo(other: DungeonChestLootItem): Int = value.compareTo(other.value)
    }

    object DungeonChestProfitHud : HudElement("Dungeon Chest Profit", 200f, 120f) {
        override val toggleState: State<Boolean>
            get() = Skytils.config.dungeonChestProfit

        override fun LayoutScope.render() {
            if_(SBInfo.dungeonsState or State { SBInfo.modeState() == SkyblockIsland.DungeonHub.mode }) {
                column {
                    DungeonChest.entries.forEach { chest ->
                        val text = State {
                            chest.notifier()
                            "${chest.displayText}§f: §${(if (chest.profit > 0) "a" else "c")}${NumberUtil.format(chest.profit.toLong())}"
                        }
                        text(text, Modifier.color(chest.displayColor))
                    }
                }
            }
        }

        override fun LayoutScope.demoRender() {
            column {
                DungeonChest.entries.forEach { chest ->
                    text("${chest.displayText}: §a+300M")
                }
            }
        }
    }
}
