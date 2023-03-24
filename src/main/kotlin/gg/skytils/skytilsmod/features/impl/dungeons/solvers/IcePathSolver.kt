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
import gg.skytils.skytilsmod.features.impl.misc.Funny
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import kotlinx.coroutines.launch
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.monster.EntitySilverfish
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.awt.Point
import java.util.*

object IcePathSolver {
    private val steps: MutableList<Point?> = ArrayList()
    private var silverfishChestPos: BlockPos? = null
    private var roomFacing: EnumFacing? = null
    private var grid: Array<IntArray>? = null
    private var silverfish: EntitySilverfish? = null
    private var silverfishPos: Point? = null

    init {
        TickTask(20, repeats = true) {
            if (!Utils.inDungeons || !Skytils.config.icePathSolver || mc.thePlayer == null) return@TickTask
            if (DungeonListener.missingPuzzles.contains("Ice Path")) {
                val silverfish = mc.theWorld.getEntities(
                    EntitySilverfish::class.java
                ) { s: EntitySilverfish? -> mc.thePlayer.getDistanceSqToEntity(s) < 20 * 20 }
                if (silverfish.size > 0) {
                    this.silverfish = silverfish[0]
                    if (silverfishChestPos == null || roomFacing == null) {
                        Skytils.launch {
                            findChest@ for (te in mc.theWorld.loadedTileEntityList) {
                                val playerX = mc.thePlayer.posX.toInt()
                                val playerZ = mc.thePlayer.posZ.toInt()
                                val xRange = playerX - 25..playerX + 25
                                val zRange = playerZ - 25..playerZ + 25
                                if (te.pos.y == 67 && te is TileEntityChest && te.numPlayersUsing == 0 && te.pos.x in xRange && te.pos.z in zRange
                                ) {
                                    val pos = te.pos
                                    if (mc.theWorld.getBlockState(pos.down()).block == Blocks.packed_ice && mc.theWorld.getBlockState(
                                            pos.up(2)
                                        ).block == Blocks.hopper
                                    ) {
                                        for (direction in EnumFacing.HORIZONTALS) {
                                            if (mc.theWorld.getBlockState(pos.offset(direction)).block == Blocks.stonebrick) {
                                                silverfishChestPos = pos
                                                roomFacing = direction
                                                println(
                                                    "Silverfish chest is at $silverfishChestPos and is facing $roomFacing",
                                                )
                                                break@findChest
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (silverfishChestPos != null && roomFacing != null) {
                if (grid == null) {
                    grid = layout
                    silverfishPos = getGridPointFromPos(silverfish!!.position)
                    steps.clear()
                    if (silverfishPos != null) {
                        steps.addAll(solve(grid!!, silverfishPos!!.x, silverfishPos!!.y, 9, 0))
                    }
                } else if (silverfish != null) {
                    val silverfishGridPos = getGridPointFromPos(silverfish!!.position)
                    if (silverfish!!.isEntityAlive && silverfishGridPos != silverfishPos) {
                        silverfishPos = silverfishGridPos
                        if (silverfishPos != null) {
                            steps.clear()
                            steps.addAll(solve(grid!!, silverfishPos!!.x, silverfishPos!!.y, 9, 0))
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Skytils.config.icePathSolver) return
        if (silverfishChestPos != null && roomFacing != null && grid != null && silverfish!!.isEntityAlive) {
            for (i in 0 until steps.size - 1) {
                val point = steps[i]
                val point2 = steps[i + 1]
                val pos = getVec3RelativeToGrid(point!!.x, point.y)
                val pos2 = getVec3RelativeToGrid(point2!!.x, point2.y)
                GlStateManager.disableCull()
                RenderUtil.draw3DLine(
                    pos!!.addVector(0.5, 0.5, 0.5),
                    pos2!!.addVector(0.5, 0.5, 0.5),
                    5,
                    Color(1f, 0f, 0f, Funny.alphaMult),
                    event.partialTicks,
                    UMatrixStack.Compat.get()
                )
                GlStateManager.enableCull()
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        silverfishChestPos = null
        roomFacing = null
        grid = null
        steps.clear()
        silverfish = null
        silverfishPos = null
    }

    private fun getVec3RelativeToGrid(column: Int, row: Int): Vec3? {
        return if (silverfishChestPos == null || roomFacing == null) null else Vec3(
            silverfishChestPos!!
                .offset(roomFacing!!.opposite, 4)
                .offset(roomFacing!!.rotateYCCW(), 8)
                .offset(roomFacing!!.rotateY(), column)
                .offset(roomFacing!!.opposite, row)
        )
    }

    private fun getGridPointFromPos(pos: BlockPos): Point? {
        if (silverfishChestPos == null || roomFacing == null) return null
        for (row in 0..16) {
            for (column in 0..16) {
                if (BlockPos(getVec3RelativeToGrid(column, row)) == pos) {
                    return Point(column, row)
                }
            }
        }
        return null
    }

    private val layout: Array<IntArray>?
        get() {
            if (silverfishChestPos == null || roomFacing == null) return null
            val grid = Array(17) { IntArray(17) }
            for (row in 0..16) {
                for (column in 0..16) {
                    grid[row][column] = if (mc.theWorld.getBlockState(
                            BlockPos(
                                getVec3RelativeToGrid(
                                    column,
                                    row
                                )
                            )
                        ).block !== Blocks.air
                    ) 1 else 0
                }
                if (row == 16) return grid
            }
            return null
        }

    /**
     * This code was modified into returning an ArrayList and was taken under CC BY-SA 4.0
     *
     * @link https://stackoverflow.com/a/55271133
     * @author ofekp
     */
    private fun solve(iceCave: Array<IntArray>, startX: Int, startY: Int, endX: Int, endY: Int): ArrayList<Point?> {
        val startPoint = Point(startX, startY)
        val queue = ArrayDeque<Point>()
        val iceCaveColors = Array(
            iceCave.size
        ) { arrayOfNulls<Point>(iceCave[0].size) }
        queue.addLast(Point(startX, startY))
        iceCaveColors[startY][startX] = startPoint
        while (queue.size != 0) {
            val currPos = queue.pollFirst()
            // traverse adjacent nodes while sliding on the ice
            for (dir in EnumFacing.HORIZONTALS) {
                val nextPos = move(iceCave, iceCaveColors, currPos, dir)
                if (nextPos != null) {
                    queue.addLast(nextPos)
                    iceCaveColors[nextPos.y][nextPos.x] = Point(
                        currPos.x, currPos.y
                    )
                    if (nextPos.getY() == endY.toDouble() && nextPos.getX() == endX.toDouble()) {
                        val steps = ArrayList<Point?>()
                        // we found the end point
                        var tmp = currPos // if we start from nextPos we will count one too many edges
                        var count = 0
                        steps.add(nextPos)
                        steps.add(currPos)
                        while (tmp !== startPoint) {
                            count++
                            tmp = iceCaveColors[tmp!!.y][tmp.x]
                            steps.add(tmp)
                        }
                        //System.out.println("Silverfish solved in " + count + " moves.");
                        return steps
                    }
                }
            }
        }
        return arrayListOf()
    }

    /**
     * This code was modified to fit Minecraft and was taken under CC BY-SA 4.0
     *
     * @link https://stackoverflow.com/a/55271133
     * @author ofekp
     */
    private fun move(
        iceCave: Array<IntArray>?,
        iceCaveColors: Array<Array<Point?>>,
        currPos: Point,
        dir: EnumFacing
    ): Point? {
        val x = currPos.x
        val y = currPos.y
        val diffX = dir.directionVec.x
        val diffY = dir.directionVec.z
        var i = 1
        while (x + i * diffX >= 0 && x + i * diffX < iceCave!![0].size && y + i * diffY >= 0 && y + i * diffY < iceCave.size && iceCave[y + i * diffY][x + i * diffX] != 1) {
            i++
        }
        i-- // reverse the last step
        return if (iceCaveColors[y + i * diffY][x + i * diffX] != null) {
            // we've already seen this point
            null
        } else Point(x + i * diffX, y + i * diffY)
    }
}