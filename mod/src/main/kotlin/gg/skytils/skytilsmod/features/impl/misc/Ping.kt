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

import gg.essential.elementa.unstable.layoutdsl.LayoutScope
import gg.essential.elementa.unstable.layoutdsl.Modifier
import gg.essential.elementa.unstable.layoutdsl.color
import gg.essential.elementa.unstable.state.v2.State
import gg.essential.elementa.unstable.state.v2.combinators.map
import gg.essential.elementa.unstable.state.v2.mutableStateOf
import gg.essential.universal.UChat
import gg.skytils.event.EventSubscriber
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.Skytils.prefix
import gg.skytils.skytilsmod._event.PacketReceiveEvent
import gg.skytils.skytilsmod._event.RenderHUDEvent
import gg.skytils.skytilsmod.core.structure.v2.HudElement
import gg.skytils.skytilsmod.gui.layout.text
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorMultiplayerServerListWidget
import gg.skytils.skytilsmod.utils.NumberUtil
import gg.skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.hasMoved
import net.minecraft.client.network.ServerInfo
import net.minecraft.client.network.MultiplayerServerListPinger
import net.minecraft.network.PacketCallbacks
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket
import net.minecraft.network.packet.s2c.play.StatisticsS2CPacket
import java.awt.Color
import kotlin.math.abs
import kotlin.math.absoluteValue

object Ping : EventSubscriber {

    var lastPingAt = -1L

    val pingCacheState = mutableStateOf(-1.0)

    var invokedCommand = false

    val oldServerPinger = MultiplayerServerListPinger()
    private val dummyServerInfo = ServerInfo("Skytils-Dummy-Hypixel", "mc.hypixel.net", ServerInfo.ServerType.OTHER)
    var lastOldServerPing = 0L

    override fun setup() {
        register(::onPacket)
        register(::onRenderHUD)
    }

    fun sendPing() {
        if (lastPingAt > 0) {
            if (invokedCommand) UChat.chat("§cAlready pinging!")
            return
        }
        mc.player?.networkHandler?.connection?.send(
            ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS),
            PacketCallbacks.always {
                lastPingAt = System.nanoTime()
            }
        )
    }

    fun pingServerList() {
        if (System.currentTimeMillis() - lastOldServerPing > 5000) {
            lastOldServerPing = System.currentTimeMillis()
            AccessorMultiplayerServerListWidget.getThreadPool()
                .submit {
                    oldServerPinger.add(
                        dummyServerInfo,
                        {},
                        { pingCacheState.set { dummyServerInfo.ping.toDouble() } },
                        //#if MC>=12111
                        //$$ net.minecraft.network.NetworkingBackend.remote(net.minecraft.client.MinecraftClient.getInstance().options.shouldUseNativeTransport())
                        //#endif
                    )
                }
        }
    }

    fun onPacket(event: PacketReceiveEvent<*>) {
        if (lastPingAt > 0) {
            when (event.packet) {
                is GameJoinS2CPacket -> {
                    lastPingAt = -1L
                    invokedCommand = false
                }

                is StatisticsS2CPacket -> {
                    val diff = (abs(System.nanoTime() - lastPingAt) / 1_000_000.0)
                    lastPingAt *= -1
                    pingCacheState.set { diff }
                    if (invokedCommand) {
                        invokedCommand = false
                        UChat.chat(
                            "$prefix §${
                                when {
                                    diff < 50 -> "a"
                                    diff < 100 -> "2"
                                    diff < 149 -> "e"
                                    diff < 249 -> "6"
                                    else -> "c"
                                }
                            }${diff.roundToPrecision(2)} §7ms"
                        )
                    }
                }
            }
        }
    }

    fun onRenderHUD(event: RenderHUDEvent) {
        if (SBInfo.skyblockState.getUntracked() && mc.player != null) {
            when (Skytils.config.pingDisplay.getUntracked()) {
                1 -> pingServerList()
                2 -> {
                    if (lastPingAt < 0 && (mc.currentScreen != null || mc.player?.hasMoved == false) && System.nanoTime()
                        - lastPingAt.absoluteValue > 1_000_000L * 5_000
                    ) {
                        sendPing()
                    }
                }
            }
        }
    }

    class PingDisplayHud : HudElement("Ping Display", 10f, 10f) {
        override val toggleState: State<Boolean> = Skytils.config.pingDisplay.map { it != 0 }

        private val color = State {
            val ping = pingCacheState()
            when {
                ping < 50 -> Color.GREEN
                ping < 100 -> Color(0x00AA00)
                ping < 149 -> Color.YELLOW
                ping < 249 -> Color.ORANGE
                else -> Color.RED
            }
        }
        private val text = State {
            val ping = pingCacheState()
            "${NumberUtil.nf.format(ping.roundToPrecision(2))}ms"
        }
        override fun LayoutScope.render() {
            if_(SBInfo.hypixelState) {
                text(text, Modifier.color(color))
            }
        }

        override fun LayoutScope.demoRender() {
            text("69.69ms", Modifier.color(Color(0x00AA00)))
        }

    }

    init {
        Skytils.guiManager.registerElement(PingDisplayHud())
    }
}