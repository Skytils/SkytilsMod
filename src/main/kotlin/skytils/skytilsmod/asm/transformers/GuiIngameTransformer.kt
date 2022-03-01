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
        var injectedWidth = false
        for (insn in instructions) {
            var prev = insn.previous ?: continue
            if (!injectedWidth) {
                if (lastAppendInsn == null && prev is LdcInsnNode && prev.cst == ": " && insn is MethodInsnNode && insn.name == "append" && insn.owner == "java/lang/StringBuilder") {
                    lastAppendInsn = insn
                }
                if (lastAppendInsn != null && insn is MethodInsnNode && insn.name == "append" && insn.owner == "java/lang/StringBuilder") {
                    if (!isScorePoints(prev)) continue
                    val labela = LabelNode()

                    instructions.insert(lastAppendInsn, insertConfigCheck(labela))

                    instructions.insert(insn, labela)
                    injectedWidth = true
                }
            } else if (insn is VarInsnNode && insn.opcode == Opcodes.ASTORE && prev is MethodInsnNode && prev.name == "toString" && prev.owner == "java/lang/StringBuilder") {
                prev = prev.previous ?: continue
                if (prev !is MethodInsnNode || !prev.matches(
                        "java/lang/StringBuilder",
                        "append",
                        null
                    )
                ) continue
                prev = prev.previous ?: continue
                if (!isScorePoints(prev)) continue
                val labela = LabelNode()
                val list = insertConfigCheck(labela, true)
                list.add(LdcInsnNode(""))
                list.add(VarInsnNode(Opcodes.ASTORE, insn.`var`))
                list.add(labela)
                instructions.insert(insn, list)
                break
            }
        }
    }
}

private fun isScorePoints(insn: AbstractInsnNode): Boolean {
    return insn is MethodInsnNode && insn.opcode == Opcodes.INVOKEVIRTUAL && (insn.matches(
        "aum",
        "c",
        "()I"
    ) || insn.matches(
        "net/minecraft/scoreboard/Score",
        "getScorePoints",
        "()I"
    ))
}

private fun insertConfigCheck(labela: LabelNode, isSecond: Boolean = false): InsnList {
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
    if (!isSecond) insnList.add(JumpInsnNode(Opcodes.IFEQ, label))
    else insnList.add(JumpInsnNode(Opcodes.IFEQ, labela))
    insnList.add(
        FieldInsnNode(
            Opcodes.GETSTATIC,
            "skytils/skytilsmod/utils/Utils",
            "isOnHypixel",
            "Z"
        )
    )
    if (!isSecond) insnList.add(JumpInsnNode(Opcodes.IFNE, labela))
    else insnList.add(JumpInsnNode(Opcodes.IFEQ, labela))
    if (!isSecond) insnList.add(label)

    return insnList
}