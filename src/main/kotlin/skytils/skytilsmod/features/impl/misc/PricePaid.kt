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

import com.google.gson.JsonObject
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.events.impl.GuiContainerEvent
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.NumberUtil
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.Utils
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.*

object PricePaid : PersistentSave(File(Skytils.modDir, "pricepaid.json")) {
    private val prices = mutableMapOf<UUID, Double>()
    private val coinRegex = Regex("(?:§6)?([\\d,]+) coins", RegexOption.IGNORE_CASE)

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
            val uuid = ItemUtil.getExtraAttributes(event.gui.inventorySlots.getSlot(13).stack)!!.getString("uuid")
                .ifEmpty { return }
            prices[UUID.fromString(uuid)] = price.replace(",", "").substringBefore(' ').toDouble()
            dirty = true
        }
    }

    override fun read(reader: InputStreamReader) {
        gson.fromJson(reader, JsonObject::class.java).entrySet().forEach { price ->
            prices[UUID.fromString(price.key)] = price.value.asDouble
        }
    }

    override fun write(writer: OutputStreamWriter) {
        val res = JsonObject()
        prices.forEach { (uuid, price) ->
            res.addProperty(uuid.toString(), price)
        }
        gson.toJson(res, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        gson.toJson(JsonObject(), writer)
    }

}