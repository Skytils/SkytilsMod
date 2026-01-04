/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.utils.multiplatform

import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemStack
import kotlin.jvm.optionals.getOrNull

val ItemStack.nbt
    get() =
        if (isEmpty) null
        //#if MC>=12110
        //$$ else components.get(net.minecraft.component.DataComponentTypes.CUSTOM_DATA)?.copyNbt()
        //#else
        else MinecraftClient.getInstance().player?.registryManager?.let(::toNbt)?.asCompound()?.getOrNull()
        //#endif
