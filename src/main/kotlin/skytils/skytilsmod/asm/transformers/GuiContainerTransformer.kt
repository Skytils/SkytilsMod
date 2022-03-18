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

package skytils.skytilsmod.asm.transformers

import dev.falsehonesty.asmhelper.dsl.modify
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

fun injectContainerCheck() = modify("net.minecraft.client.gui.inventory.GuiContainer") {
    classNode.methods.find { it.name == "<init>" }!!.apply {
        for (insn in instructions) {
            if (insn.opcode == Opcodes.RETURN) {
                instructions.insertBefore(insn, VarInsnNode(Opcodes.ALOAD, 0))
                instructions.insertBefore(
                    insn,
                    MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        "skytils/skytilsmod/utils/ContainerCheck",
                        "check",
                        "(Lnet/minecraft/client/gui/inventory/GuiContainer;)V",
                        false
                    )
                )
            }
        }
    }
}