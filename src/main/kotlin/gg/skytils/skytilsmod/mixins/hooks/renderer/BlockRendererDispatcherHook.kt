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
import gg.skytils.skytilsmod.features.impl.farming.GardenFeatures
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.SkyblockIsland
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.equalsAnyOf
import net.minecraft.block.BlockCarpet
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.BlockRendererDispatcher
import net.minecraft.client.resources.model.IBakedModel
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

fun modifyGetModelFromBlockState(
    blockRendererDispatcher: BlockRendererDispatcher,
    state: IBlockState?,
    worldIn: IBlockAccess,
    pos: BlockPos?,
    cir: CallbackInfoReturnable<IBakedModel>
) {
    if (!Utils.inSkyblock || state == null || pos == null) return
    var returnState = state
    if (SBInfo.mode == SkyblockIsland.DwarvenMines.mode) {
        if (Skytils.config.recolorCarpets && state.block === Blocks.carpet && state.getValue(
                BlockCarpet.COLOR
            ).equalsAnyOf(
                EnumDyeColor.GRAY, EnumDyeColor.LIGHT_BLUE, EnumDyeColor.YELLOW
            )
        ) {
            returnState = state.withProperty(BlockCarpet.COLOR, EnumDyeColor.RED)
        } else if (Skytils.config.darkModeMist && pos.y <= 76) {
            if (state.block === Blocks.stained_glass &&
                state.getValue(BlockStainedGlass.COLOR) == EnumDyeColor.WHITE
            ) {
                returnState = state.withProperty(BlockStainedGlass.COLOR, EnumDyeColor.GRAY)
            } else if (state.block === Blocks.carpet && state.getValue(BlockCarpet.COLOR) == EnumDyeColor.WHITE) {
                returnState = state.withProperty(BlockCarpet.COLOR, EnumDyeColor.GRAY)
            }
        }
    } else if (Skytils.config.gardenPlotCleanupHelper && GardenFeatures.isCleaningPlot && GardenFeatures.trashBlocks.contains(
            state.block
        )
    ) {
        returnState = Blocks.sponge.defaultState
    }

    if (returnState !== state) {
        cir.returnValue = blockRendererDispatcher.blockModelShapes.getModelForState(returnState)
    }
}