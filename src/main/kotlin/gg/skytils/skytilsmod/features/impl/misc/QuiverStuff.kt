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
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.API
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.RenderUtil.renderItem
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import gg.skytils.skytilsmod.utils.nonDashedString
import gg.skytils.skytilsmod.utils.toMCItems
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.random.Random
import kotlin.Pair

object QuiverStuff {
    private val clearQuiverRegex = Regex("§r§aCleared your quiver!§r")
    private val littleArrowsLeft = Regex("§r§cYou only have (?<amount>\\d+) arrows left in your Quiver!§r")
    private val arrowRefillRegex = Regex("§r§aYou filled your quiver with §r§f(?<amount>\\d+) §r§aextra arrows!§r")
    private val jaxForgedRegex = Regex("§r§aJax forged §r§.(?<selected>[\\w ]+)§r§8 x(?<amount>\\d+)§r§a.*!§r")
    private val selectedArrowRegex = Regex("§r§aYou set your selected arrow type to §r§.(?<selected>[\\w ]+)§r§a!§r")

    private var newArrows = Array(45) { Pair("NONE", 0) }
    private var selectedType = ""
    //TODO: Add §c and such before the name
    private val arrowMap = mapOf(
        "Flint Arrow" to "ARROW",
        "Reinforced Iron Arrow" to "REINFORCED_IRON_ARROW",
        "Gold-tipped Arrow" to "GOLD_TIPPED_ARROW",
        "Redstone-tipped Arrow" to "REDSTONE_TIPPED_ARROW",
        "Emerald-tipped Arrow" to "EMERALD_TIPPED_ARROW",
        "Bouncy Arrow" to "BOUNCY_ARROW",
        "Icy Arrow" to "ICY_ARROW",
        "Armorshred Arrow" to "ARMORSHRED_ARROW",
        "Explosive Arrow" to "EXPLOSIVE_ARROW",
        "Glue Arrow" to "GLUE_ARROW",
        "Nansorb Arrow" to "NANSORB_ARROW",
        "None" to "NONE"
    )

    init {
        QuiverDisplay
        SelectedArrowDisplay
    }

    fun loadFromApi() {
        val data =
            API.getSelectedSkyblockProfileSync(UPlayer.getUUID())?.members?.get(UPlayer.getUUID().nonDashedString())
        selectedType = data?.itemData?.favoriteArrow?.ifBlank { "NONE" } ?: "NONE"
        data?.inventory?.bag?.quiver?.toMCItems()?.forEachIndexed { index, it ->
            val itemId = ItemUtil.getSkyBlockItemID(it)
            if (it == null || it.item != Items.arrow || itemId == null) {
                newArrows[index] = Pair("NONE", 0)
            } else {
                newArrows[index] = Pair(itemId, it.stackSize)
            }
        }
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        val oldGui = mc.currentScreen

        ((oldGui as? GuiChest)?.inventorySlots as? ContainerChest)?.let { chest ->
            if (chest.lowerChestInventory.name == "Quiver") {
                chest.inventorySlots.subList(0, 45).forEachIndexed { index, it ->
                    if (!it.hasStack || it.stack.item != Items.arrow) {
                        newArrows[index] = Pair("NONE", 0)
                    } else {
                        newArrows[index] = Pair(ItemUtil.getSkyBlockItemID(it.stack) ?: "NONE", it.stack.stackSize)
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte()) return
        val formatted = event.message.formattedText

        clearQuiverRegex.find(formatted)?.let {
            newArrows = Array(45) { Pair("NONE", 0) }
        }

        littleArrowsLeft.find(formatted)?.let {
            val amount = it.groups["amount"]?.value?.toIntOrNull() ?: return@let
            var totalAmountExcluding = 0
            if (selectedType == "NONE") {
                for (i in 0 until 45) {
                    val arrow = newArrows[i]
                    if (arrow.second == 0) continue
                    totalAmountExcluding += arrow.second
                    if (totalAmountExcluding > amount) {
                        newArrows[i] = Pair(arrow.first, arrow.second - (totalAmountExcluding - amount))
                        break
                    } else if (totalAmountExcluding == amount) {
                        newArrows[i] = Pair("NONE", 0)
                        break
                    } else {
                        newArrows[i] = Pair("NONE", 0)
                    }
                }
            } else {
                for (i in 0 until 45) {
                    val arrow = newArrows[i]
                    if (arrow.first != selectedType || arrow.second == 0) continue
                    totalAmountExcluding += arrow.second
                    if (totalAmountExcluding > amount) {
                        newArrows[i] = Pair(arrow.first, arrow.second - (totalAmountExcluding - amount))
                        break
                    } else if (totalAmountExcluding == amount) {
                        newArrows[i] = Pair("NONE", 0)
                        break
                    } else {
                        newArrows[i] = Pair("NONE", 0)
                    }
                }
            }
        }

        arrowRefillRegex.find(formatted)?.let {
            val amount = it.groups["amount"]?.value?.toIntOrNull() ?: return@let
            var remaining = amount
            for (i in 0 until 45) {
                val arrow = newArrows[i]
                if (!Utils.equalsOneOf(arrow.first, "NONE", "ARROW") || arrow.second == 64) continue
                val toAdd = 64 - arrow.second
                if (toAdd >= remaining) {
                    newArrows[i] = Pair("ARROW", arrow.second + remaining)
                    break
                } else {
                    newArrows[i] = Pair("ARROW", 64)
                    remaining -= toAdd
                }
            }
        }

        jaxForgedRegex.find(formatted)?.let {
            val selected = arrowMap[it.groups["selected"]?.value] ?: return@let
            val amount = it.groups["amount"]?.value?.toIntOrNull() ?: return@let
            var remaining = amount
            for (i in 0 until 45) {
                val arrow = newArrows[i]
                if (!Utils.equalsOneOf(arrow.first, "NONE", selected) || arrow.second == 64) continue
                val toAdd = 64 - arrow.second
                if (toAdd >= remaining) {
                    newArrows[i] = Pair(selected, arrow.second + remaining)
                    break
                } else {
                    newArrows[i] = Pair(selected, 64)
                    remaining -= toAdd
                }
            }
        }

        selectedArrowRegex.find(formatted)?.let {
            selectedType = arrowMap[it.groups["selected"]?.value] ?: return@let

            val selectedArrowCount =
                newArrows.filter { arrow -> arrow.first == selectedType || selectedType == "NONE" }.sumOf { arrow ->
                    arrow.second
                }

            if (Skytils.config.restockArrowsWarning != 0 && selectedArrowCount < Skytils.config.restockArrowsWarning) {
                GuiManager.createTitle(
                    "§cRESTOCK ${arrowMap.entries.find { type -> type.value == selectedType }?.key?.plus("s") ?: selectedType}",
                    60
                )
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onReceivePacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inSkyblock || event.packet !is S29PacketSoundEffect) return
        val packet = event.packet
        val sound = packet.soundName
        val volume = packet.volume
        val selectedArrowCount = newArrows.filter { arrow -> arrow.first == selectedType || selectedType == "NONE" }
            .sumOf { arrow -> arrow.second }

        if (sound == "random.bow" && volume == 1f && selectedArrowCount > 0 && mc.thePlayer.heldItem.item == Items.bow) {
            val extraAttr = ItemUtil.getExtraAttributes(mc.thePlayer.heldItem)
            if (extraAttr != null) {
                val level = when {
                    mc.thePlayer.isSneaking -> 0
                    else -> extraAttr.getCompoundTag("enchantments")?.getInteger("infinite_quiver")?.times(3) ?: 0
                }
                val randomNum = Random.nextInt(0, 100)
                if (level <= randomNum + 1) {
                    if (Skytils.config.restockArrowsWarning != 0 && selectedArrowCount == Skytils.config.restockArrowsWarning) {
                        GuiManager.createTitle(
                            "§cRESTOCK ${arrowMap.entries.find { it.value == selectedType }?.key?.plus("s") ?: selectedType}",
                            60
                        )
                    }

                    for (i in 0 until 45) {
                        val arrow = newArrows[i]
                        if (arrow.first == selectedType) {
                            if (arrow.second == 1) {
                                newArrows[i] = Pair("NONE", 0)
                                break
                            } else {
                                newArrows[i] = Pair(selectedType, arrow.second - 1)
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    object QuiverDisplay : GuiElement("Quiver Display", x = 0.05f, y = 0.4f) {
        private val arrowItem = ItemStack(Items.arrow, 1, 0)

        override fun render() {
            if (!toggled || !Utils.inSkyblock) return
            val arrowCount = newArrows.filter { arrow -> arrow.first == selectedType || selectedType == "NONE" }
                .sumOf { arrow -> arrow.second }

            val color = when {
                arrowCount < 400 -> CommonColors.RED
                arrowCount < 1200 -> CommonColors.YELLOW
                else -> CommonColors.GREEN
            }

            renderItem(arrowItem, 0, 0)
            ScreenRenderer.fontRenderer.drawString(
                arrowCount.toString(), 20f, 5f, color, SmartFontRenderer.TextAlignment.LEFT_RIGHT, textShadow
            )
        }

        override fun demoRender() {
            renderItem(arrowItem, 0, 0)
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
            val text =
                "Selected: §r${arrowMap.entries.find { it.value == selectedType && it.value != "NONE" }?.key?.plus("s") ?: if (selectedType == "NONE") "None" else selectedType}"

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
                "Selected: §rSkytils Arrow",
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
            get() = ScreenRenderer.fontRenderer.getStringWidth("Selected: Skytils Arrow")
        override val toggled: Boolean
            get() = Skytils.config.showSelectedArrowDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}
