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
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.funny.Funny
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
import java.awt.Color

object IceFillSolver {
    private var puzzles: Triple<IceFillPuzzle, IceFillPuzzle, IceFillPuzzle>? = null
    private var job: Job? = null

    init {
        tickTimer(20, repeats = true) {
            if (!Utils.inDungeons || !Skytils.config.iceFillSolver || mc.thePlayer == null) return@tickTimer
            val world: World = mc.theWorld
            if (DungeonListener.missingPuzzles.contains("Ice Fill") && puzzles == null && (job == null || job?.isActive == false)) {
                job = Skytils.launch {
                    val playerX = mc.thePlayer.posX.toInt()
                    val playerZ = mc.thePlayer.posZ.toInt()
                    val xRange = playerX - 25..playerX + 25
                    val zRange = playerZ - 25..playerZ + 25
                    findChest@ for (te in mc.theWorld.loadedTileEntityList) {
                        if (te.pos.y == 75 && te is TileEntityChest && te.numPlayersUsing == 0 && te.pos.x in xRange && te.pos.z in zRange
                        ) {
                            val pos = te.pos
                            if (world.getBlockState(pos.down()).block == Blocks.stone) {
                                for (direction in EnumFacing.HORIZONTALS) {
                                    fun checkChestTorches(dir: EnumFacing): Boolean {
                                        return world.getBlockState(
                                            pos.offset(
                                                dir,
                                                1
                                            )
                                        ).block == Blocks.torch && world.getBlockState(
                                            pos.offset(
                                                dir.opposite,
                                                3
                                            )
                                        ).block == Blocks.torch
                                    }

                                    if (world.getBlockState(pos.offset(direction)).block == Blocks.cobblestone && world.getBlockState(
                                            pos.offset(direction.opposite, 2)
                                        ).block == Blocks.iron_bars) {

                                        val offsetDir: EnumFacing? = if (checkChestTorches(direction.rotateY())) {
                                            direction.rotateYCCW()
                                        } else if (checkChestTorches(direction.rotateYCCW())) {
                                            direction.rotateY()
                                        } else continue

                                        if (world.getBlockState(
                                                pos.offset(direction.opposite)
                                                    .offset(offsetDir)
                                                    .down(2)
                                            ).block == Blocks.stone_brick_stairs) {
                                            puzzles = Triple(
                                                IceFillPuzzle(world, 70, pos, direction),
                                                IceFillPuzzle(world, 71, pos, direction),
                                                IceFillPuzzle(world, 72, pos, direction)
                                            )
                                            println(
                                                "An Ice Fill chest is at $pos and is facing $direction. Offset direction is $offsetDir."
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
    }

    private fun checkForStart(world: World, pos: BlockPos, facing: EnumFacing) =
        world.getBlockState(pos).block === Blocks.air &&
                world.getBlockState(pos.offset(facing.rotateY())).block === Blocks.cobblestone_wall &&
                world.getBlockState(pos.offset(facing.rotateYCCW())).block === Blocks.cobblestone_wall

    private fun generatePairs(world: World, positions: List<BlockPos>) =
        positions.flatMap { pos -> getPossibleMoves(world, pos).map { Move(pos, it) } }

    private fun getPossibleMoves(world: World, pos: BlockPos) =
        EnumFacing.HORIZONTALS.map { pos.offset(it) }.filter { spot ->
            val down = world.getBlockState(spot.down()).block
            (down == Blocks.ice || down == Blocks.packed_ice) && world.getBlockState(spot).block != Blocks.stone
        }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Skytils.config.iceFillSolver || "Ice Fill" !in DungeonListener.missingPuzzles) return
        val (three, five, seven) = puzzles ?: return
        val matrixStack = UMatrixStack.Compat.get()
        three.draw(matrixStack, event.partialTicks)
        five.draw(matrixStack, event.partialTicks)
        seven.draw(matrixStack, event.partialTicks)
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        puzzles = null
        job = null
    }

    private class IceFillPuzzle(world: World, y: Int, chestPos: BlockPos, roomFacing: EnumFacing) {
        private val spaces: MutableList<BlockPos> = ArrayList()
        private lateinit var start: BlockPos
        var paths: MutableSet<List<BlockPos>> = HashSet()
        fun genPaths(world: World) {
            // Generate paths
            val moves = generatePairs(world, spaces)
            val g = Graph(moves, world)
            val path: MutableList<BlockPos> = ArrayList()
            path.add(start)
            try {
                getPaths(g, start, mutableSetOf(start), path, spaces.size)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun draw(matrixStack: UMatrixStack, partialTicks: Float) =
            paths.firstOrNull()?.zipWithNext { first, second ->
                GlStateManager.disableCull()
                RenderUtil.draw3DLine(
                    Vec3(first).addVector(0.5, 0.01, 0.5),
                    Vec3(second).addVector(0.5, 0.01, 0.5),
                    5,
                    Color.RED,
                    partialTicks,
                    matrixStack,
                    Funny.alphaMult
                )
                GlStateManager.enableCull()
            }

        private fun getPaths(
            g: Graph,
            v: BlockPos,
            visited: MutableSet<BlockPos>,
            path: MutableList<BlockPos>,
            n: Int
        ) {
            if (path.size == n) {
                val newPath: List<BlockPos> = path.toList()
                paths.add(newPath)
                return
            } else {

                // Check if every move starting from position `v` leads
                // to a solution or not
                g.adjList[v]?.forEach { w ->
                    // Only check if we haven't been there before
                    if (!visited.contains(w)) {
                        visited.add(w)
                        path.add(w)

                        // Continue checking down this path
                        getPaths(g, w, visited, path, n)

                        // backtrack
                        visited.remove(w)
                        path.remove(w)
                    }
                }
            }
        }

        init {
            chestPos.offset(roomFacing.opposite, 11).run { Utils.getBlocksWithinRangeAtSameY(chestPos, 25, y) }
                .forEach { pos ->
                    when (world.getBlockState(pos.down()).block) {
                        Blocks.ice, Blocks.packed_ice ->
                            if (world.getBlockState(pos).block === Blocks.air)
                                spaces.add(pos)

                        Blocks.stone_brick_stairs, Blocks.stone -> {
                            if (!::start.isInitialized && checkForStart(world, pos, roomFacing))
                                start = pos.offset(roomFacing)
                        }
                    }
                }
            genPaths(world)
        }
    }

    private data class Move(var source: BlockPos, var dest: BlockPos)

    private class Graph(moves: Collection<Move>, world: World) {
        val adjList: Map<BlockPos, Collection<BlockPos>> = buildMap {
            moves.forEach { (source, dest) ->
                this[source] = getPossibleMoves(world, source)
                this[dest] = getPossibleMoves(world, dest)
            }
        }
    }
}