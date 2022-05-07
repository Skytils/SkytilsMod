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

package gg.skytils.skytilsmod.features.impl.trackers.impl

import gg.essential.universal.UChat
import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.core.SoundQueue
import gg.skytils.skytilsmod.core.structure.FloatPair
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

    private val timestampFormat = DateTimeFormatter.ofPattern("M/d/yy h:mm a").withZone(
        ZoneId.of("America/New_York")
    )

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

        GAIA("Gaia Construct", "GAIA_CONSTRUCT"),
        CHAMP("Minos Champion", "MINOS_CHAMPION"),
        HUNTER("Minos Hunter", "MINOS_HUNTER"),
        INQUIS("Minos Inquisitor", "MINOS_INQUISITOR"),
        MINO("Minotaur", "MINOTAUR"),
        LYNX("Siamese Lynxes", "SIAMESE_LYNXES", plural = true);

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
        if (lastMinosChamp != 0L && Utils.inSkyblock && mc.thePlayer != null && Skytils.config.trackMythEvent && event.entity is EntityOtherPlayerMP && event.entity.getXZDistSq(
                mc.thePlayer
            ) < 5.5 * 5.5 && System.currentTimeMillis() - lastMinosChamp <= 2500
        ) {
            if (event.entity.name == "Minos Champion") {
                println("Dug is: Minos Champion")
                lastMinosChamp = 0L
                BurrowMob.CHAMP.dugTimes++
                UChat.chat("$prefix §eYou dug up a §2Minos Champion§e!")
                markDirty<MythologicalTracker>()
            } else if (event.entity.name == "Minos Inquisitor") {
                println("Dug is: Minos Inquisitor")
                lastMinosChamp = 0L
                BurrowMob.INQUIS.dugTimes++
                UChat.chat("$prefix §eYou dug up a §2Minos Inquisitor§e!")
                markDirty<MythologicalTracker>()
            }
        }
        if (lastMinosChamp != 0L && System.currentTimeMillis() - lastMinosChamp > 2500) {
            println("Dug is: Unknown")
            lastMinosChamp = 0L
            BurrowMob.CHAMP.dugTimes++
            UChat.chat("$prefix §eNo idea what you dug, counting as §2Minos Champion§e!")
            markDirty<MythologicalTracker>()
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
                        //for some reason, minos inquisitors say minos champion in the chat
                        if (mob == BurrowMob.CHAMP) {
                            Utils.cancelChatPacket(event)
                            lastMinosChamp = System.currentTimeMillis()
                        } else {
                            mob.dugTimes++
                            markDirty<MythologicalTracker>()
                        }
                    }
                } else if (unformatted.endsWith("/4)") && (unformatted.startsWith("You dug out a Griffin Burrow! (") || unformatted.startsWith(
                        "You finished the Griffin burrow chain! (4"
                    ))
                ) {
                    burrowsDug++
                    markDirty<MythologicalTracker>()
                } else if (unformatted.startsWith("RARE DROP! ")) {
                    for (drop in BurrowDrop.values()) {
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
        BurrowDrop.values().forEach { it.droppedTimes = 0L }
        BurrowMob.values().forEach { it.dugTimes = 0L }
    }

    // TODO: 5/3/2022 fix this
    @kotlinx.serialization.Serializable
    data class TrackerSave(
        val burrowsDug: Long,
        @SerialName("items")
        val drops: Map<BurrowDrop, Long>,
        val mobs: Map<BurrowMob, Long>
    )

    override fun read(reader: Reader) {
        val save = json.decodeFromString<TrackerSave>(reader.readText())
        burrowsDug = save.burrowsDug
        BurrowDrop.values().forEach { it.droppedTimes = save.drops[it] ?: 0L }
        BurrowMob.values().forEach { it.dugTimes = save.mobs[it] ?: 0L }
    }

    override fun write(writer: Writer) {
        writer.write(
            json.encodeToString(
                TrackerSave(
                    burrowsDug,
                    BurrowDrop.values().associateWith(BurrowDrop::droppedTimes),
                    BurrowMob.values().associateWith(BurrowMob::dugTimes)
                )
            )
        )
    }

    override fun setDefault(writer: Writer) {
        write(writer)
    }

    object MythologicalTrackerElement : GuiElement("Mythological Tracker", FloatPair(150, 120)) {
        override fun render() {
            if (toggled && Utils.inSkyblock && GriffinBurrows.hasSpadeInHotbar && SBInfo.mode == SkyblockIsland.Hub.mode) {
                val sr = UResolution
                val leftAlign = actualX < sr.scaledWidth / 2f
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
                for (mob in BurrowMob.values()) {
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
                for (item in BurrowDrop.values()) {
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
            val sr = UResolution
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
            for (item in BurrowDrop.values()) {
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