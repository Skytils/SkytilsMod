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
package gg.skytils.skytilsmod.features.impl.dungeons.solvers

import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.TickTask
import gg.skytils.skytilsmod.events.impl.skyblock.DungeonEvent
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.monster.EntityCreeper
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.awt.Color


object CreeperSolver {
    private val colors = CommonColors.set.copySet()
    private val solutionPairs = arrayListOf<Pair<BlockPos, BlockPos>>()
    private var creeper: EntityCreeper? = null
    private val candidateBlocks = setOf(Blocks.prismarine, Blocks.sea_lantern)

    @SubscribeEvent
    fun onPuzzleDiscovered(event: DungeonEvent.PuzzleEvent.Discovered) {
        if (event.puzzle == "Creeper Beams") {
            updatePuzzleState()
        }
    }

    fun updatePuzzleState() {
        if (this.creeper == null) {
            val creeperScan = mc.thePlayer.entityBoundingBox.expand(14.0, 8.0, 13.0)
            this.creeper = mc.theWorld?.getEntitiesWithinAABB(EntityCreeper::class.java, creeperScan) {
                it != null && !it.isInvisible && it.maxHealth == 20f && it.health == 20f && !it.hasCustomName()
            }?.firstOrNull()
        } else if (solutionPairs.isEmpty()) {
            val creeper = this.creeper!!.entityBoundingBox
            val validBox = creeper.expand(0.5, 0.75, 0.5)

            val roomBB = creeper.expand(14.0, 10.0, 13.0)
            val candidates = BlockPos.getAllInBox(BlockPos(roomBB.minVec), BlockPos(roomBB.maxVec)).filter {
                it.y > 68 && mc.theWorld?.getBlockState(it)?.block in candidateBlocks
            }
            val pairs = candidates.elementPairs()
            val usedPositions = mutableSetOf<BlockPos>()
            solutionPairs.addAll(pairs.filter { (a, b) ->
                if (a in usedPositions || b in usedPositions) return@filter false
                checkLineBox(validBox, a.middleVec(), b.middleVec(), Holder(Vec3(0.0, 0.0, 0.0))).also {
                    if (it) {
                        usedPositions.add(a)
                        usedPositions.add(b)
                    }
                }
            })

            if (SuperSecretSettings.bennettArthur) {
                solutionPairs.mapIndexed { i, pair ->
                    pair.first to solutionPairs[(i + 1) % solutionPairs.size].second
                }.let {
                    solutionPairs.clear()
                    solutionPairs.addAll(it)
                }
            }
        }
    }

    init {
        TickTask(20, repeats = true) {
            if (Skytils.config.creeperBeamsSolver && Utils.inDungeons && DungeonListener.missingPuzzles.contains(
                    "Creeper Beams"
                )
            ) {
                updatePuzzleState()
            }
        }
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
     * @author qJake
     * @link https://stackoverflow.com/a/3235902
     * https://creativecommons.org/licenses/by-sa/2.5/
     * Modified
     */
    private fun checkLineBox(bb: AxisAlignedBB, point1: Vec3, point2: Vec3, hitVec: Holder<Vec3>): Boolean {
        val minVec = bb.minVec
        val maxVec = bb.maxVec
        if (point2.x < minVec.x && point1.x < minVec.x) return false
        if (point2.x > maxVec.x && point1.x > maxVec.x) return false
        if (point2.y < minVec.y && point1.y < minVec.y) return false
        if (point2.y > maxVec.y && point1.y > maxVec.y) return false
        if (point2.z < minVec.z && point1.z < minVec.z) return false
        if (point2.z > maxVec.z && point1.z > maxVec.z) return false
        if (bb.isVecInside(point1)) {
            hitVec.value = point1
            return true
        }
        if ((getIntersection(
                point1.x - minVec.x,
                point2.x - minVec.x,
                point1,
                point2,
                hitVec
            ) && bb.isVecInYZ(hitVec.value))
            || (getIntersection(
                point1.y - minVec.y,
                point2.y - minVec.y,
                point1,
                point2,
                hitVec
            ) && bb.isVecInXZ(hitVec.value))
            || (getIntersection(
                point1.z - minVec.z,
                point2.z - minVec.z,
                point1,
                point2,
                hitVec
            ) && bb.isVecInXY(hitVec.value))
            || (getIntersection(
                point1.x - maxVec.x,
                point2.x - maxVec.x,
                point1,
                point2,
                hitVec
            ) && bb.isVecInYZ(hitVec.value))
            || (getIntersection(
                point1.y - maxVec.y,
                point2.y - maxVec.y,
                point1,
                point2,
                hitVec
            ) && bb.isVecInXZ(hitVec.value))
            || (getIntersection(
                point1.z - maxVec.z,
                point2.z - maxVec.z,
                point1,
                point2,
                hitVec
            ) && bb.isVecInXY(hitVec.value))
        ) return true

        return false
    }

    /**
     * @author qJake
     * @link https://stackoverflow.com/a/3235902
     * https://creativecommons.org/licenses/by-sa/2.5/
     * Modified
     */
    private fun getIntersection(
        dist1: Double,
        dist2: Double,
        point1: Vec3,
        point2: Vec3,
        hitVec: Holder<Vec3>
    ): Boolean {
        if ((dist1 * dist2) >= 0.0f) return false
        if (dist1 == dist2) return false
        hitVec.value = point1 + ((point2 - point1) * (-dist1 / (dist2 - dist1)))
        return true
    }

    /**
     * Checks if the specified vector is within the YZ dimensions of the bounding box.
     */
    private fun AxisAlignedBB.isVecInYZ(vec: Vec3): Boolean {
        return vec.yCoord >= this.minY && vec.yCoord <= this.maxY && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ
    }

    /**
     * Checks if the specified vector is within the XZ dimensions of the bounding box.
     */
    private fun AxisAlignedBB.isVecInXZ(vec: Vec3): Boolean {
        return vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ

    }

    /**
     * Checks if the specified vector is within the XY dimensions of the bounding box.
     */
    private fun AxisAlignedBB.isVecInXY(vec: Vec3): Boolean {
        return vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.yCoord >= this.minY && vec.yCoord <= this.maxY
    }

    private class Holder<T>(var value: T)
}