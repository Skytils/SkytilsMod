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
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.features.impl.misc.Funny
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.block.BlockButtonStone
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object SimonSaysSolver {
    val startBtn = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var clickNeeded = 0

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        val pos = event.pos
        val old = event.old
        val state = event.update
        if (Utils.inDungeons) {
            if (Skytils.config.simonSaysSolver && Utils.equalsOneOf(
                    DungeonFeatures.dungeonFloor,
                    "F7",
                    "M7"
                ) && DungeonTimer.phase2ClearTime != -1L && DungeonTimer.phase3ClearTime == -1L
            ) {
                if ((pos.y in 120..123) && pos.z in 92..95) {
                    if (pos.x == 111) {
                        //println("Block at $pos changed to ${state.block.localizedName} from ${old.block.localizedName}")
                        if (state.block === Blocks.sea_lantern) {
                            if (!clickInOrder.contains(pos)) {
                                clickInOrder.add(pos)
                            }
                        }
                    } else if (pos.x == 110) {
                        if (state.block === Blocks.air) {
                            //println("Buttons on simon says were removed!")
                            clickNeeded = 0
                            clickInOrder.clear()
                        } else if (state.block === Blocks.stone_button) {
                            if (old.block === Blocks.stone_button) {
                                if (state.getValue(BlockButtonStone.POWERED)) {
                                    //println("Button on simon says was pressed")
                                    clickNeeded++
                                }
                            }
                        }
                    }
                } else if ((pos == startBtn && state.block === Blocks.stone_button && state.getValue(BlockButtonStone.POWERED)) || Funny.ticks == 180) {
                    //println("Simon says was started")
                    clickInOrder.clear()
                    clickNeeded = 0
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)

        if (Skytils.config.simonSaysSolver && clickNeeded < clickInOrder.size) {
            val matrixStack = UMatrixStack()

            for (i in clickNeeded until clickInOrder.size) {
                val pos = clickInOrder[i]
                val x = pos.x - viewerX
                val y = pos.y - viewerY + .372
                val z = pos.z - viewerZ + .308
                val color = when (i) {
                    clickNeeded -> Color.GREEN
                    clickNeeded + 1 -> Color.YELLOW
                    else -> Color.RED
                }

                RenderUtil.drawFilledBoundingBox(
                    matrixStack,
                    AxisAlignedBB(x, y, z, x - .13, y + .26, z + .382),
                    color,
                    0.5f * Funny.alphaMult
                )
            }
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        clickInOrder.clear()
        clickNeeded = 0
    }
}