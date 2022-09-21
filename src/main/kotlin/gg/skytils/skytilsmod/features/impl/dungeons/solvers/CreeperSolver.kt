/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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
package gg.skytils.skytilsmod.features.impl.dungeons.solvers

import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.opengl.GL11
import java.awt.Color


class CreeperSolver {
    private val colors = CommonColors.set.copySet()
    private val solutionPairs = arrayListOf<Pair<BlockPos, BlockPos>>()
    private var ticks = 0
    private var creeper: EntityCreeper? = null

    /**
     * Original code was taken from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        val mc = Minecraft.getMinecraft()
        val world: World? = mc.theWorld
        val player = mc.thePlayer
        if (ticks % 20 == 0) {
            if (Skytils.config.creeperBeamsSolver && Utils.inDungeons && world != null && player != null && DungeonListener.missingPuzzles.contains(
                    "Creeper Beams"
                )
            ) {
                val x = player.posX
                val y = player.posY
                val z = player.posZ
                if (this.creeper == null) {
                    // Find creepers nearby
                    val creeperScan = AxisAlignedBB(x - 14, y - 8, z - 13, x + 14, y + 8, z + 13) // 28x16x26 cube
                    this.creeper = world.getEntitiesWithinAABB(EntityCreeper::class.java, creeperScan).find {
                        !it.isInvisible && it.maxHealth == 20f && it.health == 20f && !it.hasCustomName()
                    }
                } else {
                    val creeper = this.creeper!!
                    // Start creeper line drawings
                    if (solutionPairs.isEmpty()) {
                        // Search for nearby sea lanterns and prismarine blocks
                        val point1 = BlockPos(creeper.posX - 14, creeper.posY - 7, creeper.posZ - 13)
                        val point2 = BlockPos(creeper.posX + 14, creeper.posY + 10, creeper.posZ + 13)
                        for (blockPos in BlockPos.getAllInBox(point1, point2)) {
                            val block: Block = world.getBlockState(blockPos).block
                            if (block === Blocks.sea_lantern || block === Blocks.prismarine) {
                                // Connect block to nearest block on opposite side
                                val startBlock = Vec3(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5)
                                val oppositeBlock =
                                    getFirstBlockPosAfterVectors(
                                        startBlock,
                                        Vec3(creeper.posX, creeper.posY + 1, creeper.posZ),
                                        10,
                                        20
                                    )
                                val endBlock =
                                    getNearbyBlock(oppositeBlock, Blocks.sea_lantern, Blocks.prismarine)
                                if (endBlock != null && startBlock.yCoord > 68 && endBlock.y > 68) { // Don't create line underground
                                    if (solutionPairs.none {
                                            Utils.equalsOneOf(
                                                blockPos,
                                                it.first,
                                                it.second
                                            ) || Utils.equalsOneOf(endBlock, it.first, it.second)
                                        }) solutionPairs.add(blockPos to endBlock)
                                }
                            }
                            if (SuperSecretSettings.bennettArthur) {
                                solutionPairs.mapIndexed { i, pair ->
                                    pair.first to solutionPairs[(i + 1) % solutionPairs.size].second
                                }
                            }
                        }
                    }
                }
            }
            ticks = 0
        }
        ticks++
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (Skytils.config.creeperBeamsSolver && solutionPairs.isNotEmpty() && !creeper!!.isDead && DungeonListener.missingPuzzles.contains(
                "Creeper Beams"
            )
        ) {
            val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(event.partialTicks)
            GlStateManager.disableCull()
            val blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND)
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            val matrixStack = UMatrixStack()
            for (i in solutionPairs.indices) {
                val (one, two) = solutionPairs[i]
                if (mc.theWorld.getBlockState(one).block == Blocks.prismarine && mc.theWorld.getBlockState(two).block == Blocks.prismarine) {
                    continue
                }
                val color = Color(colors[i % colors.size].toInt())
                val first = Vec3(one).addVector(-viewerX, -viewerY, -viewerZ)
                val second = Vec3(two).addVector(-viewerX, -viewerY, -viewerZ)
                val aabb1 = AxisAlignedBB(
                    first.xCoord,
                    first.yCoord,
                    first.zCoord,
                    first.xCoord + 1,
                    first.yCoord + 1,
                    first.zCoord + 1
                )
                val aabb2 = AxisAlignedBB(
                    second.xCoord,
                    second.yCoord,
                    second.zCoord,
                    second.xCoord + 1,
                    second.yCoord + 1,
                    second.zCoord + 1
                )
                RenderUtil.drawFilledBoundingBox(
                    matrixStack, aabb1.expand(0.01, 0.01, 0.01), color, 0.5f
                )
                RenderUtil.drawFilledBoundingBox(
                    matrixStack, aabb2.expand(0.01, 0.01, 0.01), color, 0.5f
                )
            }
            if (!blendEnabled) GlStateManager.disableBlend()
            GlStateManager.enableCull()
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        creeper = null
        solutionPairs.clear()
    }

    /**
     * Original code was taken from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    private fun getNearbyBlock(pos: BlockPos?, vararg blockTypes: Block): BlockPos? {
        if (pos == null) return null
        val pos1 = BlockPos(pos.x - 2, pos.y - 3, pos.z - 2)
        val pos2 = BlockPos(pos.x + 2, pos.y + 3, pos.z + 2)
        var closestBlock: BlockPos? = null
        var closestBlockDistance = 99.0
        val blocks = BlockPos.getAllInBox(pos1, pos2)
        for (block in blocks) {
            for (blockType in blockTypes) {
                if (mc.theWorld.getBlockState(block).block === blockType && block.distanceSq(pos) < closestBlockDistance) {
                    closestBlock = block
                    closestBlockDistance = block.distanceSq(pos)
                }
            }
        }
        return closestBlock
    }

    /**
     * Original code was taken from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    private fun getFirstBlockPosAfterVectors(
        pos1: Vec3,
        pos2: Vec3,
        strength: Int,
        distance: Int
    ): BlockPos? {
        val x = pos2.xCoord - pos1.xCoord
        val y = pos2.yCoord - pos1.yCoord
        val z = pos2.zCoord - pos1.zCoord
        for (i in strength until distance * strength) { // Start at least 1 strength away
            val newX = pos1.xCoord + x / strength * i
            val newY = pos1.yCoord + y / strength * i
            val newZ = pos1.zCoord + z / strength * i
            val newBlock = BlockPos(newX, newY, newZ)
            if (mc.theWorld.getBlockState(newBlock).block !== Blocks.air) {
                return newBlock
            }
        }
        return null
    }
}