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

import dev.falsehonesty.asmhelper.dsl.modify
import net.minecraft.entity.Entity
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode

fun addColoredNamesCheck() = modify("me.Danker.features.ColouredNames") {
    findMethod("onRenderLiving", "(Lnet.minecraftforge.client.event.RenderLivingEvent.Specials.Pre;)V").also { n ->
        for (insn in n.instructions) {
            if (insn is MethodInsnNode) {
                if (insn.owner == "net/minecraft/entity/Entity" && insn.name == "func_145818_k_" && insn.desc == "()Z") {
                    n.instructions.insert(
                        insn,
                        MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "skytils/skytilsmod/asm/transformers/DSMColoredNamesTransformer",
                            "modifyColoredNamesCheck",
                            "(Lnet/minecraft/entity/Entity;)Z"
                        )
                    )
                    n.instructions.remove(insn)
                    break
                }
            }
        }
    }
}

object DSMColoredNamesTransformer {
    @JvmStatic
    fun modifyColoredNamesCheck(entity: Entity): Boolean {
        val customName = entity.customNameTag
        return customName.isNotEmpty() && !customName.endsWith("§c❤") && !customName
            .endsWith(" Hits")
    }
}