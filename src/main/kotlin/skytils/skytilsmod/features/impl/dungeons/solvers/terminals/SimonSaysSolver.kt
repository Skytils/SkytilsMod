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
package skytils.skytilsmod.features.impl.dungeons.solvers.terminals

import net.minecraft.block.BlockButtonStone
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.BlockChangeEvent
import skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import java.awt.Color

class SimonSaysSolver {
    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        val pos = event.pos
        val old = event.old
        val state = event.update
        if (Utils.inDungeons) {
            if (Skytils.config.simonSaysSolver && DungeonFeatures.dungeonFloor == "F7" && DungeonTimer.phase2ClearTime != -1L && DungeonTimer.phase3ClearTime == -1L) {
                if (pos.y in 120..123 && pos.z >= 291 && pos.z <= 294) {
                    if (pos.x == 310) {
                        //println("Block at $pos changed to ${state.block.localizedName} from ${old.block.localizedName}")
                        if (state.block === Blocks.sea_lantern) {
                            if (!clickInOrder.contains(pos)) {
                                clickInOrder.add(pos)
                            }
                        }
                    } else if (pos.x == 309) {
                        if (state.block === Blocks.air) {
                            //println("Buttons on simon says were removed!")
                            clickNeeded = 0
                        } else if (state.block === Blocks.stone_button) {
                            if (old.block === Blocks.stone_button) {
                                if (state.getValue(BlockButtonStone.POWERED)) {
                                    //println("Button on simon says was pressed")
                                    clickNeeded++
                                }
                            }
                        }
                    }
                } else if (pos == BlockPos(309, 121, 290)) {
                    if (state.block === Blocks.stone_button) {
                        if (state.getValue(BlockButtonStone.POWERED)) {
                            //println("Simon says was started")
                            clickInOrder.clear()
                            clickNeeded = 0
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)

        if (Skytils.config.simonSaysSolver && clickNeeded < clickInOrder.size) {
            val pos = clickInOrder[clickNeeded].west()
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1),
                if (clickNeeded == clickInOrder.size - 1) Color(0, 255, 0) else Color(255, 0, 0),
                0.5f
            )
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        clickInOrder.clear()
        clickNeeded = 0
    }

    companion object {
        private val clickInOrder = ArrayList<BlockPos>()
        private var clickNeeded = 0
    }
}