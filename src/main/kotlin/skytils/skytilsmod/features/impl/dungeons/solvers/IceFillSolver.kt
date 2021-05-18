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

import com.google.common.collect.ImmutableList
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import java.awt.Color
import kotlin.concurrent.thread

class IceFillSolver {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || mc.thePlayer == null || mc.theWorld == null) return
        if (!Skytils.config.iceFillSolver) return
        val world: World = mc.theWorld
        if (ticks % 20 == 0) {
            if (chestPos == null || roomFacing == null) {
                thread(name = "Skytils-Ice-Fill-Detection") {
                    findChest@ for (te in mc.theWorld.loadedTileEntityList) {
                        if (te.pos.y == 75 && te is TileEntityChest && te.numPlayersUsing == 0 && mc.thePlayer.getDistanceSq(
                                te.pos
                            ) < 25 * 25
                        ) {
                            val pos = te.pos
                            if (world.getBlockState(pos.down()).block == Blocks.stone) {
                                for (direction in EnumFacing.HORIZONTALS) {
                                    if (world.getBlockState(pos.offset(direction)).block == Blocks.cobblestone && world.getBlockState(
                                            pos.offset(direction.opposite, 2)
                                        ).block == Blocks.iron_bars && world.getBlockState(
                                            pos.offset(
                                                direction.rotateY(),
                                                2
                                            )
                                        ).block == Blocks.torch && world.getBlockState(
                                            pos.offset(
                                                direction.rotateYCCW(),
                                                2
                                            )
                                        ).block == Blocks.torch && world.getBlockState(
                                            pos.offset(direction.opposite).down(2)
                                        ).block == Blocks.stone_brick_stairs
                                    ) {
                                        chestPos = pos
                                        roomFacing = direction
                                        println(
                                            "Ice fill chest is at $chestPos and is facing $roomFacing"
                                        )
                                        break@findChest
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if ((solverThread == null || !solverThread!!.isAlive) && chestPos != null) {
                solverThread = Thread({
                    if (three == null) {
                        three = IceFillPuzzle(world, 70)
                    }
                    if (five == null) {
                        five = IceFillPuzzle(world, 71)
                    }
                    if (seven == null) {
                        seven = IceFillPuzzle(world, 72)
                    }
                    if (three!!.paths.size == 0) {
                        three!!.genPaths(world)
                    }
                    if (five!!.paths.size == 0) {
                        five!!.genPaths(world)
                    }
                    if (seven!!.paths.size == 0) {
                        seven!!.genPaths(world)
                    }
                }, "Skytils-Ice-Fill-Solution")
                solverThread!!.start()
            }
            ticks = 0
        }
        ticks++
    }

    private fun checkForStart(world: World, pos: BlockPos): Boolean {
        return world.getBlockState(pos).block === Blocks.air && world.getBlockState(pos.offset(roomFacing!!.rotateY())).block === Blocks.cobblestone_wall && world.getBlockState(
            pos.offset(
                roomFacing!!.rotateYCCW()
            )
        ).block === Blocks.cobblestone_wall
    }

    private fun generatePairs(world: World, positions: List<BlockPos>): List<Move> {
        val moves: MutableList<Move> = ArrayList()
        for (pos in positions) {
            val potential = getPossibleMoves(world, pos)
            for (potent in potential) {
                val potentialMove = Move(pos, potent)
                if (!moves.contains(potentialMove)) moves.add(potentialMove)
            }
        }
        return moves
    }

    private fun getPossibleMoves(world: World, pos: BlockPos): List<BlockPos> {
        val moves: MutableList<BlockPos> = ArrayList()
        if (world.getBlockState(
                pos.north().down()
            ).block === Blocks.ice && world.getBlockState(pos.north()).block !== Blocks.stone
        ) {
            moves.add(pos.north())
        }
        if (world.getBlockState(
                pos.south().down()
            ).block === Blocks.ice && world.getBlockState(pos.south()).block !== Blocks.stone
        ) {
            moves.add(pos.south())
        }
        if (world.getBlockState(
                pos.east().down()
            ).block === Blocks.ice && world.getBlockState(pos.east()).block !== Blocks.stone
        ) {
            moves.add(pos.east())
        }
        if (world.getBlockState(
                pos.west().down()
            ).block === Blocks.ice && world.getBlockState(pos.west()).block !== Blocks.stone
        ) {
            moves.add(pos.west())
        }
        return moves
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Skytils.config.iceFillSolver) return
        if (chestPos != null && roomFacing != null) {
            if (three != null && three!!.paths.size > 0) {
                for (i in 0 until three!!.paths[0].size - 1) {
                    val pos = Vec3(three!!.paths[0][i])
                    val pos2 = Vec3(three!!.paths[0][i + 1])
                    GlStateManager.disableCull()
                    RenderUtil.draw3DLine(
                        pos.addVector(0.5, 0.01, 0.5),
                        pos2.addVector(0.5, 0.01, 0.5),
                        5,
                        Color(255, 0, 0),
                        event.partialTicks
                    )
                    GlStateManager.enableCull()
                }
            }
            if (five != null && five!!.paths.size > 0) {
                for (i in 0 until five!!.paths[0].size - 1) {
                    val pos = Vec3(five!!.paths[0][i])
                    val pos2 = Vec3(five!!.paths[0][i + 1])
                    GlStateManager.disableCull()
                    RenderUtil.draw3DLine(
                        pos.addVector(0.5, 0.01, 0.5),
                        pos2.addVector(0.5, 0.01, 0.5),
                        5,
                        Color(255, 0, 0),
                        event.partialTicks
                    )
                    GlStateManager.enableCull()
                }
            }
            if (seven != null && seven!!.paths.size > 0) {
                for (i in 0 until seven!!.paths[0].size - 1) {
                    val pos = Vec3(seven!!.paths[0][i])
                    val pos2 = Vec3(seven!!.paths[0][i + 1])
                    GlStateManager.disableCull()
                    RenderUtil.draw3DLine(
                        pos.addVector(0.5, 0.01, 0.5),
                        pos2.addVector(0.5, 0.01, 0.5),
                        5,
                        Color(255, 0, 0),
                        event.partialTicks
                    )
                    GlStateManager.enableCull()
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        chestPos = null
        roomFacing = null
        three = null
        five = null
        seven = null
    }

    private inner class IceFillPuzzle(world: World, y: Int) {
        private val spaces: MutableList<BlockPos> = ArrayList()
        private var start: BlockPos? = null
        var paths: MutableList<List<BlockPos?>> = ArrayList()
        fun genPaths(world: World) {
            // Generate paths
            val moves = generatePairs(world, spaces)
            val g = Graph(moves, world)
            val path: MutableList<BlockPos?> = ArrayList()
            path.add(start)
            val visited: MutableMap<BlockPos?, Boolean?> = HashMap()
            visited[start] = true
            try {
                getPaths(g, start, visited, path, spaces.size)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * Take from Techie delight
         * Modified
         * https://www.techiedelight.com/print-all-hamiltonian-path-present-in-a-graph/
         */
        private fun getPaths(
            g: Graph,
            v: BlockPos?,
            visited: MutableMap<BlockPos?, Boolean?>,
            path: MutableList<BlockPos?>,
            N: Int
        ) {
            if (path.size == N) {
                val newPath: List<BlockPos?> = ImmutableList.copyOf(path)
                if (!paths.contains(path)) paths.add(newPath)
                return
            } else {

                // Check if every move starting from position `v` leads
                // to a solution or not
                val check = g.adjList[v]
                if (check != null) {
                    for (w in check) {
                        // process only unvisited vertices as the Hamiltonian
                        // path visit each vertex exactly once
                        if (visited[w] == null || !visited[w]!!) {
                            visited[w] = true
                            path.add(w)

                            // check if adding vertex `w` to the path leads
                            // to the solution or not
                            getPaths(g, w, visited, path, N)

                            // backtrack
                            visited[w] = false
                            path.removeAt(path.size - 1)
                        }
                    }
                }
            }
        }

        init {
            if (chestPos == null) throw NullPointerException()
            for (pos in Utils.getBlocksWithinRangeAtSameY(chestPos!!, 25, y)) {
                val block = world.getBlockState(pos)
                if (world.getBlockState(pos.down()).block === Blocks.ice || world.getBlockState(pos.down()).block === Blocks.packed_ice) {
                    if (block.block === Blocks.air) {
                        spaces.add(pos)
                    }
                } else if ((world.getBlockState(pos.down()).block === Blocks.stone_brick_stairs || world.getBlockState(
                        pos.down()
                    ).block === Blocks.stone) && start == null
                ) {
                    if (checkForStart(world, pos)) {
                        start = pos.offset(roomFacing)
                    }
                }
            }
        }
    }

    /**
     * Take from Techie delight
     * Modified
     * https://www.techiedelight.com/print-all-hamiltonian-path-present-in-a-graph/
     */
    private inner class Move(var source: BlockPos, var dest: BlockPos) {
        override fun equals(other: Any?): Boolean {
            return equals(this, other)
        }

        fun equals(original: Any, other: Any?): Boolean {
            if (other === original) return true
            if (other !is Move || original !is Move) return false
            return if (original.dest == other.dest && original.source == other.source) {
                true
            } else original.dest == other.source && original.source == other.dest
        }

        override fun hashCode(): Int {
            var result = source.hashCode()
            result = 31 * result + dest.hashCode()
            return result
        }
    }

    /**
     * Take from Techie delight
     * Modified
     * https://www.techiedelight.com/print-all-hamiltonian-path-present-in-a-graph/
     */
    private inner class Graph constructor(moves: List<Move>, world: World) {
        var adjList: MutableMap<BlockPos, List<BlockPos>> = HashMap()

        init {
            for (move in moves) {
                val src = move.source
                val dest = move.dest
                adjList[src] = getPossibleMoves(world, src)
                adjList[dest] = getPossibleMoves(world, dest)
            }
        }
    }

    companion object {
        private val mc = Minecraft.getMinecraft()
        private var ticks = 0
        private var chestPos: BlockPos? = null
        private var roomFacing: EnumFacing? = null
        private var three: IceFillPuzzle? = null
        private var five: IceFillPuzzle? = null
        private var seven: IceFillPuzzle? = null
        private var solverThread: Thread? = null
    }
}