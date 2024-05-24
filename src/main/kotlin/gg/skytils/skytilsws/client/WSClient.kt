/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsws.client

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsws.shared.SkytilsWS
import gg.skytils.skytilsws.shared.packet.C2SPacketConnect
import gg.skytils.skytilsws.shared.packet.Packet
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.ExperimentalSerializationApi
import java.util.zip.Deflater

object WSClient {
    var session: DefaultClientWebSocketSession? = null
    val wsClient by lazy {
        HttpClient(CIO) {
            install(UserAgent) {
                agent = "Skytils/${Skytils.VERSION} SkytilsWS/${SkytilsWS.version}"
            }

            install(WebSockets) {
                pingInterval = 59_000L
                @OptIn(ExperimentalSerializationApi::class)
                contentConverter = KotlinxWebsocketSerializationConverter(SkytilsWS.packetSerializer)
                extensions {
                    install(WebSocketDeflateExtension) {
                        compressionLevel = Deflater.DEFAULT_COMPRESSION
                        compressIfBiggerThan(bytes = 4 * 1024)
                    }
                }
            }

            engine {
                endpoint {
                    connectTimeout = 10000
                    keepAliveTime = 60000
                }
                https {
                    trustManager = Skytils.trustManager
                }
            }
        }
    }

    suspend fun openConnection() {
        if (session != null) error("Session already open")

        wsClient.webSocketSession(System.getProperty("skytils.websocketURL", "wss://ws.skytils.gg/ws")).apply {
            session = this
            try {
                sendSerialized<Packet>(C2SPacketConnect(SkytilsWS.version, Skytils.VERSION))
                while (true) {
                    val packet = receiveDeserialized<Packet>()
                    PacketHandler.processPacket(this@apply, packet)
                }
            } catch(e: ClosedReceiveChannelException) {
                e.printStackTrace()
                closeExceptionally(e)
            } catch (e: Throwable) {
                e.printStackTrace()
                closeExceptionally(e)
            } finally {
                session = null
            }
        }
    }

    suspend fun closeConnection() {
        session?.close(CloseReason(CloseReason.Codes.NORMAL, "Client closed connection"))
    }

    suspend fun sendPacket(packet: Packet) {
        session?.sendSerialized(packet) ?: error("Tried to send packet but session was null")
    }
}