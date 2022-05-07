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

package gg.skytils.skytilsmod.asm.transformers

import dev.falsehonesty.asmhelper.dsl.modify
import gg.skytils.skytilsmod.utils.matches
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode

fun fixSBADungeonCheck() = modify("codes/biscuit/skyblockaddons/utils/Utils") {
    classNode.methods.find { it.name == "parseSidebar" }?.apply {
        for (insn in instructions) {
            if (insn is LdcInsnNode && insn.cst == "Dungeon Cleared: ") {
                insn.cst = "Cleared: "
                val next = insn.next
                if (next is MethodInsnNode && next.matches("java/lang/String", "contains", null)) {
                    instructions.set(
                        next,
                        MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            "java/lang/String",
                            "startsWith",
                            "(Ljava/lang/String;)Z",
                            false
                        )
                    )
                }
                println("Skytils patched SBA Utils method check")
                break
            }
        }
    }
}