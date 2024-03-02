/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.universal.UResolution
import gg.essential.universal.wrappers.UPlayer
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.API
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.random.Random

object QuiverStuff {
    private val clearQuiverRegex = Regex("§r§aCleared your quiver!§r")

    // Currently this gets triggered incorrectly: §r§cYou don't have any more arrows left in your Quiver!§r
    private val emptyQuiverRegex = Regex("§r§cYour quiver is empty!§r")
    private val littleArrowsLeft = Regex("§r§cYou only have (?<amount>\\d+) arrows left in your Quiver!§r")
    private val arrowRefillRegex = Regex("§r§aYou filled your quiver with §r§f(?<amount>\\d+) §r§aextra arrows!§r")
    private val jaxForgedRegex = Regex("§r§aJax forged §r(?<type>§.[\\w -]+)§r§8 x(?<amount>\\d+)§r§a.*!§r")
    private val selectedArrowRegex = Regex("§r§aYou set your selected arrow type to §r(?<selected>§.[\\w -]+)§r§a!§r")
    private val resetPreferenceRegex = Regex("§r§cYour favorite arrow has been reset!§r")
    private val arrowMap = mapOf(
        "§fFlint Arrow" to "ARROW",
        "§fReinforced Iron Arrow" to "REINFORCED_IRON_ARROW",
        "§fGold-tipped Arrow" to "GOLD_TIPPED_ARROW",
        "§aRedstone-tipped Arrow" to "REDSTONE_TIPPED_ARROW",
        "§aEmerald-tipped Arrow" to "EMERALD_TIPPED_ARROW",
        "§9Bouncy Arrow" to "BOUNCY_ARROW",
        "§9Icy Arrow" to "ICY_ARROW",
        "§9Armorshred Arrow" to "ARMORSHRED_ARROW",
        "§9Explosive Arrow" to "EXPLOSIVE_ARROW",
        "§9Glue Arrow" to "GLUE_ARROW",
        "§9Nansorb Arrow" to "NANSORB_ARROW",
        "§cNone" to "NONE",
        "§cUnknown" to "UNKNOWN"
    )

    private var selectedType = "UNKNOWN"
    private var quiver = mutableListOf<Pair<String, Int>?>()
    private var arrowCount: Int
        get() = getArrows(if (selectedType == "NONE") "ARROW" else selectedType)
        set(value) {
            setArrows(value, if (selectedType == "NONE") "ARROW" else selectedType)
        }

    init {
        QuiverDisplay
        SelectedArrowDisplay
    }

    fun loadFromApi() {
        val data =
            API.getSelectedSkyblockProfileSync(UPlayer.getUUID())?.members?.get(UPlayer.getUUID().nonDashedString())
                ?: return

        selectedType = data.item_data.favorite_arrow.ifBlank { "NONE" }
        updateArrows(data.inventory.bag.quiver.toMCItems())
    }

    private fun updateArrows(itemStacks: List<ItemStack?>) {
        quiver = itemStacks.filter { it?.item != Item.getItemFromBlock(Blocks.stained_glass_pane) }.map {
            if (it == null || it.item != Items.arrow) return@map null
            val itemId = ItemUtil.getSkyBlockItemID(it) ?: return@map null

            if (arrowMap.entries.find { entry -> entry.value == itemId } != null) {
                Pair(itemId, it.stackSize)
            } else Pair("UNKNOWN", it.stackSize)
        }.toMutableList()
    }

    private fun getArrows(type: String): Int {
        val currentType = arrowMap.entries.find { entry -> entry.value == type }?.value ?: "UNKNOWN"
        return quiver.filterNotNull().map {
            if (currentType == it.first) {
                it.second
            } else 0
        }.fold(0) { acc, i -> acc + i }
    }

    private fun setArrows(amount: Int, type: String) {
        val currentType = arrowMap.entries.find { entry -> entry.value == type }?.value ?: "UNKNOWN"
        var amountToRemove = arrowCount - amount
        if (amountToRemove == 0) return

        if (amountToRemove > 0) {
            for (i in quiver.indices) {
                val pair = quiver[i] ?: continue
                if (pair.first != currentType) continue

                if (pair.second > amountToRemove) {
                    quiver[i] = Pair(pair.first, pair.second - amountToRemove)
                    amountToRemove = 0
                    break
                } else {
                    amountToRemove -= pair.second
                    quiver[i] = null
                }
            }
        }

        var amountToAdd = -amountToRemove

        if (amountToAdd > 0) {
            for (i in quiver.indices) {
                val pair = quiver[i] ?: continue
                if (pair.first != currentType) continue

                if (pair.second + amountToAdd <= 64) {
                    quiver[i] = Pair(pair.first, pair.second + amountToAdd)
                    amountToAdd = 0
                    break
                } else {
                    amountToAdd -= 64 - pair.second
                    quiver[i] = Pair(pair.first, 64)
                }
            }
        }

        if (amountToAdd > 0) {
            for (i in quiver.indices) {
                if (quiver[i] != null) continue
                if (amountToAdd <= 64) {
                    quiver[i] = Pair(currentType, amountToAdd)
                    amountToAdd = 0
                    break
                } else {
                    amountToAdd -= 64
                    quiver[i] = Pair(currentType, 64)
                }
            }
        }
    }

    private fun sendWarning() {
        val text = if (selectedType == "NONE" || selectedType == "UNKNOWN") {
            "§fArrows"
        } else {
            arrowMap.entries.find { type -> type.value == selectedType }?.key ?: selectedType
        }
        GuiManager.createTitle("§cRESTOCK $text", 60)
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        val oldGui = Skytils.mc.currentScreen

        ((oldGui as? GuiChest)?.inventorySlots as? ContainerChest)?.let { chest ->
            val chestName = chest.lowerChestInventory.name
            val itemStacks = chest.inventorySlots.map { it.stack }

            if (chestName == "Quiver") {
                updateArrows(itemStacks.subList(0, 45))
            } else if (chestName == "Forge Arrows") {
                itemStacks.find { ItemUtil.getSkyBlockItemID(it) == "ARROW_SWAPPER" }?.let {
                    val line = ItemUtil.getItemLore(it).find { lore -> lore.startsWith("§aSelected: ") }
                    selectedType = arrowMap[line?.substringAfter("§aSelected: ")] ?: "UNKNOWN"
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChatReceived(event: ClientChatReceivedEvent) {
        if (event.type == 2.toByte() || !Utils.inSkyblock) return
        val formatted = event.message.formattedText

        if (clearQuiverRegex.matches(formatted)) {
            quiver.clear()
        }

        if (emptyQuiverRegex.matches(formatted)) {
            if (quiver.isNotEmpty()) {
                quiver.clear()
            }
        }

        littleArrowsLeft.find(formatted)?.let { result ->
            val amount = result.groups["amount"]?.value?.toIntOrNull() ?: return@let

            arrowCount = amount
        }

        arrowRefillRegex.find(formatted)?.let {
            val amount = it.groups["amount"]?.value?.toIntOrNull() ?: return@let

            setArrows(getArrows("ARROW") + amount, "ARROW")
        }

        jaxForgedRegex.find(formatted)?.let {
            val forged = arrowMap[it.groups["type"]?.value] ?: "UNKNOWN"
            val amount = it.groups["amount"]?.value?.toIntOrNull() ?: return@let

            setArrows(getArrows(forged) + amount, forged)
        }

        selectedArrowRegex.find(formatted)?.let { result ->
            val selected = result.groups["selected"]?.value
            selectedType = arrowMap.entries.find { it.key.substring(2) == selected?.substring(2) }?.value ?: "UNKNOWN"

            if (Skytils.config.restockArrowsWarning != 0 && arrowCount < Skytils.config.restockArrowsWarning) {
                sendWarning()
            }
        }

        if (resetPreferenceRegex.matches(formatted)) {
            selectedType = "NONE"
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onReceivePacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inSkyblock) return
        if (event.packet is S29PacketSoundEffect) {
            val packet = event.packet
            val sound = packet.soundName
            val volume = packet.volume

            if (sound == "random.bow" && volume == 1f && arrowCount > 0 && Skytils.mc.thePlayer.heldItem.item == Items.bow) {
                val armor = (3 downTo 0).mapNotNull { Skytils.mc.thePlayer.getCurrentArmor(it) }
                val extraAttr = ItemUtil.getExtraAttributes(Skytils.mc.thePlayer.heldItem)
                if (!armor.any { it.displayName.contains("Skeleton Master") } && extraAttr != null) {
                    val level = when {
                        Skytils.mc.thePlayer.isSneaking -> 0
                        else -> extraAttr.getCompoundTag("enchantments")?.getInteger("infinite_quiver")?.times(3) ?: 0
                    }
                    val randomNum = Random.nextInt(0, 100)

                    if (level <= randomNum + 1) {
                        if (Skytils.config.restockArrowsWarning != 0 && arrowCount == Skytils.config.restockArrowsWarning) {
                            sendWarning()
                        }

                        arrowCount--
                    }
                }
            }
        }
    }

    object QuiverDisplay : GuiElement("Quiver Display", x = 0.05f, y = 0.4f) {
        private val arrowItem = ItemStack(Items.arrow, 1, 0)

        override fun render() {
            if (!toggled || !Utils.inSkyblock) return
            val temp = arrowCount

            val color = when {
                temp < 400 -> CommonColors.RED
                temp < 1200 -> CommonColors.YELLOW
                else -> CommonColors.GREEN
            }

            RenderUtil.renderItem(arrowItem, 0, 0)
            ScreenRenderer.fontRenderer.drawString(
                temp.toString(), 20f, 5f, color, SmartFontRenderer.TextAlignment.LEFT_RIGHT, textShadow
            )
        }

        override fun demoRender() {
            RenderUtil.renderItem(arrowItem, 0, 0)
            ScreenRenderer.fontRenderer.drawString(
                "2000", 20f, 5f, CommonColors.GREEN, SmartFontRenderer.TextAlignment.LEFT_RIGHT, textShadow
            )
        }

        override val height: Int
            get() = 16
        override val width: Int
            get() = 20 + ScreenRenderer.fontRenderer.getStringWidth("2000")

        override val toggled: Boolean
            get() = Skytils.config.quiverDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    object SelectedArrowDisplay : GuiElement("Arrow Swapper Display", x = 0.65f, y = 0.85f) {
        override fun render() {
            if (!toggled || !Utils.inSkyblock) return
            val alignment =
                if (scaleX < UResolution.scaledWidth / 2f) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            val selected = arrowMap.entries.find { it.value == selectedType }?.key ?: "§cUnknown"
            val text = "Selected: §r$selected"

            ScreenRenderer.fontRenderer.drawString(
                text,
                if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                0f,
                CommonColors.GREEN,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )

        }

        override fun demoRender() {
            val alignment =
                if (scaleX < UResolution.scaledWidth / 2f) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Selected: Redstone-tipped Arrow",
                if (scaleX < UResolution.scaledWidth / 2f) 0f else width.toFloat(),
                0f,
                CommonColors.GREEN,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Selected: Redstone-tipped Arrow")
        override val toggled: Boolean
            get() = Skytils.config.showSelectedArrowDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}