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

import dev.falsehonesty.asmhelper.AsmHelper
import dev.falsehonesty.asmhelper.dsl.instructions.Descriptor
import dev.falsehonesty.asmhelper.dsl.instructions.InsnListBuilder
import dev.falsehonesty.asmhelper.dsl.modify
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import skytils.skytilsmod.asm.SkytilsTransformer
import skytils.skytilsmod.utils.descriptor
import skytils.skytilsmod.utils.getOrSelf

fun changeRenderedName() = modify("net.minecraft.client.renderer.entity.RendererLivingEntity") {
    classNode.apply {
        this.methods.find {
            SkytilsTransformer.methodMaps.getOrSelf(
                AsmHelper.remapper.remapMethodName(
                    "net/minecraft/client/renderer/entity/RendererLivingEntity",
                    it.name,
                    it.desc
                )
            ) == "renderName" && AsmHelper.remapper.remapDesc(it.desc) == "(Lnet/minecraft/entity/EntityLivingBase;DDD)V"
        }?.apply {
            for (insn in instructions) {
                if (insn is VarInsnNode && insn.opcode == Opcodes.ASTORE) {
                    var prev = insn.previous
                    if (prev is MethodInsnNode && prev.opcode == Opcodes.INVOKEINTERFACE && prev.descriptor == Descriptor(
                            "net/minecraft/util/IChatComponent",
                            "getFormattedText",
                            "()Ljava/lang/String;"
                        )
                    ) {
                        prev = prev.previous
                        if (prev is MethodInsnNode && prev.opcode == Opcodes.INVOKEVIRTUAL && prev.descriptor == Descriptor(
                                "net/minecraft/entity/EntityLivingBase",
                                "getDisplayName",
                                "()Lnet/minecraft/util/IChatComponent;"
                            )
                        ) {
                            prev = prev.previous
                            if (prev is VarInsnNode && prev.opcode == Opcodes.ALOAD && prev.`var` == 1) {
                                instructions.insert(insn, InsnListBuilder(this).apply {
                                    invokeStatic(
                                        "skytils/skytilsmod/mixins/hooks/renderer/RendererLivingEntityHookKt",
                                        "replaceEntityName",
                                        "(Lnet/minecraft/entity/EntityLivingBase;Ljava/lang/String;)Ljava/lang/String;"
                                    ) {
                                        aload(1)
                                        aload(insn.`var`)
                                    }
                                    astore(insn.`var`)
                                }.build())
                                break
                            }
                        }
                    }
                }
            }
        }
    }
}