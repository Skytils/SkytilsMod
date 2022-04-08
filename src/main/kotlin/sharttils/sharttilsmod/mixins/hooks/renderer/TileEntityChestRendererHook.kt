/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.mixins.hooks.renderer

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.tileentity.TileEntityChest
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import sharttils.sharttilsmod.features.impl.dungeons.solvers.ThreeWeirdosSolver
import sharttils.sharttilsmod.utils.bindColor
import java.awt.Color

fun setChestColor(
    te: TileEntityChest,
    x: Double,
    y: Double,
    z: Double,
    partialTicks: Float,
    destroyStage: Int,
    ci: CallbackInfo
) {
    if (te.pos == ThreeWeirdosSolver.riddleChest) {
        Color.RED.bindColor()
        GlStateManager.disableTexture2D()
    }
}

fun setChestColorPost(
    te: TileEntityChest,
    x: Double,
    y: Double,
    z: Double,
    partialTicks: Float,
    destroyStage: Int,
    ci: CallbackInfo
) {
    GlStateManager.enableTexture2D()
}