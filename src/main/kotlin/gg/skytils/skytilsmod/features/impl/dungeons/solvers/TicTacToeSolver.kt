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

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.TickTask
import gg.skytils.skytilsmod.events.impl.skyblock.DungeonEvent
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.tictactoe.AlphaBetaAdvanced
import gg.skytils.skytilsmod.utils.tictactoe.Board
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.experimental.and

object TicTacToeSolver {

    private var topLeft: BlockPos? = null
    private var roomFacing: EnumFacing? = null
    private var board: Board? = null
    private var mappedPositions = HashMap<Int, EntityItemFrame>()
    private var bestMove: BlockPos? = null

    init {
        TickTask(20, repeats = true) {
            if (!Utils.inDungeons || !Skytils.config.ticTacToeSolver || mc.thePlayer == null) return@TickTask
            if (SuperSecretSettings.azooPuzzoo || DungeonListener.missingPuzzles.contains("Tic Tac Toe")) {
                updatePuzzleState()
            } else {
                bestMove = null
            }
        }
    }

    @SubscribeEvent
    fun onPuzzleDiscovered(event: DungeonEvent.PuzzleEvent.Discovered) {
        if (event.puzzle == "Tic Tac Toe") {
            updatePuzzleState()
        }
    }

    fun updatePuzzleState() {
        val frames = mc.theWorld.loadedEntityList.filter {
            if (it !is EntityItemFrame) return@filter false
            val realPos = it.position.down()
            if (it.rotation != 0 || realPos.y !in 70..72) return@filter false
            val item = it.displayedItem
            if (item == null || item.item != Items.filled_map) return@filter false
            val mapData = Items.filled_map.getMapData(item, mc.theWorld) ?: return@filter false
            val colorInt: Int = (mapData.colors[8256] and 255.toByte()).toInt()
            if (colorInt != 114 && colorInt != 33) return@filter false
            val blockBehind = realPos.offset(it.facingDirection.opposite, 1)
            if (mc.theWorld.getBlockState(blockBehind).block != Blocks.iron_block) return@filter false
            return@filter true
        }
        if (topLeft == null || roomFacing == null || board == null) {
            for (frame in frames) {
                if (frame !is EntityItemFrame) continue
                val realPos = frame.position.down()
                val blockBehind = realPos.offset(frame.facingDirection.opposite, 1)
                val row = when (realPos.y) {
                    72 -> 0
                    71 -> 1
                    70 -> 2
                    else -> continue
                }
                val column = when {
                    mc.theWorld.getBlockState(blockBehind.offset(frame.facingDirection.rotateYCCW())).block != Blocks.iron_block -> 2
                    mc.theWorld.getBlockState(blockBehind.offset(frame.facingDirection.rotateY())).block != Blocks.iron_block -> 0
                    else -> 1
                }
                val mapData = Items.filled_map.getMapData(frame.displayedItem, mc.theWorld) ?: continue
                val colorInt: Int = (mapData.colors[8256] and 255.toByte()).toInt()
                val owner = if (colorInt == 114) Board.State.X else Board.State.O
                if (board == null) {
                    topLeft = realPos.up(row).offset(frame.facingDirection.rotateY(), column)
                    roomFacing = frame.facingDirection.opposite
                    board = Board()
                }
                with(board!!) {
                    try {
                        place(column, row, owner)
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                    mappedPositions[row * Board.BOARD_WIDTH + column] = frame
                }
            }
            if (board != null) {
                board!!.turn = if (frames.size % 2 == 0) Board.State.X else Board.State.O
            }
        } else if (!board!!.isGameOver) {
            with(board!!) {
                turn = if (frames.size % 2 == 0) Board.State.X else Board.State.O
                if (turn == Board.State.O) {
                    for (frame in frames) {
                        if (frame !is EntityItemFrame) continue
                        if (!mappedPositions.containsValue(frame)) {
                            val mapData =
                                Items.filled_map.getMapData(frame.displayedItem, mc.theWorld) ?: continue
                            val colorInt: Int = (mapData.colors[8256] and 255.toByte()).toInt()
                            val owner = if (colorInt == 114) Board.State.X else Board.State.O
                            val realPos = frame.position.down()
                            val blockBehind = realPos.offset(frame.facingDirection.opposite, 1)
                            with(board!!) {
                                val row = when (realPos.y) {
                                    72 -> 0
                                    71 -> 1
                                    70 -> 2
                                    else -> -1
                                }
                                val column =
                                    if (mc.theWorld.getBlockState(blockBehind.offset(frame.facingDirection.rotateYCCW())).block != Blocks.iron_block) {
                                        2
                                    } else {
                                        if (mc.theWorld.getBlockState(blockBehind.offset(frame.facingDirection.rotateY())).block != Blocks.iron_block) {
                                            0
                                        } else 1
                                    }
                                place(column, row, owner)
                                mappedPositions[row * Board.BOARD_WIDTH + column] = frame
                            }
                        }
                    }
                    AlphaBetaAdvanced.run(this)
                    val move =
                        if (!SuperSecretSettings.bennettArthur) algorithmBestMove else availableMoves.randomOrNull()
                            ?: -1
                    if (move != -1) {
                        val column = move % Board.BOARD_WIDTH
                        val row = move / Board.BOARD_WIDTH
                        bestMove = topLeft!!.down(row).offset(roomFacing!!.rotateY(), column)
                    }
                } else {
                    bestMove = null
                }
            }
        } else {
            bestMove = null
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        topLeft = null
        roomFacing = null
        board = null
        bestMove = null
        mappedPositions.clear()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Utils.inDungeons || !Skytils.config.ticTacToeSolver) return
        if (bestMove != null) {
            RenderUtil.drawOutlinedBoundingBox(
                AxisAlignedBB(bestMove, bestMove!!.add(1, 1, 1)),
                Skytils.config.ticTacToeSolverColor,
                3f,
                event.partialTicks
            )
        }
    }
}