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

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.HypixelPacketEvent
import gg.skytils.skytilsmod.events.impl.skyblock.LocationChangeEvent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent
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
    var objective: String? = null
    var mode: String? = null
    var server: String? = null
    var currentTimeDate: Date? = null
    var lastLocationPacket: ClientboundLocationPacket? = null

    @JvmField
    var lastOpenContainerName: String? = null
    private val junkRegex = Regex("[^\u0020-\u0127û]")

    @SubscribeEvent(priority = EventPriority.HIGHEST)
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
    fun onWorldChange(event: WorldEvent.Unload) {
        lastOpenContainerName = null
    }

    @SubscribeEvent
    fun onDisconnect(event: ClientDisconnectionFromServerEvent)  {
        mode = null
        server = null
        lastLocationPacket = null
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onHypixelPacket(event: HypixelPacketEvent.ReceiveEvent) {
        if (event.packet is ClientboundLocationPacket) {
            Utils.checkThreadAndQueue {
                mode = event.packet.mode.orElse(null)
                server = event.packet.serverName
                lastLocationPacket = event.packet
                println(event.packet)
                LocationChangeEvent(event.packet).postAndCatch()
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null || !Utils.inSkyblock) return
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
    KuudraHollow("Kuudra's Hollow", "kuudra"),
    TheRift("The Rift", "rift"),
    Unknown("(Unknown)", "");

    object ModeSerializer : KSerializer<SkyblockIsland> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("SkyblockIsland", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): SkyblockIsland =
            decoder.decodeString().let { s -> entries.firstOrNull { it.mode == s } ?: Unknown }

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
                    1 -> return@decodeStructure decodeStringElement(descriptor, index).let { s ->
                        entries
                            .first { it.mode == s }
                    }

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