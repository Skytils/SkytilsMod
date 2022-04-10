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

import gg.essential.universal.UChat
import net.minecraft.client.network.OldServerPinger
import net.minecraft.network.play.client.C16PacketClientStatus
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S37PacketStatistics
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.Skytils.Companion.prefix
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.impl.PacketEvent
import skytils.skytilsmod.mixins.transformers.accessors.AccessorServerListEntryNormal
import skytils.skytilsmod.utils.NumberUtil
import skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import skytils.skytilsmod.utils.hasMoved
import kotlin.math.abs
import kotlin.math.absoluteValue

object Ping {

    var lastPingAt = -1L

    var pingCache = -1.0

    var invokedCommand = false

    val oldServerPinger = OldServerPinger()
    var lastOldServerPing = 0L

    fun sendPing() {
        if (lastPingAt > 0) {
            if (invokedCommand) UChat.chat("§cAlready pinging!")
            return
        }
        mc.thePlayer.sendQueue.networkManager.sendPacket(
            C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS),
            {
                lastPingAt = System.nanoTime()
            }
        )
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.ReceiveEvent) {
        if (lastPingAt > 0) {
            when (event.packet) {
                is S01PacketJoinGame -> {
                    lastPingAt = -1L
                    invokedCommand = false
                }
                is S37PacketStatistics -> {
                    val diff = (abs(System.nanoTime() - lastPingAt) / 1_000_000.0)
                    lastPingAt *= -1
                    pingCache = diff
                    if (invokedCommand) {
                        invokedCommand = false
                        UChat.chat(
                            "$prefix §${
                                when {
                                    diff < 100 -> "a"
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

    class PingDisplayElement : GuiElement(name = "Ping Display", fp = FloatPair(10, 10)) {
        override fun render() {
            if (Utils.isOnHypixel && toggled && mc.thePlayer != null) {
                when (Skytils.config.pingDisplay) {
                    1 -> {
                        if (System.currentTimeMillis() - lastOldServerPing > 5000) {
                            lastOldServerPing = System.currentTimeMillis()
                            AccessorServerListEntryNormal.getPingerPool().submit {
                                oldServerPinger.ping(mc.currentServerData)
                            }
                        }
                        if (mc.currentServerData.pingToServer != -1L) pingCache =
                            mc.currentServerData.pingToServer.toDouble()
                    }
                    2 -> {
                        if (lastPingAt < 0 && (mc.currentScreen != null || !mc.thePlayer.hasMoved) && System.nanoTime()
                            - lastPingAt.absoluteValue > 1_000_000L * 5_000
                        ) {
                            sendPing()
                        }
                    }
                }
                if (pingCache != -1.0) {
                    fr.drawString(
                        "${NumberUtil.nf.format(pingCache.roundToPrecision(2))}ms",
                        0f,
                        0f,
                        when {
                            pingCache < 100 -> CommonColors.GREEN
                            pingCache < 250 -> CommonColors.ORANGE
                            else -> CommonColors.RED
                        },
                        SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                        SmartFontRenderer.TextShadow.NONE
                    )
                }
            }
        }

        override fun demoRender() {
            fr.drawString(
                "69.69ms",
                0f,
                0f,
                CommonColors.GREEN,
                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                SmartFontRenderer.TextShadow.NONE
            )
        }

        override val toggled: Boolean
            get() = Skytils.config.pingDisplay != 0
        override val height: Int
            get() = fr.FONT_HEIGHT
        override val width: Int
            get() = fr.getStringWidth("69.69ms")

        init {
            Skytils.guiManager.registerElement(this)
        }

    }

    init {
        PingDisplayElement()
    }
}