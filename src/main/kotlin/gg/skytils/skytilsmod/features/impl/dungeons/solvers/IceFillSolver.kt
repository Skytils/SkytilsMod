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

import gg.essential.universal.UChat
import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.funny.Funny
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.ifNull
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
            if (DungeonListener.missingPuzzles.contains("Ice Fill") && puzzles == null && job?.isActive != true) {
                job = Skytils.launch {
                    val playerX = mc.thePlayer.posX.toInt()
                    val playerZ = mc.thePlayer.posZ.toInt()
                    val xRange = playerX - 30..playerX + 30
                    val zRange = playerZ - 30..playerZ + 30
                    findChest@ for (te in world.loadedTileEntityList) {
                        if (te.pos.y == 75 && te is TileEntityChest && te.numPlayersUsing == 0 && te.pos.x in xRange && te.pos.z in zRange
                        ) {
                            val pos = te.pos
                            if (world.getBlockState(pos.down()).block == Blocks.stone) {
                                for (direction in EnumFacing.HORIZONTALS) {
                                    if (world.getBlockState(pos.offset(direction)).block == Blocks.cobblestone && world.getBlockState(
                                            pos.offset(direction.opposite, 2)
                                        ).block == Blocks.iron_bars) {

                                        val offsetDir = listOf(direction.rotateYCCW(), direction.rotateY()).find {
                                            return@find world.getBlockState(
                                                pos.offset(
                                                    it,
                                                    1
                                                )
                                            ).block == Blocks.torch && world.getBlockState(
                                                pos.offset(
                                                    it.opposite,
                                                    3
                                                )
                                            ).block == Blocks.torch
                                        }?.opposite ?: continue

                                        if (world.getBlockState(
                                                pos.offset(direction.opposite)
                                                    .offset(offsetDir)
                                                    .down(2)
                                            ).block == Blocks.stone_brick_stairs) {
                                            //chestCenter: -11 75 -89; direction: east
                                            val chestCenter = pos.offset(offsetDir)

                                            val starts = Triple(
                                                //three: -33 70 -89
                                                chestCenter.down(5).offset(direction.opposite, 22),
                                                //five: -28 71 -89
                                                chestCenter.down(4).offset(direction.opposite, 17),
                                                //seven: -21 72 -89
                                                chestCenter.down(3).offset(direction.opposite, 10),
                                            )
                                            val ends = Triple(
                                                //three: -29 70 -89
                                                starts.first.offset(direction, 3),
                                                //five: -23 71 -89
                                                starts.second.offset(direction, 5),
                                                //seven: -14 72 -89
                                                starts.third.offset(direction, 7),
                                            )

                                            puzzles = Triple(
                                                IceFillPuzzle(pos, world, starts.first, ends.first, direction),
                                                IceFillPuzzle(pos, world, starts.second, ends.second, direction),
                                                IceFillPuzzle(pos, world, starts.third, ends.third, direction)
                                            )

                                            println(
                                                "An Ice Fill chest is at $pos, is facing $direction and is offset $offsetDir"
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

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!Utils.inDungeons || !Skytils.config.iceFillSolver || "Ice Fill" !in DungeonListener.missingPuzzles) return
        val (three, five, seven) = puzzles ?: return
        val matrixStack = UMatrixStack.Compat.get()
        three.draw(matrixStack, event.partialTicks)
        five.draw(matrixStack, event.partialTicks)
        seven.draw(matrixStack, event.partialTicks)
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        //TODO: Will only stop the scan task, not currently running path finders
        job?.cancel()
        job = null
        puzzles = null
    }

    private class IceFillPuzzle(
        val chestCenter: BlockPos, val world: World, val start: BlockPos, val end: BlockPos, val facing: EnumFacing
    ) {
        private val optimal = SuperSecretSettings.azooPuzzoo
        private var path: List<Vec3>? = null

        init {
            Skytils.launch {
                path = findPath().ifNull {
                    UChat.chat("${Skytils.failPrefix} §cFailed to find a solution for Ice Fill. Please report this on our Discord at discord.gg/skytils.")
                    println("Ice Fill Data: chestCenter=$chestCenter, start=$start, end=$end, facing=$facing, optimal=$optimal")
                }
            }
        }

        private fun findPath(): List<Vec3>? {
            val spaces = getSpaces()

            val moves = spaces.associate {
                val neighbors = EnumFacing.HORIZONTALS.associateBy { direction -> it.offset(direction) }
                    .filterKeys { spot -> spot in spaces }
                    .mapKeys { (pos, _) -> spaces.indexOf(pos) }
                Pair(spaces.indexOf(it), neighbors)
            }

            val startIndex = spaces.indexOf(start)
            val n = spaces.size
            val visited = BooleanArray(n).also { it[startIndex] = true }
            val startPath = IntArray(n) { -1 }.also { it[0] = startIndex }

            if (optimal) {
                val optimizedMoves = Array(n) {
                    moves[it]!!.map { map ->
                        map.key + (map.value.ordinal.toLong() shl 32)
                    }.toLongArray()
                }

                return getOptimalPath(
                    optimizedMoves, n, startIndex, visited, startPath, 1, facing.ordinal, 0, Int.MAX_VALUE
                )?.first?.map { Vec3(spaces.elementAt(it)).addVector(0.5, 0.01, 0.5) }
            } else {
                val simplifiedMoves = moves.mapValues { (_, y) -> y.map { it.key } }

                return getFirstPath(
                    Array(n) { simplifiedMoves[it]!! }, n, startIndex, visited, startPath, 1
                )?.map { Vec3(spaces.elementAt(it)).addVector(0.5, 0.01, 0.5) }
            }
        }

        fun draw(matrixStack: UMatrixStack, partialTicks: Float) {
            path?.let {
                GlStateManager.pushMatrix()
                GlStateManager.disableCull()

                it.zipWithNext { first, second ->
                    RenderUtil.draw3DLine(first, second, 5, Color.MAGENTA, partialTicks, matrixStack, Funny.alphaMult)
                }
                GlStateManager.popMatrix()
            }
        }

        private fun getFirstPath(
            moves: Array<List<Int>>,
            n: Int,
            visiting: Int,
            visited: BooleanArray,
            path: IntArray,
            depth: Int,
        ): List<Int>? {
            if (depth == n) {
                return path.toList()
            }

            val move = moves[visiting]

            for (index in move) {
                if (!visited[index]) {
                    visited[index] = true
                    path[depth] = index

                    getFirstPath(moves, n, index, visited, path, depth + 1)?.let {
                        return it
                    }

                    visited[index] = false
                }
            }

            return null
        }

        private fun getOptimalPath(
            moves: Array<LongArray>,
            n: Int,
            visiting: Int,
            visited: BooleanArray,
            path: IntArray,
            depth: Int,
            lastDirection: Int,
            corners: Int,
            knownLeastCorners: Int
        ): Pair<List<Int>, Int>? {
            if (corners >= knownLeastCorners) {
                return null
            }

            if (depth == n) {
                return Pair(path.toList(), corners)
            }

            var bestPath: List<Int>? = null
            var leastCorners = knownLeastCorners

            val move = moves[visiting]

            for (value in move) {
                val index = value.toInt()
                if (visited[index]) continue

                val direction = (value shr 32).toInt()

                visited[index] = true
                path[depth] = index

                val newCorners = if (lastDirection != direction) corners + 1 else corners

                val newPath = getOptimalPath(
                    moves, n, index, visited, path, depth + 1, direction, newCorners, leastCorners
                )

                if (newPath != null) {
                    bestPath = newPath.first
                    leastCorners = newPath.second
                }

                visited[index] = false
            }

            return bestPath?.let { Pair(it, leastCorners) }
        }

        private fun getSpaces(): List<BlockPos> {
            val spaces = mutableListOf(start)
            val queue = mutableListOf(start)

            while (queue.isNotEmpty()) {
                val current = queue.removeLast()
                EnumFacing.HORIZONTALS.forEach { direction ->
                    val next = current.offset(direction)
                    if (next !in spaces && world.getBlockState(next).block === Blocks.air && Utils.equalsOneOf(
                            world.getBlockState(
                                next.down()
                            ).block, Blocks.ice, Blocks.packed_ice
                        )
                    ) {
                        spaces.add(next)
                        queue.add(next)
                    }
                }
            }

            return spaces
        }
    }
}