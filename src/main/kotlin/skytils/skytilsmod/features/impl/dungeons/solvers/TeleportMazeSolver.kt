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
package skytils.skytilsmod.features.impl.dungeons.solvers

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils

class TeleportMazeSolver {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (!Skytils.config.teleportMazeSolver || !Utils.inDungeons || !DungeonListener.missingPuzzles.contains("Teleport Maze")) return
        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.posY < 68 || mc.thePlayer.posY > 75) return
        val groundBlock = BlockPos(mc.thePlayer.posX, 69.0, mc.thePlayer.posZ)
        val state = mc.theWorld.getBlockState(groundBlock)
        if (state.block == Blocks.stone_slab) {
            if (lastTpPos != null) {
                if (BlockPos.getAllInBox(lastTpPos, groundBlock).any {
                        mc.theWorld.getBlockState(it).block === Blocks.iron_bars
                    }) {
                    Utils.getBlocksWithinRangeAtSameY(lastTpPos!!, 1, 69).find {
                        mc.theWorld.getBlockState(it).block === Blocks.end_portal_frame
                    }?.let {
                        steppedPads.add(it)
                    }
                    Utils.getBlocksWithinRangeAtSameY(groundBlock, 1, 69).find {
                        mc.theWorld.getBlockState(it).block === Blocks.end_portal_frame
                    }?.let {
                        steppedPads.add(it)
                    }
                }
            }
            lastTpPos = groundBlock
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Skytils.config.teleportMazeSolver || steppedPads.isEmpty() || !DungeonListener.missingPuzzles.contains("Teleport Maze")) return
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)

        for (pos in steppedPads) {
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                Skytils.config.teleportMazeSolverColor,
                1f
            )
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        steppedPads.clear()
        lastTpPos = null
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        private val steppedPads = HashSet<BlockPos>()
        private var lastTpPos: BlockPos? = null
    }
}