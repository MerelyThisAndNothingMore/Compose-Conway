package com.merelythis.compose.conway.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * @author : zhangjing
 * @date : 星期二 2/27/24
 */

class GameInfo(private val gameRow: Int, private val gameCol: Int) {

    val state: MutableState<GameState> by lazy {
        mutableStateOf(
            GameState(
                isPaused = true,
                generation = 0,
                cells = Matrix(gameRow, gameCol) { _, _ ->
                    false
                }
            )
        )
    }

    fun reset() {
        state.value = GameState(
            isPaused = true,
            generation = 0,
            cells = Matrix(gameRow, gameCol) { _, _ -> false }
        )
    }

    fun selectCell(row: Int, col: Int) {
        val newValue = state.value.cells[row, col].not()
        state.value = state.value.copy(
            cells = state.value.cells.copyWith(row, col, newValue)
        )
    }

    fun activateCell(row: Int, col: Int) {
        state.value = state.value.copy(
            cells = state.value.cells.copyWith(row, col, true)
        )
    }

    fun deactivateCell(row: Int, col: Int) {
        state.value = state.value.copy(
            cells = state.value.cells.copyWith(row, col, false)
        )
    }

    fun pauseOrResume(pause: Boolean? = null) {
        state.value = state.value.copy(isPaused = pause ?: state.value.isPaused.not())
    }

    fun nextGeneration() {
        val newMatrix = Matrix(gameRow, gameCol) { r, c ->
            state.value.cells.canLiveAhead(r, c)
        }
        val newCount = newMatrix.count { it }
        val oldCount = state.value.cells.count { it }
        val isStill = (newCount > 0 && newCount == oldCount) && newMatrix.isSame(state.value.cells)
        state.value = state.value.copy(
            generation = state.value.generation + 1,
            cells = newMatrix,
            isStillLife = isStill,
        )
    }

    private fun Matrix<Boolean>.canLiveAhead(row: Int, col: Int): Boolean {
        val count = countNeighbours(row, col)
        return if (state.value.cells[row, col]) {
            count in 2..3
        } else {
            count == 3
        }
    }

    private fun countNeighbours(row: Int, col: Int): Int {
        var count = 0
        for (r in (row - 1)..(row + 1)) {
            for (c in (col - 1)..(col + 1)) {
                if (r in 0 until gameRow && c in 0 until gameCol && !(r == row && c == col) && state.value.cells[r, c]) {
                    count++
                }
            }
        }
        return count
    }
}