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
package gg.skytils.skytilsmod.mixins.hooks.model

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.features.impl.dungeons.solvers.BlazeSolver
import gg.skytils.skytilsmod.utils.bindColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

fun changeBlazeColor(
    entity: Entity,
    p_78088_2_: Float,
    p_78088_3_: Float,
    p_78088_4_: Float,
    p_78088_5_: Float,
    p_78088_6_: Float,
    scale: Float,
    ci: CallbackInfo
) {
    if (!Skytils.config.blazeSolver) return
    if (BlazeSolver.orderedBlazes.size == 0) return
    GlStateManager.disableTexture2D()
    GlStateManager.enableBlend()
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    if (BlazeSolver.blazeMode < 0) {
        if (entity == BlazeSolver.orderedBlazes[0].blaze) {
            Skytils.config.lowestBlazeColor.bindColor()
        } else if (Skytils.config.showNextBlaze && BlazeSolver.blazeMode != 0 && BlazeSolver.orderedBlazes.size > 1 && entity == BlazeSolver.orderedBlazes[1].blaze) {
            Skytils.config.nextBlazeColor.bindColor()
        }
    }
    if (BlazeSolver.blazeMode > 0) {
        if (entity == BlazeSolver.orderedBlazes[BlazeSolver.orderedBlazes.size - 1].blaze) {
            Skytils.config.highestBlazeColor.bindColor()
        } else if (Skytils.config.showNextBlaze && BlazeSolver.blazeMode != 0 && BlazeSolver.orderedBlazes.size > 1 && entity == BlazeSolver.orderedBlazes[BlazeSolver.orderedBlazes.size - 2].blaze) {
            val color = Skytils.config.nextBlazeColor
            color.bindColor()
        }
    }
}

fun renderModelBlazePost(
    entityIn: Entity,
    p_78088_2_: Float,
    p_78088_3_: Float,
    p_78088_4_: Float,
    p_78088_5_: Float,
    p_78088_6_: Float,
    scale: Float,
    ci: CallbackInfo
) {
    if (!Skytils.config.blazeSolver) return
    GlStateManager.disableBlend()
    GlStateManager.enableTexture2D()
}