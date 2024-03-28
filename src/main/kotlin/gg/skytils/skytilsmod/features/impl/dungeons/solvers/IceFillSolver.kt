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
import gg.essential.universal.UKeyboard
import gg.essential.universal.UMatrixStack
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.misc.Funny
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.RenderUtil
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
            //TODO: DEBUG
            if (UKeyboard.isAltKeyDown() && UKeyboard.isKeyDown(UKeyboard.KEY_D)) {
                job = null
                puzzles = null
            }
            //TODO: DEBUG
            if (!Utils.inDungeons || !Skytils.config.iceFillSolver || "Ice Fill" !in DungeonListener.missingPuzzles ||
                puzzles != null || job?.isActive == true
            ) return@tickTimer
            val player = mc.thePlayer ?: return@tickTimer
            val world = mc.theWorld ?: return@tickTimer

            job = Skytils.launch {
                world.loadedTileEntityList.filterIsInstance<TileEntityChest>().filter {
                    it.numPlayersUsing == 0 && it.pos.y == 75 && it.getDistanceSq(
                        player.posX, player.posY, player.posZ
                    ) < 750
                }.map { chest ->
                    val pos = chest.pos
                    EnumFacing.HORIZONTALS.firstOrNull {
                        world.getBlockState(pos.down()).block == Blocks.stone && world.getBlockState(pos.offset(it)).block == Blocks.cobblestone && world.getBlockState(
                            pos.offset(it.opposite, 2)
                        ).block == Blocks.iron_bars && world.getBlockState(
                            pos.offset(
                                it.rotateY(), 2
                            )
                        ).block == Blocks.torch && world.getBlockState(
                            pos.offset(
                                it.rotateYCCW(), 2
                            )
                        ).block == Blocks.torch && world.getBlockState(
                            pos.offset(it.opposite).down(2)
                        ).block == Blocks.stone_brick_stairs
                    }?.let {
                        Pair(chest, it)
                    }
                }.firstOrNull()?.let {
                    //chest: BlockPos{x=-11, y=75, z=-89}; direction: east
                    val (chest, direction) = it
                    val pos = chest.pos

                    val starts = Triple(
                        //three: BlockPos{x=-33, y=70, z=-89}
                        pos.down(5).offset(direction.opposite, 22),
                        //five: BlockPos{x=-28, y=71, z=-89}
                        pos.down(4).offset(direction.opposite, 17),
                        //seven: BlockPos{x=-21, y=72, z=-89}
                        pos.down(3).offset(direction.opposite, 10),
                    )
                    val ends = Triple(
                        //three: BlockPos{x=-29, y=70, z=-89}
                        starts.first.offset(direction, 3),
                        //five: BlockPos{x=-23, y=71, z=-89}
                        starts.second.offset(direction, 5),
                        //seven: BlockPos{x=-14, y=72, z=-89}
                        starts.third.offset(direction, 7),
                    )

                    puzzles = Triple(
                        IceFillPuzzle(world, starts.first, ends.first, direction),
                        IceFillPuzzle(world, starts.second, ends.second, direction),
                        IceFillPuzzle(world, starts.third, ends.third, direction)
                    )
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        //TODO: DEBUG
        if (!Utils.inDungeons || !Skytils.config.iceFillSolver || "Ice Fill" !in DungeonListener.missingPuzzles) return
        val (three, five, seven) = puzzles ?: return
        val matrixStack = UMatrixStack.Compat.get()
        three.draw(matrixStack, event.partialTicks)
        five.draw(matrixStack, event.partialTicks)
        seven.draw(matrixStack, event.partialTicks)
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        job = null //TODO: Does not stop getPaths method while running
        puzzles = null
    }

    private class IceFillPuzzle(val world: World, val start: BlockPos, val end: BlockPos, val facing: EnumFacing) {
        private val path = getBestPath().ifNull {
            UChat.chat("${Skytils.failPrefix} §cFailed to find a solution for Ice Fill.")
            println("Ice Fill Data: start=$start, end=$end, facing=$facing")
        }

        fun draw(matrixStack: UMatrixStack, partialTicks: Float) {
            GlStateManager.pushMatrix()
            GlStateManager.disableCull()
            path?.zipWithNext { first, second ->
                RenderUtil.draw3DLine(
                    Vec3(first).addVector(0.5, 0.01, 0.5),
                    Vec3(second).addVector(0.5, 0.01, 0.5),
                    5,
                    Color.GREEN,
                    partialTicks,
                    matrixStack,
                    Funny.alphaMult
                )
            }
            GlStateManager.popMatrix()
        }

        private fun getBestPath(): List<BlockPos>? {
            val spaces = getSpaces()

            //TODO: Can be combined with getSpaces, I just can't be bothered
            val moves: Map<BlockPos, Map<BlockPos, EnumFacing>> = spaces.associateWith {
                EnumFacing.HORIZONTALS.associateBy { direction ->
                    it.offset(direction)
                }.filterKeys { spot -> spot in spaces }
            }

            var bestPath: List<BlockPos>? = null
            var leastCorners = Int.MAX_VALUE

            //TODO: Rework this method
            fun getPaths(
                moves: Map<BlockPos, Map<BlockPos, EnumFacing>>,
                v: BlockPos,
                visited: HashSet<BlockPos>,
                path: MutableList<BlockPos>,
                n: Int,
                lastDirection: EnumFacing,
                corners: Int
            ) {
                val move = moves[v] ?: return
                if (path.size == n && v == end) {
                    val newPath: List<BlockPos> = path.toList()
                    leastCorners = corners
                    bestPath = newPath
                } else if (move.containsKey(end) && path.size != n - 1) {
                    return
                } else {
                    move.forEach { w ->
                        if (!visited.contains(w.key)) {
                            visited.add(w.key)
                            path.add(w.key)

                            val newCorners = if (lastDirection != w.value) corners + 1 else corners
                            if (newCorners >= leastCorners) {
                                return@forEach
                            }

                            getPaths(moves, w.key, visited, path, n, w.value, newCorners)

                            visited.remove(w.key)
                            path.remove(w.key)
                        }
                    }
                }
            }

            getPaths(moves, start, hashSetOf(start), mutableListOf(start), spaces.size, facing, 0)
            return bestPath
        }

        private fun getSpaces(): Set<BlockPos> {
            val spaces = hashSetOf(start)
            val queue = mutableListOf(start)

            while (queue.isNotEmpty()) {
                val current = queue.removeAt(0)
                EnumFacing.HORIZONTALS.forEach { direction ->
                    val next = current.offset(direction)
                    if (next !in spaces && Utils.equalsOneOf(
                            world.getBlockState(next.down()).block, Blocks.ice, Blocks.packed_ice
                        ) && world.getBlockState(next).block === Blocks.air
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