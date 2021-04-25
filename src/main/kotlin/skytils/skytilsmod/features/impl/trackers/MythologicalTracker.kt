/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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

package skytils.skytilsmod.features.impl.trackers

import com.google.gson.JsonObject
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.utils.ItemRarity
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.Utils
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class MythologicalTracker : PersistentSave(File(File(Skytils.modDir, "trackers"), "mythological.json")) {

    @Suppress("UNUSED")
    enum class BurrowDrop(val itemId: String, val itemName: String, val rarity: ItemRarity, var droppedTimes: Int = 0) {
        REMEDIES("ANTIQUE_REMEDIES", "Antique Remedies", ItemRarity.EPIC),
        PLUSHIE("CROCHET_TIGER_PLUSHIE", "Crochet Tiger Plushie", ItemRarity.EPIC),
        COG("CROWN_OF_GREED", "Crown of Greed", ItemRarity.LEGENDARY),
        STICK("DAEDALUS_STICK", "Daedalus Stick", ItemRarity.LEGENDARY),
        SHELMET("DWARF_TURTLE_SHELMET", "Dwarf Turtle Shelmet", ItemRarity.RARE),
        CHIMERA("ENCHANTED_BOOK-ULTIMATE_CHIMERA-1", "Chimera 1", ItemRarity.COMMON),
        RELIC("MINOS_RELIC", "Minos Relic", ItemRarity.EPIC),
        WASHED("WASHED_UP_SOUVENIR", "Washed-Up Souvenir", ItemRarity.LEGENDARY);

        companion object {
            fun getFromId(id: String?): BurrowDrop? {
                return values().find { it.itemId == id }
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
        if (!Utils.inSkyblock || (!Skytils.config.trackMythCreatureDrops && !Skytils.config.broadcastMythCreatureDrop)) return
        if (event.packet !is S2FPacketSetSlot) return
        val item = event.packet.func_149174_e() ?: return
        if (event.packet.func_149175_c() != 0 || mc.thePlayer.ticksExisted <= 1) return
        val drop = BurrowDrop.getFromId(AuctionData.getIdentifier(item)) ?: return
        val extraAttr = ItemUtil.getExtraAttributes(item) ?: return
        if (!extraAttr.hasKey("timestamp")) return
        val time = ZonedDateTime.from(
            DateTimeFormatter.ofPattern("M/d/yy h:mm a").withZone(
                ZoneId.of("America/New_York")
            ).parse(extraAttr.getString("timestamp"))
        )
        if (ZonedDateTime.now().withSecond(0).withNano(0).toEpochSecond() - time.toEpochSecond() > 60) return

        if (Skytils.config.broadcastMythCreatureDrop) {
            mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("§6§lRARE DROP! ${drop.rarity.baseColor}${drop.itemName} §b(Skytils User Luck!)"))
        }
        if (Skytils.config.trackMythCreatureDrops) {
            drop.droppedTimes++
            markDirty(this::class)
        }
    }

    override fun read(reader: FileReader) {
        val obj = gson.fromJson(reader, JsonObject::class.java)
        for (entry in obj.get("items").asJsonObject.entrySet()) {
            (BurrowDrop.getFromId(entry.key) ?: continue).droppedTimes = entry.value.asInt
        }
    }

    override fun write(writer: FileWriter) {
        val obj = JsonObject()
        val itemObj = JsonObject()
        for (item in BurrowDrop.values()) {
            itemObj.addProperty(item.itemId, item.droppedTimes)
        }
        obj.add("items", itemObj)
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: FileWriter) {
        val obj = JsonObject()
        val itemObj = JsonObject()
        for (item in BurrowDrop.values()) {
            itemObj.addProperty(item.itemId, 0)
        }
        obj.add("items", itemObj)
        gson.toJson(obj, writer)
    }
}