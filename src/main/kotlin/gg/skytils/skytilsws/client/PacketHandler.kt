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

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Reference
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.Room
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.Unknown
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonInfo
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.ScanUtils
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsws.shared.IPacketHandler
import gg.skytils.skytilsws.shared.SkytilsWS
import gg.skytils.skytilsws.shared.packet.*
import io.ktor.websocket.*
import kotlinx.coroutines.coroutineScope
import java.util.*

object PacketHandler : IPacketHandler {
    suspend fun handleLogin(session: WebSocketSession, packet: S2CPacketAcknowledge) {
        val serverId = UUID.randomUUID().toString().replace("-".toRegex(), "")
        mc.sessionService.joinServer(
            //#if MC==10809
            //$$ mc.session.profile,
            //#else
            mc.session.uuidOrNull,
            //#endif
            mc.session.accessToken, serverId)
        WSClient.sendPacket(C2SPacketLogin(mc.session.username,
            //#if MC==10809
            //$$ mc.session.profile.id.toString(),
            //#else
            mc.session.uuidOrNull!!.toString(),
            //#endif
            Reference.VERSION, SkytilsWS.version, serverId))
    }

    override suspend fun processPacket(session: WebSocketSession, packet: Packet) {
        println("Received packet: $packet")
        when (packet) {
            is S2CPacketAcknowledge -> {
                if (packet.wsVersion != SkytilsWS.version) {
                    session.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Incompatible WS version"))
                    UChat.chat("${Skytils.failPrefix} §cIncompatible WS version. Expected ${packet.wsVersion} but you have protocol version ${SkytilsWS.version}.")
                } else {
                    coroutineScope {
                        handleLogin(session, packet)
                    }
                }
            }
            is S2CPacketDungeonRoomSecret -> {
                DungeonInfo.uniqueRooms[packet.roomId]?.let {
                    if (packet.secretCount > (it.foundSecrets ?: -1)) {
                        it.foundSecrets = packet.secretCount
                    }
                }
            }
            is S2CPacketDungeonRoom -> {
                val room = DungeonInfo.dungeonList[packet.row * 11 + packet.col]
                if (room is Unknown || (room as? Room)?.data?.name == "Unknown") {
                    val data = ScanUtils.roomList.find { it.name == packet.roomId }
                    DungeonInfo.dungeonList[packet.row * 11 + packet.col] = Room(packet.x, packet.z, data ?: return).apply {
                        isSeparator = packet.isSeparator
                        core = packet.core
                        addToUnique(packet.row, packet.col)
                    }
                }
            }
            is S2CPacketDungeonMimic -> {
                ScoreCalculation.mimicKilled.set(true)
            }
            is S2CPacketJerryMayor -> {
                MayorInfo.jerryMayor = MayorInfo.mayorData.find { it.name == packet.mayor }
                MayorInfo.newJerryPerks = packet.endTime
                if (MayorInfo.currentMayor != "Jerry") {
                    MayorInfo.fetchMayorData()
                }
            }
            else -> {
                session.close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Unknown packet type"))
            }
        }
    }
}