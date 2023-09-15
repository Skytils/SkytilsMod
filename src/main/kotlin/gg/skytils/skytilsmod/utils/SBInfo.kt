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
package gg.skytils.skytilsmod.utils

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.json
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.skyblock.LocrawReceivedEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.events.impl.SendChatMessageEvent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapted from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
 * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
 *
 * @author Moulberry
 */
object SBInfo {

    private val timePattern = ".+(am|pm)".toRegex()

    var location = ""
    var date = ""
    var time = ""
    var objective: String? = ""
    var mode: String? = ""
    var currentTimeDate: Date? = null

    @JvmField
    var lastOpenContainerName: String? = null
    private var lastManualLocRaw: Long = -1
    private var lastLocRaw: Long = -1
    private var joinedWorld: Long = -1
    private var locraw: LocrawObject? = null
    private val junkRegex = Regex("[^\u0020-\u0127û]")

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (!Utils.inSkyblock) return
        if (event.gui is GuiChest) {
            val chest = event.gui as GuiChest
            val container = chest.inventorySlots as ContainerChest
            val containerName = container.lowerChestInventory.displayName.unformattedText
            lastOpenContainerName = containerName
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        lastLocRaw = -1
        locraw = null
        mode = null
        joinedWorld = System.currentTimeMillis()
        lastOpenContainerName = null
    }

    @SubscribeEvent
    fun onSendChatMessage(event: SendChatMessageEvent) {
        val msg = event.message
        if (msg.trim().startsWith("/locraw")) {
            lastManualLocRaw = System.currentTimeMillis()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
    fun onChatMessage(event: PacketEvent.ReceiveEvent) {
        if (event.packet is S02PacketChat) {
            val unformatted = event.packet.chatComponent.unformattedText
            if (unformatted.startsWith("{") && unformatted.endsWith("}")) {
                try {
                    val obj = json.decodeFromString<LocrawObject>(unformatted)
                    if (System.currentTimeMillis() - lastManualLocRaw > 5000) {
                        Utils.cancelChatPacket(event)
                    }
                    locraw = obj
                    mode = obj.mode
                    LocrawReceivedEvent(obj).postAndCatch()
                } catch (e: SerializationException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.SendEvent) {
        if (Utils.isOnHypixel && event.packet is C01PacketChatMessage) {
            if (event.packet.message.startsWith("/locraw")) {
                lastLocRaw = System.currentTimeMillis()
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null || !Utils.inSkyblock) return
        val currentTime = System.currentTimeMillis()
        if (locraw == null && currentTime - joinedWorld > 1300 && currentTime - lastLocRaw > 15000) {
            lastLocRaw = System.currentTimeMillis()
            Skytils.sendMessageQueue.add("/locraw")
        }
        try {
            val lines = ScoreboardUtil.fetchScoreboardLines().map { it.stripControlCodes() }
            if (lines.size >= 5) {
                //§707/14/20
                date = lines[2].stripControlCodes().trim()
                //§74:40am
                val matcher = timePattern.find(lines[3])
                if (matcher != null) {
                    time = matcher.groupValues[0].stripControlCodes().trim()
                    try {
                        val timeSpace = time.replace("am", " am").replace("pm", " pm")
                        val parseFormat = SimpleDateFormat("hh:mm a")
                        currentTimeDate = parseFormat.parse(timeSpace)
                    } catch (_: ParseException) {
                    }
                }
                lines.find { it.contains('⏣') }?.replace(junkRegex, "")?.trim()?.let {
                    location = it
                }
            }
            objective = null
            for ((i, line) in lines.withIndex()) {
                if (line == "Objective") {
                    objective = lines.elementAt(i + 1)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Serializable(with = SkyblockIsland.ObjectSerializer::class)
enum class SkyblockIsland(val displayName: String, val mode: String) {
    PrivateIsland("Private Island", "dynamic"),
    TheGarden("The Garden", "garden"),
    SpiderDen("Spider's Den", "combat_1"),
    CrimsonIsle("Crimson Isle", "crimson_isle"),
    TheEnd("The End", "combat_3"),
    GoldMine("Gold Mine", "mining_1"),
    DeepCaverns("Deep Caverns", "mining_2"),
    DwarvenMines("Dwarven Mines", "mining_3"),
    CrystalHollows("Crystal Hollows", "crystal_hollows"),
    FarmingIsland("The Farming Islands", "farming_1"),
    ThePark("The Park", "foraging_1"),
    Dungeon("Dungeon", "dungeon"),
    DungeonHub("Dungeon Hub", "dungeon_hub"),
    Hub("Hub", "hub"),
    DarkAuction("Dark Auction", "dark_auction"),
    JerryWorkshop("Jerry's Workshop", "winter"),
    Instanced("Instanced", "instanced"),
    TheRift("The Rift", "rift"),
    Unknown("(Unknown)", "");

    object ModeSerializer : KSerializer<SkyblockIsland> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("SkyblockIsland", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): SkyblockIsland =
            decoder.decodeString().let { s -> values().firstOrNull { it.mode == s } ?: Unknown }

        override fun serialize(encoder: Encoder, value: SkyblockIsland) = encoder.encodeString(value.mode)
    }

    object ObjectSerializer : KSerializer<SkyblockIsland> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SkyblockIsland") {
            element("displayName", serialDescriptor<String>())
            element("mode", serialDescriptor<String>())
        }

        override fun deserialize(decoder: Decoder): SkyblockIsland = decoder.decodeStructure(descriptor) {
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    1 -> return@decodeStructure SkyblockIsland.values()
                        .first { it.mode == decodeStringElement(descriptor, index) }

                    CompositeDecoder.DECODE_DONE -> break
                }
            }
            error("Failed to decode SkyblockIsland")
        }

        override fun serialize(encoder: Encoder, value: SkyblockIsland) = encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.displayName)
            encodeStringElement(descriptor, 1, value.mode)
        }
    }
}


@Serializable
data class LocrawObject(
    val server: String,
    val gametype: String = "unknown",
    val mode: String = "unknown",
    val map: String = "unknown"
)