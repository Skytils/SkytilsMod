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

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.utils.Utils
import com.mojang.blaze3d.systems.RenderSystem
import gg.essential.universal.UMatrixStack
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.ItemEntity
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

fun scaleItemDrop(
    matrixStack: MatrixStack
) {
    if (!Utils.inSkyblock) return
    val scale = Skytils.config.itemDropScale
    matrixStack.scale(scale, scale, scale)
}