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

package gg.skytils.skytilsmod.utils.tictactoe

/**
 * Uses the Alpha-Beta Pruning algorithm to play a move in a game of Tic Tac Toe
 * but includes depth in the evaluation function.
 *
 * The vanilla MiniMax algorithm plays perfectly but it may occasionally
 * decide to make a move that will results in a slower victory or a faster loss.
 * For example, playing the move 0, 1, and then 7 gives the AI the opportunity
 * to play a move at index 6. This would result in a victory on the diagonal.
 * But the AI does not choose this move, instead it chooses another one. It
 * still wins inevitably, but it chooses a longer route. By adding the depth
 * into the evaluation function, it allows the AI to pick the move that would
 * make it win as soon as possible.
 *
 * Modified version of LazoCoder's Tic-Tac-Toe Java Implementation, GPLv3 License
 * @link https://github.com/LazoCoder/Tic-Tac-Toe
 */
internal object AlphaBetaAdvanced {
    private var maxPly = 0.0

    /**
     * Play using the Alpha-Beta Pruning algorithm. Include depth in the
     * evaluation function and a depth limit.
     * @param board     the Tic Tac Toe board to play on
     * @param ply       the maximum depth
     */
    fun run(board: Board, ply: Double = Double.POSITIVE_INFINITY): Int {
        return run(board.turn, board, ply)
    }

    /**
     * Execute the algorithm.
     * @param player        the player that the AI will identify as
     * @param board         the Tic Tac Toe board to play on
     * @param maxPly        the maximum depth
     * @return              the score of the move
     */
    private fun run(player: Board.State, board: Board, maxPly: Double): Int {
        require(maxPly >= 1) { "Maximum depth must be greater than 0." }
        AlphaBetaAdvanced.maxPly = maxPly
        return alphaBetaPruning(player, board, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0)
    }

    /**
     * The meat of the algorithm.
     * @param player        the player that the AI will identify as
     * @param board         the Tic Tac Toe board to play on
     * @param alpha         the alpha value
     * @param beta          the beta value
     * @param currentPly    the current depth
     * @return              the index of the move to make
     */
    private fun alphaBetaPruning(player: Board.State, board: Board, alpha: Double, beta: Double, currentPly: Int): Int {
        var cp = currentPly
        if (cp++.toDouble() == maxPly || board.isGameOver) {
            return score(player, board, cp)
        }
        return if (board.turn == player) {
            getMax(player, board, alpha, beta, cp)
        } else {
            getMin(player, board, alpha, beta, cp)
        }
    }

    /**
     * Play the move with the highest score.
     * @param player        the player that the AI will identify as
     * @param board         the Tic Tac Toe board to play on
     * @param alpha         the alpha value
     * @param beta          the beta value
     * @param currentPly    the current depth
     * @return              the index of the move to make
     */
    private fun getMax(player: Board.State, board: Board, alpha: Double, beta: Double, currentPly: Int): Int {
        var a = alpha
        var indexOfBestMove = -1
        for (theMove in board.availableMoves) {
            val modifiedBoard = board.deepCopy
            modifiedBoard.move(theMove)
            val score = alphaBetaPruning(player, modifiedBoard, a, beta, currentPly)
            if (score > a) {
                a = score.toDouble()
                indexOfBestMove = theMove
            }
            if (a >= beta) {
                break
            }
        }
        if (indexOfBestMove != -1) {
            board.algorithmBestMove = indexOfBestMove
        }
        return a.toInt()
    }

    /**
     * Play the move with the lowest score.
     * @param player        the player that the AI will identify as
     * @param board         the Tic Tac Toe board to play on
     * @param alpha         the alpha value
     * @param beta          the beta value
     * @param currentPly    the current depth
     * @return              the score of the move
     */
    private fun getMin(player: Board.State, board: Board, alpha: Double, beta: Double, currentPly: Int): Int {
        var b = beta
        var indexOfBestMove = -1
        for (theMove in board.availableMoves) {
            val modifiedBoard = board.deepCopy
            modifiedBoard.move(theMove)
            val score = alphaBetaPruning(player, modifiedBoard, alpha, b, currentPly)
            if (score < b) {
                b = score.toDouble()
                indexOfBestMove = theMove
            }
            if (alpha >= b) {
                break
            }
        }
        if (indexOfBestMove != -1) {
            board.algorithmBestMove = indexOfBestMove
        }
        return b.toInt()
    }

    /**
     * Get the score of the board. Takes depth into account.
     * @param player        the play that the AI will identify as
     * @param board         the Tic Tac Toe board to play on
     * @param currentPly    the current depth
     * @return              the score of the move
     */
    private fun score(player: Board.State, board: Board, currentPly: Int): Int {
        require(player != Board.State.Blank) { "Player must be X or O." }
        val opponent = if (player == Board.State.X) Board.State.O else Board.State.X
        return if (board.isGameOver && board.winner == player) {
            10 - currentPly
        } else if (board.isGameOver && board.winner == opponent) {
            -10 + currentPly
        } else {
            0
        }
    }
}