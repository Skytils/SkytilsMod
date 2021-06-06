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
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.core.SoundQueue
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.features.impl.handlers.AuctionData
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import kotlin.math.pow

class MythologicalTracker : PersistentSave(File(File(Skytils.modDir, "trackers"), "mythological.json")) {

    private val rareDugDrop: Pattern = Pattern.compile("^RARE DROP! You dug out a (.+)!$")
    private val mythCreatureDug =
        Pattern.compile("^(?:Oi|Uh oh|Yikes|Woah|Oh|Danger|Good Grief)! You dug out (?:a )?(.+)!$")

    private var lastMinosChamp = 0L

    @Suppress("UNUSED")
    enum class BurrowDrop(
        val itemId: String,
        val itemName: String,
        val rarity: ItemRarity,
        val neededGriffin: Int,
        val isChat: Boolean = false,
        val mobDrop: Boolean = false,
        var droppedTimes: Long = 0L
    ) {
        REMEDIES("ANTIQUE_REMEDIES", "Antique Remedies", ItemRarity.EPIC, 4),

        // this does have a chat message but it's just Enchanted Book
        CHIMERA("ENCHANTED_BOOK-ULTIMATE_CHIMERA-1", "Chimera 1", ItemRarity.COMMON, 4),
        COINS("COINS", "Coins", ItemRarity.LEGENDARY, -1, isChat = true),
        PLUSHIE("CROCHET_TIGER_PLUSHIE", "Crochet Tiger Plushie", ItemRarity.EPIC, 4),
        COG("CROWN_OF_GREED", "Crown of Greed", ItemRarity.LEGENDARY, 3, true),
        STICK("DAEDALUS_STICK", "Daedalus Stick", ItemRarity.LEGENDARY, 4, mobDrop = true),
        SHELMET("DWARF_TURTLE_SHELMET", "Dwarf Turtle Shelmet", ItemRarity.RARE, 4),
        FEATHER("GRIFFIN_FEATHER", "Griffin Feather", ItemRarity.RARE, -1, isChat = true),
        RELIC("MINOS_RELIC", "Minos Relic", ItemRarity.EPIC, 4),
        WASHED("WASHED_UP_SOUVENIR", "Washed-up Souvenir", ItemRarity.LEGENDARY, 4, true);

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
        val neededGriffin: Int = 5,
        val plural: Boolean = false,
        var dugTimes: Long = 0L,
    ) {

        GAIA("Gaia Construct", "GAIA_CONSTRUCT", 2),
        CHAMP("Minos Champion", "MINOS_CHAMPION", 3),
        HUNTER("Minos Hunter", "MINOS_HUNTER", -1),
        INQUIS("Minos Inquisitor", "MINOS_INQUISITOR", 4),
        MINO("Minotaur", "MINOTAUR", 1),
        LYNX("Siamese Lynxes", "SIAMESE_LYNXES", 0, plural = true);

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
    fun onJoinWorld(event: EntityJoinWorldEvent) {
        if (Utils.inSkyblock && mc.thePlayer != null && Skytils.config.trackMythEvent && event.entity is EntityOtherPlayerMP && System.currentTimeMillis() - lastMinosChamp <= 2500 && event.entity.getDistanceSqToEntity(
                mc.thePlayer
            ) < 5.5 * 5.5
        ) {
            if (event.entity.name == "Minos Champion") {
                println("Dug is: Minos Champion")
                lastMinosChamp = 0L
                //BurrowMob.CHAMP.dugTimes++
            } else if (event.entity.name == "Minos Inquisitor") {
                println("Dug is: Minos Inquisitor")
                lastMinosChamp = 0L
                //BurrowMob.INQUIS.dugTimes++
                mc.thePlayer.addChatMessage(ChatComponentText("§bSkytils: §eActually, you dug up a §2Minos Inquisitor§e!"))
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
        if (!Utils.inSkyblock || (!Skytils.config.trackMythEvent && !Skytils.config.broadcastMythCreatureDrop)) return
        when (event.packet) {
            is S02PacketChat -> {
                if (event.packet.type == 2.toByte() || !Skytils.config.trackMythEvent) return
                val unformatted = event.packet.chatComponent.unformattedText.stripControlCodes()
                if (unformatted.startsWith("RARE DROP! You dug out a ")) {
                    val matcher = rareDugDrop.matcher(unformatted)
                    if (matcher.matches()) {
                        (BurrowDrop.getFromName(matcher.group(1)) ?: return).droppedTimes++
                        markDirty(this::class)
                    }
                } else if (unformatted.startsWith("Wow! You dug out ") && unformatted.endsWith(
                        " coins!"
                    )
                ) {
                    BurrowDrop.COINS.droppedTimes += unformatted.replace(Regex("[^\\d]"), "").toLong()
                } else if (unformatted.contains("! You dug out ")) {
                    val matcher = mythCreatureDug.matcher(unformatted)
                    if (matcher.matches()) {
                        val mob = BurrowMob.getFromName(matcher.group(1)) ?: return
                        //for some reason, minos inquisitors say minos champion in the chat
                        //if (mob == BurrowMob.CHAMP) {
                        //    lastMinosChamp = System.currentTimeMillis()
                        //} else {
                            mob.dugTimes++
                            markDirty(this::class)
                        //}
                    }
                } else if (unformatted.endsWith("/4)") && (unformatted.startsWith("You dug out a Griffin Burrow! (") || unformatted.startsWith(
                        "You finished the Griffin burrow chain! (4"
                    ))
                ) {
                    burrowsDug++
                    markDirty(this::class)
                } else if (unformatted.startsWith("RARE DROP! ")) {
                    for (drop in BurrowDrop.values()) {
                        if (!drop.mobDrop) continue
                        if (unformatted.startsWith("RARE DROP! ${drop.itemName}")) {
                            drop.droppedTimes++
                            break
                        }
                    }
                }
            }
            is S2FPacketSetSlot -> {
                val item = event.packet.func_149174_e() ?: return
                if (event.packet.func_149175_c() != 0 || mc.thePlayer == null || mc.thePlayer.ticksExisted <= 1) return
                val drop = BurrowDrop.getFromId(AuctionData.getIdentifier(item)) ?: return
                if (drop.isChat || drop.mobDrop) return
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

                    SoundQueue.addToQueue(
                        SoundQueue.QueuedSound(
                            "note.pling",
                            2.0.pow(-9.0 / 12).toFloat(),
                            isLoud = true
                        )
                    )
                    SoundQueue.addToQueue(
                        SoundQueue.QueuedSound(
                            "note.pling",
                            2.0.pow(-2.0 / 12).toFloat(),
                            ticks = 4,
                            isLoud = true
                        )
                    )
                    SoundQueue.addToQueue(
                        SoundQueue.QueuedSound(
                            "note.pling",
                            2.0.pow(1.0 / 12).toFloat(),
                            ticks = 8,
                            isLoud = true
                        )
                    )
                    SoundQueue.addToQueue(
                        SoundQueue.QueuedSound(
                            "note.pling",
                            2.0.pow(3.0 / 12).toFloat(),
                            ticks = 12,
                            isLoud = true
                        )
                    )
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
            mobObj.addProperty(mob.modId, mob.dugTimes)
        }
        obj.add("mobs", mobObj)
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: FileWriter) {
    }

    companion object {
        var burrowsDug = 0L

        init {
            MythologicalTrackerElement()
        }
    }

    class MythologicalTrackerElement : GuiElement("Mythological Tracker", FloatPair(150, 120)) {
        fun getTotal(): Int {
            if (!Skytils.config.trackGriffinPet) return 17
            var sum = 1
            BurrowMob.values().forEach {
                if (it.neededGriffin <= Skytils.config.griffinPetRarity) {
                    sum++
                }
            }
            BurrowDrop.values().forEach {
                if (it.neededGriffin <= Skytils.config.griffinPetRarity) {
                    sum++
                }
            }
            return sum
        }
        override fun render() {
            if (toggled && Utils.inSkyblock && SBInfo.mode == SBInfo.SkyblockIsland.Hub.mode) {
                val sr = ScaledResolution(Minecraft.getMinecraft())
                val leftAlign = actualX < sr.scaledWidth / 2f
                val alignment =
                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                val player = Minecraft.getMinecraft().thePlayer

                for (i in 0..7) {
                    val hotbarItem = player.inventory.getStackInSlot(i) ?: continue
                    if (hotbarItem.displayName.contains("Ancestral Spade")) {
                        ScreenRenderer.fontRenderer.drawString(
                            "Burrows Dug§f: $burrowsDug",
                            if (leftAlign) 0f else width.toFloat(),
                            0f,
                            CommonColors.YELLOW,
                            alignment,
                            SmartFontRenderer.TextShadow.NORMAL
                        )
                        var drawnLines = 1
                        for (mob in BurrowMob.values()) {
                            if (mob.neededGriffin <= Skytils.config.griffinPetRarity || !Skytils.config.trackGriffinPet) {
                                ScreenRenderer.fontRenderer.drawString(
                                    "${mob.mobName}§f: ${mob.dugTimes}",
                                    if (leftAlign) 0f else width.toFloat(),
                                    (drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                                    CommonColors.CYAN,
                                    alignment,
                                    SmartFontRenderer.TextShadow.NORMAL
                                )
                                drawnLines++
                            }

                        }
                        for (item in BurrowDrop.values()) {
                            if (item.neededGriffin <= Skytils.config.griffinPetRarity || !Skytils.config.trackGriffinPet) {
                                ScreenRenderer.fontRenderer.drawString(
                                    "${item.rarity.baseColor}${item.itemName}§f: §r${item.droppedTimes}",
                                    if (leftAlign) 0f else width.toFloat(),
                                    (drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                                    CommonColors.CYAN,
                                    alignment,
                                    SmartFontRenderer.TextShadow.NORMAL
                                )
                                drawnLines++
                            }
                        }
                        break
                    }
                }
            }
        }

        override fun demoRender() {
            val sr = ScaledResolution(Minecraft.getMinecraft())
            val leftAlign = actualX < sr.scaledWidth / 2f
            val alignment =
                if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
            ScreenRenderer.fontRenderer.drawString(
                "Burrows Dug§f: 1000",
                if (leftAlign) 0f else width.toFloat(),
                0f,
                CommonColors.YELLOW,
                alignment,
                SmartFontRenderer.TextShadow.NORMAL
            )
            var drawnLines = 1
            for (mob in BurrowMob.values()) {
                if (mob.neededGriffin <= Skytils.config.griffinPetRarity  || !Skytils.config.trackGriffinPet) {
                    ScreenRenderer.fontRenderer.drawString(
                        "${mob.mobName}§f: 100",
                        if (leftAlign) 0f else width.toFloat(),
                        (drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                        CommonColors.CYAN,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                    drawnLines++
                }
            }
            for (item in BurrowDrop.values()) {
                if (item.neededGriffin <= Skytils.config.griffinPetRarity || !Skytils.config.trackGriffinPet) {
                    ScreenRenderer.fontRenderer.drawString(
                        "${item.rarity.baseColor}${item.itemName}§f: §r100",
                        if (leftAlign) 0f else width.toFloat(),
                        (drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                        CommonColors.CYAN,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                    drawnLines++
                }
            }
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * getTotal()
        override val width: Int
            get() {
                if (!Skytils.config.trackGriffinPet) return ScreenRenderer.fontRenderer.getStringWidth("Crochet Tiger Plushie: 100")
                if (Skytils.config.griffinPetRarity == 3) return ScreenRenderer.fontRenderer.getStringWidth("Crown of Greed: 100")
                else if (Skytils.config.griffinPetRarity < 4) return ScreenRenderer.fontRenderer.getStringWidth("Griffin Feather: 100")
                return ScreenRenderer.fontRenderer.getStringWidth("Crochet Tiger Plushie: 100")
            }

        override val toggled: Boolean
            get() = Skytils.config.trackMythEvent

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    @SubscribeEvent
    fun onGuiOpen(event: GuiScreenEvent.BackgroundDrawnEvent) {
        if (!Utils.inSkyblock) return
        if (mc.currentScreen is GuiChest) {
            val chest = mc.currentScreen as GuiChest
            val inventory = chest.inventorySlots as ContainerChest
            if (inventory.lowerChestInventory.displayName.unformattedText.equals("Pets")) { // pets menu
                var slots = chest.inventorySlots.inventorySlots
                slots.forEach {
                    if (it.hasStack) {
                        if (ItemUtil.isPet(it.stack)) {
                            val name = it.stack.displayName
                            if (name.contains("Griffin")) {
                                Skytils.config.griffinPetRarity = ItemUtil.getRarity(it.stack)!!.rarityInt // set rarity for use above, 0-indexed with -1 as no griffin
                                Skytils.config.markDirty()
                                Skytils.config.writeData()
                            }
                        }
                    }
                }
            }
        }
    }
}