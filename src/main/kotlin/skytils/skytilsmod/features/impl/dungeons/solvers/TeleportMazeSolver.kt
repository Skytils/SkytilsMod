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

import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UChat
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
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.SendChatMessageEvent
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.utils.DevTools
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import java.awt.Color

class TeleportMazeSolver {

    companion object {
        private val steppedPads = ArrayList<BlockPos>()
        private var lastTpPos: BlockPos? = null
        val poss = HashSet<BlockPos>()
    }

    @SubscribeEvent
    fun onSendMsg(event: SendChatMessageEvent) {
        if (DevTools.getToggle("tpmaze") && event.message == "/resettp") {
            lastTpPos = null
            steppedPads.clear()
            poss.clear()
            event.isCanceled = true
        }
    }

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
                        if (!steppedPads.contains(it)) steppedPads.add(it)
                    }
                    Utils.getBlocksWithinRangeAtSameY(groundBlock, 1, 69).find {
                        mc.theWorld.getBlockState(it).block === Blocks.end_portal_frame
                    }?.let { pos ->
                        if (!steppedPads.contains(pos)) {
                            steppedPads.add(pos)
                            val vec = mc.thePlayer.lookVec.normalize()
                            val valid = HashSet<BlockPos>()
                            for (i in 4..23) {
                                val bp = BlockPos(
                                    mc.thePlayer.posX + vec.xCoord * i,
                                    69.0,
                                    mc.thePlayer.posZ + vec.zCoord * i
                                )
                                val allDir = Utils.getBlocksWithinRangeAtSameY(bp, 2, 69)

                                if (allDir.none { it == mc.thePlayer.position }) {
                                    valid.addAll(allDir.filter { it !in steppedPads && mc.theWorld.getBlockState(it).block === Blocks.end_portal_frame })
                                }
                            }
                            if (DevTools.getToggle("tpmaze")) UChat.chat(valid.joinToString { it.toString() })
                            if (poss.isEmpty()) {
                                poss.addAll(valid)
                            } else {
                                poss.removeAll {
                                    it !in valid
                                }
                            }
                        }
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

        for (pos in poss) {
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1).expand(0.01, 0.01, 0.01),
                Color.GREEN.withAlpha(69),
                1f
            )
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        steppedPads.clear()
        lastTpPos = null
        poss.clear()
    }
}