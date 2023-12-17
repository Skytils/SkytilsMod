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
import gg.skytils.skytilsmod.core.tickTimer
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
import net.minecraft.world.storage.MapData
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.experimental.and

object TicTacToeSolver {

    private const val MAP_COLOR_INDEX = 8256
    private const val COLOR_INT_X = 114
    private const val COLOR_INT_O = 33
    private var topLeft: BlockPos? = null
    private var roomFacing: EnumFacing? = null
    private var board: Board? = null
    private var mappedPositions = HashMap<Int, EntityItemFrame>()
    private var bestMove: BlockPos? = null

    init {
        tickTimer(20, repeats = true) {
            if (!Utils.inDungeons || !Skytils.config.ticTacToeSolver || mc.thePlayer == null) return@tickTimer
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

    @Throws(IllegalStateException::class)
    fun updatePuzzleState() {
        val frames = getBoardFrames()
        if (topLeft == null || roomFacing == null || board == null) {
            parseInitialState(frames)
        } else if (!board!!.isGameOver) {
            board!!.turn = if (frames.size % 2 == 0) Board.State.X else Board.State.O
            if (board!!.turn == Board.State.O) {
                for (frame in frames) {
                    if (frame !in mappedPositions.values) {
                        val (row, column) = getBoardPosition(frame)
                        board!!.place(column, row, getSpotOwner(frame))
                        mappedPositions[row * Board.BOARD_WIDTH + column] = frame
                    }
                }
                AlphaBetaAdvanced.run(board!!)
                val move =
                    if (!SuperSecretSettings.bennettArthur) board!!.algorithmBestMove else board!!.availableMoves.randomOrNull()
                        ?: -1
                if (move != -1) {
                    val column = move % Board.BOARD_WIDTH
                    val row = move / Board.BOARD_WIDTH
                    bestMove = topLeft!!.down(row).offset(roomFacing!!.rotateY(), column)
                }
            } else {
                bestMove = null
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

    @Suppress("UNCHECKED_CAST")
    private fun getBoardFrames(): List<EntityItemFrame> = mc.theWorld.loadedEntityList.filter {
        it is EntityItemFrame &&
                it.rotation == 0 &&
                it.position.down().let { realPos -> realPos.y in 70..72 } &&
                it.displayedItem?.let { item ->
                    item.item == Items.filled_map &&
                            Items.filled_map.getMapData(item, mc.theWorld)?.let { mapData ->
                                mapData[MAP_COLOR_INDEX].let { colorInt ->
                                    colorInt == COLOR_INT_X || colorInt == COLOR_INT_O
                                }
                            } ?: false
                } ?: false &&
                mc.theWorld.getBlockState(it.position.down().offset(it.facingDirection.opposite, 1)).block == Blocks.iron_block
    } as List<EntityItemFrame>

    private fun parseInitialState(frames: List<EntityItemFrame>) {
        for (frame in frames) {
            val (row, column) = getBoardPosition(frame)
            if (board == null) {
                topLeft = frame.position.up(row-1).offset(frame.facingDirection.rotateY(), column)
                roomFacing = frame.facingDirection.opposite
                board = Board()
            }
            board!!.place(column, row, getSpotOwner(frame))
            mappedPositions[row * Board.BOARD_WIDTH + column] = frame
        }
        if (board != null) {
            board!!.turn = if (frames.size % 2 == 0) Board.State.X else Board.State.O
        }
    }

    private operator fun MapData.get(index: Int): Int {
        return (this.colors[index] and 255.toByte()).toInt()
    }

    private fun getMapData(entity: EntityItemFrame) = Items.filled_map.getMapData(entity.displayedItem, mc.theWorld)

    private fun getBoardPosition(frame: EntityItemFrame): Pair<Int, Int> {
        val realPos = frame.position.down()
        val blockBehind = realPos.offset(frame.facingDirection.opposite)

        val row = 72 - realPos.y
        val column = when {
            mc.theWorld.getBlockState(blockBehind.offset(frame.facingDirection.rotateYCCW())).block != Blocks.iron_block -> 2
            mc.theWorld.getBlockState(blockBehind.offset(frame.facingDirection.rotateY())).block != Blocks.iron_block -> 0
            else -> 1
        }

        return row to column
    }

    private fun getSpotOwner(frame: EntityItemFrame): Board.State {
        val mapData = getMapData(frame) ?: error("Non map checked")
        return if (mapData[MAP_COLOR_INDEX] == COLOR_INT_X) Board.State.X else Board.State.O
    }
}