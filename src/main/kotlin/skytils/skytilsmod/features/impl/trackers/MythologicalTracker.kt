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
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.utils.ItemRarity
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.StringUtils
import skytils.skytilsmod.utils.Utils
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class MythologicalTracker : PersistentSave(File(File(Skytils.modDir, "trackers"), "mythological.json")) {

    private val rareDugDrop: Pattern = Pattern.compile("^RARE DROP! You dug out a (.+)!$")
    private val mythCreatureDug = Pattern.compile("^(?:Oi|Uh oh|Yikes|Woah|Oh|Danger)! You dug out (?:a )?(.+)!$")

    var burrowsDug = 0L

    @Suppress("UNUSED")
    enum class BurrowDrop(
        val itemId: String,
        val itemName: String,
        val rarity: ItemRarity,
        val isChat: Boolean = false,
        var droppedTimes: Long = 0L
    ) {
        REMEDIES("ANTIQUE_REMEDIES", "Antique Remedies", ItemRarity.EPIC),
        COINS("COINS", "Coins", ItemRarity.LEGENDARY, isChat = true),
        PLUSHIE("CROCHET_TIGER_PLUSHIE", "Crochet Tiger Plushie", ItemRarity.EPIC),
        COG("CROWN_OF_GREED", "Crown of Greed", ItemRarity.LEGENDARY, true),
        STICK("DAEDALUS_STICK", "Daedalus Stick", ItemRarity.LEGENDARY, true),
        SHELMET("DWARF_TURTLE_SHELMET", "Dwarf Turtle Shelmet", ItemRarity.RARE),
        CHIMERA("ENCHANTED_BOOK-ULTIMATE_CHIMERA-1", "Chimera 1", ItemRarity.COMMON),
        FEATHER("GRIFFIN_FEATHER", "Griffin Feather", ItemRarity.RARE, isChat = true),
        RELIC("MINOS_RELIC", "Minos Relic", ItemRarity.EPIC),
        WASHED("WASHED_UP_SOUVENIR", "Washed-up Souvenir", ItemRarity.LEGENDARY, true);

        companion object {
            fun getFromId(id: String?): BurrowDrop? {
                return values().find { it.itemId == id }
            }

            fun getFromName(name: String?): BurrowDrop? {
                return values().find { it.itemName == name }
            }
        }
    }

    @Suppress("UNUSED")
    enum class BurrowMob(
        val mobName: String,
        val modId: String,
        val plural: Boolean = false,
        var dugTimes: Long = 0L,
    ) {

        HUNTER("Minos Hunter", "MINOS_HUNTER"),
        LYNX("Siamese Lynxes", "SIAMESE_LYNXES", plural = true),
        MINO("Minotaur", "MINOTAUR"),
        GAIA("Gaia Construct", "GAIA_CONSTRUCT"),
        CHAMP("Minos Champion", "MINOS_CHAMPION"),
        INQUIS("Minos Inquisitor", "MINOS_INQUISITOR");

        companion object {
            fun getFromId(id: String?): BurrowMob? {
                return values().find { it.modId == id }
            }

            fun getFromName(name: String?): BurrowMob? {
                return values().find { it.mobName == name }
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
        if (!Utils.inSkyblock || !Skytils.config.trackMythEvent) return
        when (event.packet) {
            is S02PacketChat -> {
                if (event.packet.type == 2.toByte()) return
                val unformatted = StringUtils.stripControlCodes(event.packet.chatComponent.unformattedText)
                if (unformatted.startsWith("RARE DROP! You dug out a ")) {
                    val matcher = rareDugDrop.matcher(unformatted)
                    if (matcher.matches()) {
                        (BurrowDrop.getFromName(matcher.group(1)) ?: return).droppedTimes++
                        markDirty(this::class)
                    }
                } else if (unformatted.startsWith("Wow! You dug out ") && unformatted.endsWith(" coins!")) {
                    BurrowDrop.COINS.droppedTimes += unformatted.replace(Regex("[^\\d]"), "").toLong()
                } else if (unformatted.contains("! You dug out ")) {
                    val matcher = mythCreatureDug.matcher(unformatted)
                    if (matcher.matches()) {
                        (BurrowMob.getFromName(matcher.group(1)) ?: return).dugTimes++;
                        markDirty(this::class)
                    }
                } else if (unformatted.endsWith("/4)") && (unformatted.startsWith("You dug out a Griffin Burrow! (") || unformatted.startsWith("You finished the Griffin burrow chain! (4"))) {
                    burrowsDug++
                    markDirty(this::class)
                }
            }
            is S2FPacketSetSlot -> {
                val item = event.packet.func_149174_e() ?: return
                if (event.packet.func_149175_c() != 0 || mc.thePlayer.ticksExisted <= 1) return
                val drop = BurrowDrop.getFromId(AuctionData.getIdentifier(item)) ?: return
                if (drop.isChat) return
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
                if (Skytils.config.trackMythEvent) {
                    drop.droppedTimes++
                    markDirty(this::class)
                }
            }
        }
    }

    override fun read(reader: FileReader) {
        val obj = gson.fromJson(reader, JsonObject::class.java)
        burrowsDug = obj.get("dug").asLong
        for (entry in obj.get("items").asJsonObject.entrySet()) {
            (BurrowDrop.getFromId(entry.key) ?: continue).droppedTimes = entry.value.asLong
        }
        for (entry in obj.get("mobs").asJsonObject.entrySet()) {
            (BurrowMob.getFromId(entry.key) ?: continue).dugTimes = entry.value.asLong
        }
    }

    override fun write(writer: FileWriter) {
        val obj = JsonObject()

        obj.addProperty("dug", burrowsDug)

        val itemObj = JsonObject()
        for (item in BurrowDrop.values()) {
            itemObj.addProperty(item.itemId, item.droppedTimes)
        }
        obj.add("items", itemObj)

        val mobObj = JsonObject()
        for (mob in BurrowMob.values()) {
            itemObj.addProperty(mob.modId, mob.dugTimes)
        }
        obj.add("mobs", mobObj)
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: FileWriter) {
        val obj = JsonObject()

        obj.addProperty("dug", burrowsDug)

        val itemObj = JsonObject()
        for (item in BurrowDrop.values()) {
            itemObj.addProperty(item.itemId, item.droppedTimes)
        }
        obj.add("items", itemObj)

        val mobObj = JsonObject()
        for (mob in BurrowMob.values()) {
            itemObj.addProperty(mob.modId, mob.dugTimes)
        }
        obj.add("mobs", mobObj)
        gson.toJson(obj, writer)
    }
}