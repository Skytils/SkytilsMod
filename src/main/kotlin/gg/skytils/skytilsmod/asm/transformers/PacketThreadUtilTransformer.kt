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

package gg.skytils.skytilsmod.asm.transformers

import dev.falsehonesty.asmhelper.dsl.instructions.InsnListBuilder
import dev.falsehonesty.asmhelper.dsl.instructions.JumpCondition
import dev.falsehonesty.asmhelper.dsl.modify
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import net.minecraft.network.INetHandler
import net.minecraft.network.Packet
import org.objectweb.asm.Opcodes

fun insertReceivePacketEvent() = modify("net/minecraft/network/PacketThreadUtil$1") {
    classNode.visitField(
        Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL,
        "skytils\$handler",
        "Lnet/minecraft/network/INetHandler;",
        null,
        null
    ).visitEnd()
    classNode.visitField(
        Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL,
        "skytils\$packet",
        "Lnet/minecraft/network/Packet;",
        null,
        null
    ).visitEnd()
    classNode.methods.find { it.name == "<init>" }?.apply {
        instructions.insert(InsnListBuilder(this).apply {
            aload(0)
            aload(1)
            putField(classNode.name, "skytils\$packet", "Lnet/minecraft/network/Packet;")
            aload(0)
            aload(2)
            putField(classNode.name, "skytils\$handler", "Lnet/minecraft/network/INetHandler;")
        }.build())
    }
    findMethod("run", "()V").apply {
        instructions.insert(InsnListBuilder(this).apply {
            invokeStatic(
                "gg/skytils/skytilsmod/asm/transformers/PacketThreadUtilTransformer",
                "postEvent",
                "(Lnet/minecraft/network/INetHandler;Lnet/minecraft/network/Packet;)Z"
            ) {
                aload(0)
                getField(
                    "net/minecraft/network/PacketThreadUtil$1",
                    "skytils\$handler",
                    "Lnet/minecraft/network/INetHandler;"
                )
                aload(0)
                getField(
                    "net/minecraft/network/PacketThreadUtil$1",
                    "skytils\$packet",
                    "Lnet/minecraft/network/Packet;"
                )
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