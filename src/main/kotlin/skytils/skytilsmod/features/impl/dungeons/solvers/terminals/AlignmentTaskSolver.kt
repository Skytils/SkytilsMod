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

import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import java.awt.Color
import java.awt.Point
import java.util.*
import kotlin.math.floor

class AlignmentTaskSolver {
    // the blocks are on the west side, frames block pos is 1 block higher
    private val topLeft = BlockPos(197, 124, 278).up()
    private val bottomRight = BlockPos(197, 120, 274).up()
    private val box = BlockPos.getAllInBox(topLeft, bottomRight).toList().sortedWith { a, b ->
        if (a.y == b.y) {
            return@sortedWith b.z - a.z
        }
        if (a.y < b.y) return@sortedWith 1
        if (a.y > b.y) return@sortedWith -1
        return@sortedWith 0
    }

    private val grid = LinkedHashSet<MazeSpace>()

    private val directionSet = HashMap<Point, Int>()

    private var ticks = 0

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (mc.thePlayer == null || mc.theWorld == null || event.phase != TickEvent.Phase.START) return
        if (!Skytils.config.alignmentTerminalSolver || !Utils.inDungeons || DungeonFeatures.dungeonFloor != "F7" || DungeonTimer.phase2ClearTime == -1L || DungeonTimer.phase3ClearTime != -1L) return
        if (ticks % 20 == 0) {
            if (mc.thePlayer.getDistanceSqToCenter(topLeft) <= 25 * 25) {
                if (grid.size < 25) {
                    val frames = mc.theWorld.getEntities(EntityItemFrame::class.java) {
                        it != null && box.contains(it.position) && it.displayedItem != null && Utils.equalsOneOf(
                            it.displayedItem.item,
                            Items.arrow,
                            Item.getItemFromBlock(Blocks.wool)
                        )
                    }
                    if (frames.isNotEmpty()) {
                        for ((i, pos) in box.withIndex()) {
                            val row = i % 5
                            val column = floor((i / 5f).toDouble()).toInt()
                            val coords = Point(row, column)
                            val frame = frames.find { it.position == pos }
                            if (frame != null) {
                                val type = with(frame.displayedItem) {
                                    when (item) {
                                        Items.arrow -> SpaceType.PATH
                                        Item.getItemFromBlock(Blocks.wool) -> {
                                            when (itemDamage) {
                                                5 -> SpaceType.STARTER
                                                14 -> SpaceType.END
                                                else -> SpaceType.PATH
                                            }
                                        }
                                        else -> SpaceType.EMPTY
                                    }
                                }
                                grid.add(MazeSpace(frame.hangingPosition, type, coords))
                            } else {
                                grid.add(MazeSpace(type = SpaceType.EMPTY, coords = coords))
                            }
                        }
                    }
                } else if (directionSet.isEmpty()) {
                    val startPositions = grid.filter { it.type == SpaceType.STARTER }
                    val endPositions = grid.filter { it.type == SpaceType.END }
                    val layout = layout
                    for (start in startPositions) {
                        for (endPosition in endPositions) {
                            val pointMap = solve(layout, start.coords, endPosition.coords)
                            if (pointMap.size == 0) continue
                            val moveSet = convertPointMapToMoves(pointMap)
                            for (move in moveSet) {
                                directionSet[move.point] = move.directionNum
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
    fun onRenderWorld(event: RenderWorldLastEvent) {
        for (space in grid) {
            if (space.type != SpaceType.PATH || space.framePos == null) continue
            val frame =
                (mc.theWorld.loadedEntityList.find { it is EntityItemFrame && it.hangingPosition == space.framePos }
                    ?: continue) as EntityItemFrame
            var neededClicks = directionSet.getOrElse(space.coords) { 0 } - frame.rotation
            if (neededClicks == 0) continue
            if (neededClicks < 0) neededClicks += 8
            RenderUtil.draw3DString(
                getVec3RelativeToGrid(space.coords.x, space.coords.y).addVector(0.5, 0.5, 0.5),
                neededClicks.toString(),
                Color.RED,
                event.partialTicks
            )
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        grid.clear()
        directionSet.clear()
    }

    class MazeSpace(val framePos: BlockPos? = null, val type: SpaceType, val coords: Point)

    enum class SpaceType {
        EMPTY,
        PATH,
        STARTER,
        END,
    }

    class GridMove(val point: Point, val directionNum: Int)

    private fun convertPointMapToMoves(solution: ArrayList<Point>): ArrayList<GridMove> {
        solution.reverse()
        val moves = arrayListOf<GridMove>()
        for (i in 0 until solution.size - 1) {
            val current = solution[i]
            val next = solution[i + 1]
            val diffX = current.x - next.x
            val diffY = current.y - next.y
            directionCheck@ for (dir in EnumFacing.HORIZONTALS) {
                val dirX = dir.directionVec.x
                val dirY = dir.directionVec.z
                if (dirX == diffX && dirY == diffY) {
                    val rotation = when (dir.opposite) {
                        EnumFacing.EAST -> 1
                        EnumFacing.WEST -> 5
                        EnumFacing.SOUTH -> 3
                        EnumFacing.NORTH -> 7
                        else -> 0
                    }
                    moves.add(GridMove(current, rotation))
                    break@directionCheck
                }
            }
        }
        solution.reverse()
        return moves
    }

    private val layout: Array<IntArray>
        get() {
            val grid = Array(5) { IntArray(5) }
            for (row in 0..4) {
                for (column in 0..4) {
                    val space = this.grid.find { it.coords == Point(row, column) }
                    grid[column][row] = if (space?.framePos != null) 0 else 1
                }
            }
            return grid
        }

    private fun getVec3RelativeToGrid(row: Int, column: Int): Vec3 {
        return Vec3(topLeft.down().north(row).down(column))
    }

    private val directions = EnumFacing.HORIZONTALS.reversed()

    /**
     * This code was modified into returning an ArrayList and was taken under CC BY-SA 4.0
     *
     * @link https://stackoverflow.com/a/55271133
     * @author ofekp
     */
    private fun solve(
        grid: Array<IntArray>,
        start: Point,
        end: Point
    ): ArrayList<Point> {
        val queue = LinkedList<Point>()
        val gridCopy = Array(
            grid.size
        ) { arrayOfNulls<Point>(grid[0].size) }
        queue.addLast(start)
        gridCopy[start.y][start.x] = start
        while (queue.size != 0) {
            val currPos = queue.pollFirst()!!
            // traverse adjacent nodes while sliding on the ice
            for (dir in directions) {
                val nextPos = move(grid, gridCopy, currPos, dir)
                if (nextPos != null) {
                    queue.addLast(nextPos)
                    gridCopy[nextPos.getY().toInt()][nextPos.getX().toInt()] = Point(
                        currPos.getX().toInt(), currPos.getY().toInt()
                    )
                    if (end == Point(nextPos.x, nextPos.y)) {
                        val steps = ArrayList<Point>()
                        // we found the end point
                        var tmp = currPos // if we start from nextPos we will count one too many edges
                        var count = 0
                        steps.add(nextPos)
                        steps.add(currPos)
                        while (tmp !== start) {
                            count++
                            tmp = gridCopy[tmp.getY().toInt()][tmp.getX().toInt()]!!
                            steps.add(tmp)
                        }
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
        grid: Array<IntArray>,
        gridCopy: Array<Array<Point?>>,
        currPos: Point,
        dir: EnumFacing
    ): Point? {
        val x = currPos.getX().toInt()
        val y = currPos.getY().toInt()
        val diffX = dir.directionVec.x
        val diffY = dir.directionVec.z
        val i =
            if (x + diffX >= 0 && x + diffX < grid[0].size && y + diffY >= 0 && y + diffY < grid.size && grid[y + diffY][x + diffX] != 1) {
                1
            } else 0
        return if (gridCopy[y + i * diffY][x + i * diffX] != null) {
            // we've already seen this point
            null
        } else Point(x + i * diffX, y + i * diffY)
    }
}