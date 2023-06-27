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

package gg.skytils.skytilsmod.features.impl.dungeons.solvers.terminals

import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.features.impl.misc.Funny
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.block.BlockPressurePlateWeighted
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object ShootTheTargetSolver {
    private val positions = listOf(
        BlockPos(68, 130, 50), BlockPos(66, 130, 50), BlockPos(64, 130, 50),
        BlockPos(68, 128, 50), BlockPos(66, 128, 50), BlockPos(64, 128, 50),
        BlockPos(68, 126, 50), BlockPos(66, 126, 50), BlockPos(64, 126, 50)
    )
    private val plate = BlockPos(63, 127, 35)

    private val shot = arrayListOf<BlockPos>()

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!Utils.inDungeons || DungeonTimer.phase2ClearTime == -1L || !Skytils.config.shootTheTargetSolver) return
        val pos = event.pos
        val old = event.old
        val state = event.update
        if (positions.contains(pos)) {
            if (old.block == Blocks.emerald_block && state.block == Blocks.stained_hardened_clay) {
                shot.add(pos)
            }
        } else if (pos == plate && state.block is BlockPressurePlateWeighted) {
            if (state.getValue(BlockPressurePlateWeighted.POWER) == 0 || old !is BlockPressurePlateWeighted || old.getValue(
                    BlockPressurePlateWeighted.POWER
                ) == 0
            ) {
                shot.clear()
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Skytils.config.shootTheTargetSolver || shot.isEmpty()) return
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
        val matrixStack = UMatrixStack()

        for (pos in shot) {
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                matrixStack,
                AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                Color.RED,
                0.5f * Funny.alphaMult
            )
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onLoad(event: WorldEvent.Load) {
        shot.clear()
    }
}