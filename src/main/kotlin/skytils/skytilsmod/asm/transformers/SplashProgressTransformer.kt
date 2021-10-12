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
import dev.falsehonesty.asmhelper.dsl.modify
import net.minecraft.util.ResourceLocation
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode
import skytils.skytilsmod.utils.Utils
import kotlin.random.Random

fun injectSplashProgressTransformer() = modify("net.minecraftforge.fml.client.SplashProgress") {
    findMethod("start", "()V").apply {
        val v = localVariables.find {
            it.name == "forgeLoc" && (it.desc == "Ljy;" || it.desc == "Lnet/minecraft/util/ResourceLocation;")
        } ?: return@modify println("unable to find localvar")
        var index = -1
        for (insn in instructions) {
            if (insn is MethodInsnNode) {
                if (insn.owner == "net/minecraftforge/fml/client/SplashProgress" && insn.name == "getMaxTextureSize") {
                    val list = InsnListBuilder(this).apply {
                        aload(v.index)
                        invokeStatic(
                            "skytils/skytilsmod/asm/transformers/SplashProgressTransformer",
                            "setForgeGif",
                            "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/util/ResourceLocation;"
                        )
                        astore().also {
                            index = it.index
                        }
                    }
                    instructions.insertBefore(insn, list.build());
                }
                if (insn.owner == "net/minecraftforge/fml/client/SplashProgress$3" && insn.name == "<init>") {
                    if (index == -1) {
                        println("Failed to inject local variable, breaking")
                        break
                    }
                    instructions.remove(insn.previous)
                    instructions.insertBefore(insn, VarInsnNode(Opcodes.ALOAD, index))
                }
            }
        }
    }
}

object SplashProgressTransformer {
    val gifs = mapOf(
        0.0 to ResourceLocation("skytils", "sychicpet.gif"),
        88.5 to ResourceLocation("skytils", "sychiccat.png"),
        94.5 to ResourceLocation("skytils", "breefingdog.png"),
        96.0 to ResourceLocation("skytils", "azoopet.gif"),
        99.0 to ResourceLocation("skytils", "abdpfp.gif"),
        // this is the chance of winning the jackpot on the lottery, but even less due to a loss of precision
        100 - 100 * 1 / 302_575_350.0 to ResourceLocation("skytils", "jamcat.gif")
    )

    @JvmStatic
    fun setForgeGif(resourceLocation: ResourceLocation): ResourceLocation {
        if (Utils.noSychic) return resourceLocation

        if (Utils.breefingdog) return ResourceLocation("skytils", "breefingdog.png")
        else {
            val weight = Random.nextDouble() * 100
            return (gifs.entries.reversed().find { weight >= it.key }?.value ?: ResourceLocation(
                "skytils",
                "sychicpet.gif"
            )).also {
                println("Rolled a $weight, displaying ${it.resourcePath}")
            }
        }
    }
}