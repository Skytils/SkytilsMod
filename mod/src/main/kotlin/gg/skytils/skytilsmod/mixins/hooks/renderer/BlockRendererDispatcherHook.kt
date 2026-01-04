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
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.client.render.block.BlockRenderManager
import net.minecraft.client.render.chunk.ChunkRendererRegion
import net.minecraft.util.math.BlockPos

fun modifyBlockState(
    blockRenderManager: BlockRenderManager,
    instance: ChunkRendererRegion,
    pos: BlockPos,
    original: BlockState
): BlockState {
    return modifyBlockState(pos, original)
}

fun modifyBlockState(pos: BlockPos, original: BlockState): BlockState {
    if (!Utils.inSkyblock) return original
    var returnState = original
    if (SBInfo.mode == SkyblockIsland.DwarvenMines.mode) {
        if (Skytils.config.recolorCarpets && Utils.equalsOneOf(
                original.block,
                Blocks.GRAY_CARPET, Blocks.LIGHT_BLUE_CARPET, Blocks.YELLOW_CARPET
            )
        ) {
            returnState = Blocks.RED_CARPET.defaultState
        } else if (Skytils.config.darkModeMist && pos.y <= 76) {
            if (original.block === Blocks.WHITE_STAINED_GLASS
            ) {
                returnState = Blocks.GRAY_STAINED_GLASS.defaultState
            } else if (original.block === Blocks.WHITE_CARPET) {
                returnState = Blocks.GRAY_CARPET.defaultState
            }
        }
    } else if (Skytils.config.gardenPlotCleanupHelper && GardenFeatures.isCleaningPlot && GardenFeatures.trashBlocks.contains(
            original.block
        )
    ) {
        returnState = Blocks.SPONGE.defaultState
    }

    return returnState
}