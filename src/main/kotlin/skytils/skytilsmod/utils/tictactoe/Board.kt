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

package skytils.skytilsmod.utils.tictactoe

import java.lang.StringBuilder
import java.util.HashSet

/**
 * Represents the Tic Tac Toe board.
 * Modified version of LazoCoder's Tic-Tac-Toe Java Implementation, GPLv3 License
 * @link https://github.com/LazoCoder/Tic-Tac-Toe
 */
class Board internal constructor() {
    enum class State {
        Blank, X, O
    }

    private val board: Array<Array<State?>>

    /**
     * Check to see who's turn it is.
     * @return          the player who's turn it is
     */
    var turn: State = State.X

    /**
     * Check to see who won.
     * @return          the player who won (or Blank if the game is a draw)
     */
    var winner: State? = null
        get() {
            //check(isGameOver) { "TicTacToe is not over yet." }
            return field
        }

    /**
     * Get the indexes of all the positions on the board that are empty.
     * @return          the empty cells
     */
    var availableMoves: HashSet<Int>
        private set
    private var moveCount = 0

    /**
     * Check to see if the game is over (if there is a winner or a draw).
     * @return          true if the game is over
     */
    var isGameOver = false
        private set

    var algorithmBestMove = -1

    /**
     * Set the cells to be blank and load the available moves (all the moves are
     * available at the start of the game).
     */
    private fun initialize() {
        for (row in 0 until BOARD_WIDTH) {
            for (col in 0 until BOARD_WIDTH) {
                board[row][col] = State.Blank
            }
        }
        availableMoves.clear()
        for (i in 0 until BOARD_WIDTH * BOARD_WIDTH) {
            availableMoves.add(i)
        }
    }

    /**
     * Restart the game with a new blank board.
     */
    fun reset() {
        moveCount = 0
        isGameOver = false
        turn = State.X
        winner = State.Blank
        initialize()
    }

    /**
     * Places an X or an O on the specified index depending on whose turn it is.
     * @param index     the position on the board (example: index 4 is location (0, 1))
     * @return          true if the move has not already been played
     */
    fun move(index: Int): Boolean {
        return move(index % BOARD_WIDTH, index / BOARD_WIDTH)
    }

    /**
     * Places an X or an O on the specified location depending on who turn it is.
     * @param x         the x coordinate of the location
     * @param y         the y coordinate of the location
     * @return          true if the move has not already been played
     */
    fun move(x: Int, y: Int): Boolean {
        check(!isGameOver) { "TicTacToe is over. No moves can be played." }
        if (board[y][x] == State.Blank) {
            board[y][x] = turn
        } else {
            return false
        }
        moveCount++
        availableMoves.remove(y * BOARD_WIDTH + x)

        // The game is a draw.
        if (moveCount == BOARD_WIDTH * BOARD_WIDTH) {
            winner = State.Blank
            isGameOver = true
        }

        // Check for a winner.
        checkRow(y)
        checkColumn(x)
        checkDiagonalFromTopLeft(x, y)
        checkDiagonalFromTopRight(x, y)
        turn = if (turn == State.X) State.O else State.X
        return true
    }

    /**
     * Places an X or an O on the specified location based on a parameter
     * @param x         the x coordinate of the location
     * @param y         the y coordinate of the location
     * @param player    whether or not an X or an O should be played
     * @return          true if the move has not already been played
     */
    fun place(x: Int, y: Int, player: State): Boolean {
        check(!isGameOver) { "TicTacToe is over. No moves can be played." }
        if (board[y][x] == State.Blank) {
            board[y][x] = player
        } else {
            return false
        }
        moveCount++
        availableMoves.remove(y * BOARD_WIDTH + x)

        // The game is a draw.
        if (moveCount == BOARD_WIDTH * BOARD_WIDTH) {
            winner = State.Blank
            isGameOver = true
        }

        // Check for a winner.
        checkRow(y)
        checkColumn(x)
        checkDiagonalFromTopLeft(x, y)
        checkDiagonalFromTopRight(x, y)
        turn = if (turn == State.X) State.O else State.X
        return true
    }

    /**
     * Get a copy of the array that represents the board.
     * @return          the board array
     */
    fun toArray(): Array<Array<State?>> {
        return board.clone()
    }

    /**
     * Checks the specified row to see if there is a winner.
     * @param row       the row to check
     */
    private fun checkRow(row: Int) {
        for (i in 1 until BOARD_WIDTH) {
            if (board[row][i] != board[row][i - 1]) {
                break
            }
            if (i == BOARD_WIDTH - 1) {
                winner = turn
                isGameOver = true
            }
        }
    }

    /**
     * Checks the specified column to see if there is a winner.
     * @param column    the column to check
     */
    private fun checkColumn(column: Int) {
        for (i in 1 until BOARD_WIDTH) {
            if (board[i][column] != board[i - 1][column]) {
                break
            }
            if (i == BOARD_WIDTH - 1) {
                winner = turn
                isGameOver = true
            }
        }
    }

    /**
     * Check the left diagonal to see if there is a winner.
     * @param x         the x coordinate of the most recently played move
     * @param y         the y coordinate of the most recently played move
     */
    private fun checkDiagonalFromTopLeft(x: Int, y: Int) {
        if (x == y) {
            for (i in 1 until BOARD_WIDTH) {
                if (board[i][i] != board[i - 1][i - 1]) {
                    break
                }
                if (i == BOARD_WIDTH - 1) {
                    winner = turn
                    isGameOver = true
                }
            }
        }
    }

    /**
     * Check the right diagonal to see if there is a winner.
     * @param x     the x coordinate of the most recently played move
     * @param y     the y coordinate of the most recently played move
     */
    private fun checkDiagonalFromTopRight(x: Int, y: Int) {
        if (BOARD_WIDTH - 1 - x == y) {
            for (i in 1 until BOARD_WIDTH) {
                if (board[BOARD_WIDTH - 1 - i][i] != board[BOARD_WIDTH - i][i - 1]) {
                    break
                }
                if (i == BOARD_WIDTH - 1) {
                    winner = turn
                    isGameOver = true
                }
            }
        }
    }

    /**
     * Get a deep copy of the Tic Tac Toe board.
     * @return      an identical copy of the board
     */
    val deepCopy: Board
        get() {
            val board = Board()
            for (i in board.board.indices) {
                board.board[i] = this.board[i].clone()
            }
            board.turn = turn
            board.winner = winner
            board.availableMoves = HashSet()
            board.availableMoves.addAll(availableMoves)
            board.moveCount = moveCount
            board.isGameOver = isGameOver
            return board
        }

    override fun toString(): String {
        val sb = StringBuilder()
        for (y in 0 until BOARD_WIDTH) {
            for (x in 0 until BOARD_WIDTH) {
                if (board[y][x] == State.Blank) {
                    sb.append("-")
                } else {
                    sb.append(board[y][x]!!.name)
                }
                sb.append(" ")
            }
            if (y != BOARD_WIDTH - 1) {
                sb.append("\n")
            }
        }
        return String(sb)
    }

    companion object {
        const val BOARD_WIDTH = 3
    }

    /**
     * Construct the Tic Tac Toe board.
     */
    init {
        board = Array(BOARD_WIDTH) { arrayOfNulls(BOARD_WIDTH) }
        availableMoves = HashSet()
        reset()
    }
}