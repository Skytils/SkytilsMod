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

import gg.essential.elementa.state.v2.MutableState
import gg.essential.elementa.state.v2.State
import gg.essential.elementa.state.v2.memo
import gg.essential.elementa.state.v2.mutableStateOf
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.network.ClientDisconnectEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.screen.ScreenOpenEvent
import gg.skytils.event.postSync
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.HypixelPacketReceiveEvent
import gg.skytils.skytilsmod._event.LocationChangeEvent
import gg.skytils.skytilsmod._event.MainThreadPacketReceiveEvent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import net.hypixel.data.type.GameType
import net.hypixel.data.type.ServerType
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.screen.GenericContainerScreenHandler

import net.minecraft.network.packet.s2c.play.ScoreboardDisplayS2CPacket
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket
//#if MC>11400
import net.minecraft.network.packet.BrandCustomPayload
import net.minecraft.scoreboard.ScoreboardDisplaySlot
//#endif

object SBInfo : EventSubscriber {

    val location
        get() = locationState.getUntracked()
    val mode: String?
        get() = modeState.getUntracked().ifEmpty { null }
    val server: String?
        get() = serverIdState.getUntracked().ifEmpty { null }
    val serverType: ServerType?
        get() = serverTypeState.getUntracked()

    private val _hypixelState: MutableState<Boolean> = mutableStateOf(false)
    private val _skyblockState: MutableState<Boolean> = mutableStateOf(false)
    private val _locationState: MutableState<String> = mutableStateOf("")
    private val _modeState: MutableState<String> = mutableStateOf("")
    private val _serverIdState: MutableState<String> = mutableStateOf("")
    private val _serverTypeState: MutableState<ServerType?> = mutableStateOf(null)

    val hypixelState: State<Boolean> = memo {
        (isDeobfuscatedEnvironment() && DevTools["forcehypixel"]()) ||
                _hypixelState()
    }
    val skyblockState: State<Boolean> = memo {
        (isDeobfuscatedEnvironment() && DevTools["forceskyblock"]()) ||
                _skyblockState() || _serverTypeState() == GameType.SKYBLOCK
    }
    val dungeonsState: State<Boolean> = memo {
        (isDeobfuscatedEnvironment() && DevTools["forcedungeons"]()) ||
                _modeState() == "dungeon"
    }
    val locationState: State<String>
        get() = _locationState
    val modeState: State<String>
        get() = _modeState
    val serverIdState: State<String>
        get() = _serverIdState
    val serverTypeState: State<ServerType?>
        get() = _serverTypeState


    val lastOpenContainerName: String?
        get() = lastOpenContainerNameState.getUntracked()
    private val junkRegex = Regex("[^\u0020-\u0127û]")

    private val _lastOpenContainerNameState: MutableState<String?> = mutableStateOf(null)
    val lastOpenContainerNameState: State<String?>
        get() = _lastOpenContainerNameState

    fun onGuiOpen(event: ScreenOpenEvent) {
        if (!Utils.inSkyblock) return
        if (event.screen is GenericContainerScreen) {
            val containerName = event.screen!!.title.string
            _lastOpenContainerNameState.set(containerName)
        }
    }

    fun onWorldChange(event: WorldUnloadEvent) {
        _lastOpenContainerNameState.set(null)
    }


    fun onDisconnect(event: ClientDisconnectEvent)  {
        _hypixelState.set(false)
        _skyblockState.set(false)
        _modeState.set("")
        _serverIdState.set("")
        _serverTypeState.set(null)
        _locationState.set("")
    }

    fun onHypixelPacket(event: HypixelPacketReceiveEvent) {
        if (event.packet is ClientboundLocationPacket) {
            Utils.checkThreadAndQueue {
                _modeState.set(event.packet.mode.orElse(""))
                _serverIdState.set(event.packet.serverName)
                _serverTypeState.set(event.packet.serverType.orElse(null))
                _locationState.set("")
                postSync(LocationChangeEvent(event.packet))
            }
        }
    }

    fun onPacket(event: MainThreadPacketReceiveEvent<*>) {
        if (!Utils.isOnHypixel && event.packet is CustomPayloadS2CPacket) {
            //#if MC<11400
            //$$ if (event.packet.method_11456().toString() == "MC|Brand") {
            //$$     _hypixelState.set(event.packet.data.readString(Short.MAX_VALUE.toInt()).lowercase().contains("hypixel"))
            //$$ }
            //#else
            _hypixelState.set((event.packet.payload as? BrandCustomPayload)?.brand?.contains("hypixel") == true)
            //#endif
        }
        //#if MC<11400
        //$$ if (!Utils.inSkyblock && Utils.isOnHypixel && event.packet is ScoreboardDisplayS2CPacket && event.packet.slot == 1) {
        //#else
        if (!Utils.inSkyblock && Utils.isOnHypixel && event.packet is ScoreboardDisplayS2CPacket && event.packet.slot == ScoreboardDisplaySlot.SIDEBAR) {
        //#endif
            _skyblockState.set(event.packet.name == "SBScoreboard")
            printDevMessage("score ${event.packet.name}", "utils")
            printDevMessage("sb ${Utils.inSkyblock}", "utils")
        }
    }

    fun onTick(event: gg.skytils.event.impl.TickEvent) {
        if (mc.player == null || mc.world == null || !Utils.inSkyblock) return
        try {
            val lines = ScoreboardUtil.fetchScoreboardLines().map { it.stripControlCodes() }
            if (lines.size >= 5) {
                lines.find { it.contains('⏣') }?.replace(junkRegex, "")?.trim()?.let {
                    _locationState.set(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setup() {
        register(::onGuiOpen)
        register(::onWorldChange)
        register(::onDisconnect)
        register(::onTick)
        register(::onHypixelPacket, EventPriority.High)
        register(::onPacket, EventPriority.Highest)
    }
}

@Serializable(with = SkyblockIsland.ObjectSerializer::class)
enum class SkyblockIsland(val displayName: String, val mode: String) {
    PrivateIsland("Private Island", "dynamic"),
    TheGarden("The Garden", "garden"),
    SpiderDen("Spider's Den", "combat_1"),
    CrimsonIsle("Crimson Isle", "crimson_isle"),
    TheEnd("The End", "combat_3"),
    BackwaterBayou("Backwater Bayou", "fishing_1"),
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
    GlaciteMineshafts("Glacite Mineshafts", "mineshaft"),
    TheRift("The Rift", "rift"),
    Unknown("(Unknown)", "");

    companion object {
        val byMode = entries.associateBy { it.mode }
    }

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


/** Returns the current island based on the mode, or [SkyblockIsland.Unknown] if not found */
val SkyblockIsland.Companion.current get() = SkyblockIsland.byMode[SBInfo.mode] ?: SkyblockIsland.Unknown