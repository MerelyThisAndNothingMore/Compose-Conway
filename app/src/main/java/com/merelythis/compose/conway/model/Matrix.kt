package com.merelythis.compose.conway.model

/**
 * @author : zhangjing
 * @date : 星期二 2/27/24
 */
class Matrix<T>(
    val rowNum: Int,
    val colNum: Int,
    private val init: (Int, Int) -> T
) {

    private val _values = mutableListOf<T>().apply {
        repeat(rowNum * colNum) { i ->
            val r = i / colNum
            add(init(r, i % colNum))
        }
    }

    operator fun get(row: Int, col: Int): T {
        return _values[index(row, col)]
    }

    operator fun set(row: Int, col: Int, v: T) {
        _values[index(row, col)] = v
    }

    fun count(f: (T) -> Boolean): Int {
        return _values.count { f(it) }
    }

    fun isSame(other: Matrix<T>): Boolean {
        return other._values.filterIndexed { index, item ->
            item != this._values[index]
        }.isEmpty()
    }

    fun copyWith(row: Int, col: Int, v: T): Matrix<T> {
        val newMatrix = Matrix(rowNum, colNum) { _, _ -> init(0, 0) }
        _values.forEachIndexed { index, item ->
            val r = index / colNum
            val c = index % colNum
            newMatrix[r, c] = if (r == row && c == col) v else item
        }
        return newMatrix
    }

    private fun index(row: Int, col: Int): Int {
        return row * this.colNum + col
    }
}