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

package skytils.skytilsmod.utils

import net.minecraft.item.ItemStack
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

object NEUCompatibility {
    var isCustomAHActive = false
    var isStorageMenuActive = false
    var isTradeWindowActive = false

    val drawItemStackMethod: MethodHandle by lazy {
        try {
            MethodHandles.publicLookup().findStatic(
                Class.forName("io.github.moulberry.notenoughupdates.util.Utils"),
                "drawItemStack",
                MethodType.methodType(
                    Void.TYPE,
                    ItemStack::class.java,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
            )
        } catch (e: Throwable) {
            throw IllegalStateException(
                "Skytils couldn't locate an NEU function. Please make sure you have the latest version of NEU.",
                e
            )
        }
    }
}