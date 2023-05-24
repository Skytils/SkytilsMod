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

package gg.skytils.skytilsmod.features.impl.misc

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.utils.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.io.Reader
import java.io.Writer
import java.util.*

object PricePaid : PersistentSave(File(Skytils.modDir, "pricepaid.json")) {
    val prices = mutableMapOf<@Contextual UUID, Double>()
    private val coinRegex = Regex("(?:§6)?([\\d,]+) coins", RegexOption.IGNORE_CASE)
    private var lastBought: Triple<String, UUID, Double>? = null
    private val junkRegex = Regex("[,.]")

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onPacket(event: PacketEvent.ReceiveEvent) {
        if (!Utils.inSkyblock || lastBought == null || event.packet !is S02PacketChat) return
        val formatted = event.packet.chatComponent.formattedText
        val unformatted = event.packet.chatComponent.unformattedText.stripControlCodes()

        if (formatted.startsWith("§r§eYou purchased ") && formatted.endsWith(" coins§r§e!§r")) {
            val (name, uuid, price) = lastBought ?: return
            if (unformatted.contains(name.stripControlCodes(), true) || unformatted.replace(junkRegex, "")
                    .contains(price.toInt().toString())
            ) {
                lastBought = null
                Utils.checkThreadAndQueue {
                    prices[uuid] = price
                    dirty = true
                }
            }
        } else if (formatted.startsWith("§r§cThere was an error with the auction house!")) {
            lastBought = null
        }
    }

    @SubscribeEvent
    fun toolTip(event: ItemTooltipEvent) {
        if (!Utils.inSkyblock || !Skytils.config.pricePaid) return
        val extraAttr = ItemUtil.getExtraAttributes(event.itemStack) ?: return
        prices[UUID.fromString(extraAttr.getString("uuid").ifEmpty { return })]?.let { price ->
            event.toolTip.add("§r§7Price Paid: §9\$${NumberUtil.nf.format(price)}")
        } ?: return
    }

    @SubscribeEvent
    fun slotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (SBInfo.lastOpenContainerName?.equals("BIN Auction View") == false) return
        if (event.slotId != 31 || !event.slot!!.hasStack) return
        ItemUtil.getItemLore(event.slot.stack).firstNotNullOfOrNull {
            coinRegex.find(it)?.groupValues?.get(1)
        }?.let { price ->
            val stack = event.gui.inventorySlots.getSlot(13).stack ?: return
            val uuid = ItemUtil.getExtraAttributes(stack)?.getString("uuid")
                ?.ifEmpty { return } ?: return
            lastBought = Triple(
                ItemUtil.getDisplayName(stack),
                UUID.fromString(uuid),
                price.replace(",", "").substringBefore(' ').toDouble()
            )
        }
    }

    override fun read(reader: Reader) {
        prices.clear()
        prices.putAll(json.decodeFromString<Map<UUID, Double>>(reader.readText()))
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(prices))
    }

    override fun setDefault(writer: Writer) {
        writer.write("{}")
    }
}