/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2023 Skytils
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

import dev.falsehonesty.asmhelper.dsl.At
import dev.falsehonesty.asmhelper.dsl.InjectionPoint
import dev.falsehonesty.asmhelper.dsl.inject

fun fixUTextComponentSerialize() = inject {
    className = "net.minecraft.util.IChatComponent\$Serializer"
    methodName = "serialize"
    methodDesc =
        "(Lnet/minecraft/util/IChatComponent;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;"
    at = At(InjectionPoint.HEAD)

    insnList {
        invokeStatic(
            "gg/skytils/skytilsmod/mixins/hooks/util/IChatComponent_SerializerHookKt",
            "fixUTextComponentSerialize",
            "(Lnet/minecraft/util/IChatComponent;)Lnet/minecraft/util/IChatComponent;"
        ) {
            aload(1)
        }
        astore(1)
    }
}