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

package gg.skytils.skytilsmod.mixins.hooks.renderer

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack


//#if MC==10809
//$$ import net.minecraft.item.SwordItem
//$$
//$$ fun getItemInUseCountForFirstPerson(player: AbstractClientPlayerEntity, item: ItemStack, original: Operation<Int>? = null): Int {
//$$    if (Skytils.config.disableBlockAnimation && Utils.inSkyblock && item.item is SwordItem && player.method_0_7992() <= 7) return 0
//$$    return original?.call(player) ?: player.method_0_7990()
//$$ }
//#endif

fun modifySize(matrixStack: MatrixStack) {
    val scale = Skytils.config.itemScale * if (SuperSecretSettings.twilightGiant) 5f else 1f
    matrixStack.scale(scale, scale, scale)
    if (scale < 1) {
        val offset = 1 - scale
        matrixStack.translate(-offset, offset, 0f)
    }
}