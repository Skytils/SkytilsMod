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
import dev.falsehonesty.asmhelper.dsl.instructions.Local
import dev.falsehonesty.asmhelper.dsl.modify
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.matches
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.*

fun injectScoreboardScoreRemover() = modify("net/minecraft/client/gui/GuiIngame") {
    classNode.methods.find {
        Utils.equalsOneOf(it.name, "renderScoreboard", "a") && Utils.equalsOneOf(
            it.desc,
            "(Lauk;Lavr;)V",
            "(Lnet/minecraft/scoreboard/ScoreObjective;Lnet/minecraft/client/gui/ScaledResolution;)V"
        )
    }?.apply {
        var checkVar: Local
        instructions.insert(InsnListBuilder(this).apply {
            iconst_0()
            checkVar = istore()
            val label = makeLabel()
            getStatic("gg/skytils/skytilsmod/utils/Utils", "isOnHypixel", "Z")
            jump(JumpCondition.EQUAL, label)
            getKObjectInstance("gg/skytils/skytilsmod/core/Config")
            invokeVirtual("gg/skytils/skytilsmod/core/Config", "getHideScoreboardScore", "()Z")
            jump(JumpCondition.EQUAL, label)

            iconst_1()
            istore(checkVar.index)
            insn(label)
        }.build())
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
                    val label = LabelNode()

                    instructions.insert(lastAppendInsn, InsnList().apply {
                        add(VarInsnNode(Opcodes.ILOAD, checkVar.index))
                        add(JumpInsnNode(Opcodes.IFNE, label))
                    })

                    instructions.insert(insn, label)
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
                instructions.insert(insn, InsnList().apply {
                    val label = LabelNode()
                    add(VarInsnNode(Opcodes.ILOAD, checkVar.index))
                    add(JumpInsnNode(Opcodes.IFEQ, label))
                    add(LdcInsnNode(""))
                    add(VarInsnNode(Opcodes.ASTORE, insn.`var`))
                    add(label)
                })
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