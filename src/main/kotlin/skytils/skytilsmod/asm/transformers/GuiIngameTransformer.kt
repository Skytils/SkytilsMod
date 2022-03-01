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
import org.objectweb.asm.tree.*
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.matches

fun injectScoreboardScoreRemover() = modify("net/minecraft/client/gui/GuiIngame") {
    classNode.methods.find {
        Utils.equalsOneOf(it.name, "renderScoreboard", "a") && Utils.equalsOneOf(
            it.desc,
            "(Lauk;Lavr;)V",
            "(Lnet/minecraft/scoreboard/ScoreObjective;Lnet/minecraft/client/gui/ScaledResolution;)V"
        )
    }?.apply {
        var lastAppendInsn: MethodInsnNode? = null
        for (insn in instructions) {
            val prev = insn.previous ?: continue
            if (lastAppendInsn == null && prev is LdcInsnNode && prev.cst == ": " && insn is MethodInsnNode && insn.name == "append" && insn.owner == "java/lang/StringBuilder") {
                lastAppendInsn = insn
            }
            if (lastAppendInsn != null && insn is MethodInsnNode && insn.name == "append" && insn.owner == "java/lang/StringBuilder") {
                if (prev !is MethodInsnNode || !prev.matches("aum", "c", "()I") && !prev.matches(
                        "net/minecraft/scoreboard/Score",
                        "getScorePoints",
                        "()I"
                    )
                ) continue
                val label = LabelNode()
                val insnList = InsnList()

                insnList.add(
                    FieldInsnNode(
                        Opcodes.GETSTATIC,
                        "skytils/skytilsmod/core/Config",
                        "INSTANCE",
                        "Lskytils/skytilsmod/core/Config;"
                    )
                )
                insnList.add(
                    MethodInsnNode(
                        Opcodes.INVOKEVIRTUAL,
                        "skytils/skytilsmod/core/Config",
                        "getHideScoreboardScore",
                        "()Z",
                        false
                    )
                )
                insnList.add(JumpInsnNode(Opcodes.IFEQ, label))
                insnList.add(
                    FieldInsnNode(
                        Opcodes.GETSTATIC,
                        "skytils/skytilsmod/utils/Utils",
                        "isOnHypixel",
                        "Z"
                    )
                )
                insnList.add(JumpInsnNode(Opcodes.IFEQ, label))
                instructions.insert(lastAppendInsn, insnList)

                instructions.insert(insn, label)
                break
            }
        }
    }
}