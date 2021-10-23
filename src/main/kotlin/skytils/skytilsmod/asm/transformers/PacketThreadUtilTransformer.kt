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

package skytils.skytilsmod.asm.transformers

import dev.falsehonesty.asmhelper.dsl.instructions.InsnListBuilder
import dev.falsehonesty.asmhelper.dsl.instructions.JumpCondition
import dev.falsehonesty.asmhelper.dsl.modify
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import skytils.skytilsmod.events.impl.MainReceivePacketEvent

fun insertReceivePacketEvent() = modify("net/minecraft/network/PacketThreadUtil$1") {
    val netHandler = classNode.fields.find { it.desc == "Lnet/minecraft/network/INetHandler;" || it.desc == "Lep;" }
        ?: error("couldn't find INetHandler field")
    val packet =
        classNode.fields.find { it.desc == "Lnet/minecraft/network/Packet;" || it.desc == "Lff;" }
            ?: error("couldn't find Packet field")
    findMethod("run", "()V").apply {
        instructions.insert(InsnListBuilder(this).apply {
            invokeStatic(
                "skytils/skytilsmod/asm/transformers/PacketThreadUtilTransformer",
                "postEvent",
                "(Lnet/minecraft/network/INetHandler;Lnet/minecraft/network/Packet;)Z"
            ) {
                aload(0)
                getField("net/minecraft/network/PacketThreadUtil$1", netHandler.name, netHandler.desc)
                aload(0)
                getField("net/minecraft/network/PacketThreadUtil$1", packet.name, packet.desc)
            }
            ifClause(JumpCondition.EQUAL) {
                methodReturn()
            }
        }.build())
    }
}

object PacketThreadUtilTransformer {
    @JvmStatic
    fun postEvent(netHandler: INetHandler, packet: Packet<INetHandler>): Boolean {
        return MainReceivePacketEvent(
            netHandler,
            packet
        ).postAndCatch()
    }
}