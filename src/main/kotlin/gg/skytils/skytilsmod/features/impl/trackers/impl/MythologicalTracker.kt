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

package gg.skytils.skytilsmod.features.impl.trackers.impl

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.core.SoundQueue
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.features.impl.events.GriffinBurrows
import gg.skytils.skytilsmod.features.impl.handlers.AuctionData
import gg.skytils.skytilsmod.features.impl.trackers.Tracker
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.NumberUtil.nf
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.Reader
import java.io.Writer
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.pow

object MythologicalTracker : Tracker("mythological") {

    private val rareDugDrop = Regex("^RARE DROP! You dug out a (.+)!$")
    private val mythCreatureDug = Regex("^(?:Oi|Uh oh|Yikes|Woah|Oh|Danger|Good Grief)! You dug out (?:a )?(.+)!$")

    private var lastMinosChamp = 0L

    private val timestampFormat = DateTimeFormatter
        .ofPattern("M/d/yy h:mm a")
        .withZone(ZoneId.of("America/New_York"))
        .withLocale(Locale.US)

    private val seenUUIDs = WeakHashMap<String, Boolean>().asSet

    var burrowsDug = 0L

    init {
        MythologicalTrackerElement
    }

    @Suppress("UNUSED")
    enum class BurrowDrop(
        val itemId: String,
        val itemName: String,
        val rarity: ItemRarity,
        val isChat: Boolean = false,
        val mobDrop: Boolean = false,
        var droppedTimes: Long = 0L
    ) {
        REMEDIES("ANTIQUE_REMEDIES", "Antique Remedies", ItemRarity.EPIC),

        // this does have a chat message but it's just Enchanted Book
        CHIMERA("ENCHANTED_BOOK-ULTIMATE_CHIMERA-1", "Chimera", ItemRarity.COMMON),
        COINS("COINS", "Coins", ItemRarity.LEGENDARY, isChat = true),
        PLUSHIE("CROCHET_TIGER_PLUSHIE", "Crochet Tiger Plushie", ItemRarity.EPIC),
        COG("CROWN_OF_GREED", "Crown of Greed", ItemRarity.LEGENDARY, true),
        STICK("DAEDALUS_STICK", "Daedalus Stick", ItemRarity.LEGENDARY, mobDrop = true),
        SHELMET("DWARF_TURTLE_SHELMET", "Dwarf Turtle Shelmet", ItemRarity.RARE),
        FEATHER("GRIFFIN_FEATHER", "Griffin Feather", ItemRarity.RARE, isChat = true),
        RELIC("MINOS_RELIC", "Minos Relic", ItemRarity.EPIC),
        WASHED("WASHED_UP_SOUVENIR", "Washed-up Souvenir", ItemRarity.LEGENDARY, true);

        companion object {
            fun getFromId(id: String?): BurrowDrop? {
                return entries.find { it.itemId == id }
            }

            fun getFromName(name: String?): BurrowDrop? {
                return entries.find { it.itemName == name }
            }
        }
    }

    @Suppress("UNUSED")
    enum class BurrowMob(
        val mobName: String,
        val mobId: String,
        val plural: Boolean = false,
        var dugTimes: Long = 0L,
    ) {

        GAIA("Gaia Construct", "GAIA_CONSTRUCT"),
        CHAMP("Minos Champion", "MINOS_CHAMPION"),
        HUNTER("Minos Hunter", "MINOS_HUNTER"),
        INQUIS("Minos Inquisitor", "MINOS_INQUISITOR"),
        MINO("Minotaur", "MINOTAUR"),
        LYNX("Siamese Lynxes", "SIAMESE_LYNXES", plural = true);

        companion object {
            fun getFromId(id: String?): BurrowMob? {
                return entries.find { it.mobId == id }
            }

            fun getFromName(name: String?): BurrowMob? {
                return entries.find { it.mobName == name }
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
        if (!Utils.inSkyblock || (!Skytils.config.trackMythEvent && !Skytils.config.broadcastMythCreatureDrop)) return
        when (event.packet) {
            is S02PacketChat -> {
                if (event.packet.type == 2.toByte() || !Skytils.config.trackMythEvent) return
                val unformatted = event.packet.chatComponent.unformattedText.stripControlCodes()
                if (unformatted.startsWith("RARE DROP! You dug out a ")) {
                    rareDugDrop.matchEntire(unformatted)?.let {
                        (BurrowDrop.getFromName(it.groups[1]?.value ?: return) ?: return).droppedTimes++
                        markDirty<MythologicalTracker>()
                    }
                } else if (unformatted.startsWith("Wow! You dug out ") && unformatted.endsWith(
                        " coins!"
                    )
                ) {
                    BurrowDrop.COINS.droppedTimes += unformatted.replace(Regex("[^\\d]"), "").toLong()
                } else if (unformatted.contains("! You dug out ")) {
                    mythCreatureDug.matchEntire(unformatted)?.let {
                        val mob = BurrowMob.getFromName(it.groups[1]?.value ?: return) ?: return
                        mob.dugTimes++
                        markDirty<MythologicalTracker>()
                    }
                } else if (unformatted.endsWith("/4)") && (unformatted.startsWith("You dug out a Griffin Burrow! (") || unformatted.startsWith(
                        "You finished the Griffin burrow chain! (4"
                    ))
                ) {
                    burrowsDug++
                    markDirty<MythologicalTracker>()
                } else if (unformatted.startsWith("RARE DROP! ")) {
                    for (drop in BurrowDrop.entries) {
                        if (!drop.mobDrop) continue
                        if (unformatted.startsWith("RARE DROP! ${drop.itemName}")) {
                            drop.droppedTimes++
                            markDirty<MythologicalTracker>()
                            break
                        }
                    }
                }
            }

            is S2FPacketSetSlot -> {
                val item = event.packet.func_149174_e() ?: return
                if (event.packet.func_149175_c() != 0 || mc.thePlayer == null || mc.thePlayer.ticksExisted <= 1 || mc.thePlayer?.openContainer != mc.thePlayer?.inventoryContainer) return
                val drop = BurrowDrop.getFromId(AuctionData.getIdentifier(item)) ?: return
                if (drop.isChat || drop.mobDrop) return
                val extraAttr = ItemUtil.getExtraAttributes(item) ?: return
                if (!extraAttr.hasKey("timestamp")) return
                if (!seenUUIDs.add(extraAttr.getString("uuid"))) return
                val time = ZonedDateTime.from(
                    timestampFormat.parse(extraAttr.getString("timestamp"))
                )
                if (ZonedDateTime.now().withSecond(0).withNano(0).toEpochSecond() - time.toEpochSecond() > 120) return
                if (Skytils.config.broadcastMythCreatureDrop) {
                    val text = "§6§lRARE DROP! ${drop.rarity.baseColor}${drop.itemName} §b(Skytils User Luck!)"
                    if (Skytils.config.autoCopyRNGDrops) GuiScreen.setClipboardString(text)
                    UChat.chat(text)
                    SoundQueue.addToQueue(
                        SoundQueue.QueuedSound(
                            "note.pling",
                            2.0.pow(-9.0 / 12).toFloat(),
                            volume = 0.5f
                        )
                    )
                    SoundQueue.addToQueue(
                        SoundQueue.QueuedSound(
                            "note.pling",
                            2.0.pow(-2.0 / 12).toFloat(),
                            ticks = 4,
                            volume = 0.5f
                        )
                    )
                    SoundQueue.addToQueue(
                        SoundQueue.QueuedSound(
                            "note.pling",
                            2.0.pow(1.0 / 12).toFloat(),
                            ticks = 8,
                            volume = 0.5f
                        )
                    )
                    SoundQueue.addToQueue(
                        SoundQueue.QueuedSound(
                            "note.pling",
                            2.0.pow(3.0 / 12).toFloat(),
                            ticks = 12,
                            volume = 0.5f
                        )
                    )
                }
                if (Skytils.config.trackMythEvent) {
                    drop.droppedTimes++
                    markDirty<MythologicalTracker>()
                }
            }
        }
    }

    override fun resetLoot() {
        burrowsDug = 0L
        BurrowDrop.entries.forEach { it.droppedTimes = 0L }
        BurrowMob.entries.forEach { it.dugTimes = 0L }
    }

    // TODO: 5/3/2022 fix this
    @kotlinx.serialization.Serializable
    data class TrackerSave(
        @SerialName("dug")
        val burrowsDug: Long,
        @SerialName("items")
        val drops: Map<String, Long>,
        val mobs: Map<String, Long>
    )

    override fun read(reader: Reader) {
        val save = json.decodeFromString<TrackerSave>(reader.readText())
        burrowsDug = save.burrowsDug
        BurrowDrop.entries.forEach { it.droppedTimes = save.drops[it.itemId] ?: 0L }
        BurrowMob.entries.forEach { it.dugTimes = save.mobs[it.mobId] ?: 0L }
    }

    override fun write(writer: Writer) {
        writer.write(
            json.encodeToString(
                TrackerSave(
                    burrowsDug,
                    BurrowDrop.entries.associate { it.itemId to it.droppedTimes },
                    BurrowMob.entries.associate { it.mobId to it.dugTimes }
                )
            )
        )
    }

    override fun setDefault(writer: Writer) {
        write(writer)
    }

    object MythologicalTrackerElement : GuiElement("Mythological Tracker", x = 150, y = 120) {
        override fun render() {
            if (toggled && Utils.inSkyblock && GriffinBurrows.hasSpadeInHotbar && SBInfo.mode == SkyblockIsland.Hub.mode) {

                val leftAlign = scaleX < sr.scaledWidth / 2f
                val alignment =
                    if (leftAlign) SmartFontRenderer.TextAlignment.LEFT_RIGHT else SmartFontRenderer.TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    "Burrows Dug§f: ${nf.format(burrowsDug)}",
                    if (leftAlign) 0f else width.toFloat(),
                    0f,
                    CommonColors.YELLOW,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
                var drawnLines = 1
                for (mob in BurrowMob.entries) {
                    if (mob.dugTimes == 0L) continue
                    ScreenRenderer.fontRenderer.drawString(
                        "${mob.mobName}§f: ${nf.format(mob.dugTimes)}",
                        if (leftAlign) 0f else width.toFloat(),
                        (drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT).toFloat(),
                        CommonColors.CYAN,
                        alignment,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                    drawnLines++
                }
                for (item in BurrowDrop.entries) {
                    if (item.droppedTimes == 0L) continue
                    ScreenRenderer.fontRenderer.drawString(
                        "${item.rarity.baseColor}${item.itemName}§f: §r${nf.format(item.droppedTimes)}",
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

        override fun demoRender() {

            val leftAlign = scaleX < sr.scaledWidth / 2f
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
            for (mob in BurrowMob.entries) {
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
            for (item in BurrowDrop.entries) {
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

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT * 17
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Crochet Tiger Plushie: 100")

        override val toggled: Boolean
            get() = Skytils.config.trackMythEvent

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

}