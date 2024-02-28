package com.merelythis.compose.conway.ui.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.merelythis.compose.conway.R
import com.merelythis.compose.conway.model.GameInfo
import com.merelythis.compose.conway.model.GameState
import com.merelythis.compose.conway.model.Matrix
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.floor

/**
 * @author : zhangjing
 * @date : 星期二 2/27/24
 */


const val CELL_SIZE_DP = 15
const val TAG = "ConWayGameBoard"


@Preview
@Composable
fun GameBoard(rows: Int = 20, cols: Int = 20) {

    val game = remember { GameInfo(rows, cols) }

    val gameState = remember { game.state }

    LaunchedEffect(!gameState.value.isPaused) {

        while (!gameState.value.isPaused) {
            game.nextGeneration()
            delay(timeMillis = 150)
        }

    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        GameBoardTable(gameState.value.cells) { r, c, activate ->
            if (activate) {
                game.activateCell(r, c)
            } else {
                game.deactivateCell(r, c)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        @Composable
        fun getGameStatusText(status: Int): String {
            return when (status) {
                GameState.STATUS_READY -> stringResource(id = R.string.conway_start_tip)
                GameState.STATUS_PROGRESSING -> stringResource(id = R.string.conway_progressing)
                GameState.STATUS_PAUSED -> stringResource(id = R.string.conway_resume_tip)
                GameState.STATUS_DIED -> stringResource(id = R.string.conway_died_tip)
                else -> stringResource(id = R.string.conway_resume_tip)
            }
        }

        Text(
            getGameStatusText(status = gameState.value.status),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row {

            Text(stringResource(id = R.string.conway_generation, gameState.value.generation))

            Spacer(modifier = Modifier.width(16.dp))

            Text(text = stringResource(id = R.string.conway_population, gameState.value.population))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {

            Button(onClick = {

                game.pauseOrResume(pause = true)
                game.nextGeneration()

            }) {
                Text(stringResource(id = R.string.conway_next))
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                game.pauseOrResume()
            }) {
                Text(
                    if (gameState.value.isPaused) stringResource(id = R.string.conway_start)
                    else stringResource(id = R.string.conway_pause)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                game.reset()
            }) {
                Text(stringResource(id = R.string.conway_reset))
            }
        }
    }
}

@Composable
fun GameBoardTable(matrix: Matrix<Boolean>, onClick: (Int, Int, activate: Boolean) -> Unit) {
    var tablePosition = Offset.Zero
    var lastSelectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var activate by remember { mutableStateOf(true) }

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.onGloballyPositioned {
            tablePosition = it.positionInWindow()
        }
    ) {

        val cellSize = CELL_SIZE_DP.dp.toPx()
        (0 until matrix.rowNum).forEach { currentRow ->
            GameBoardRow(currentRow, matrix, { isAlive ->
                lastSelectedCell = null
                activate = !isAlive
            }) { x, y ->
                Log.d(
                    TAG,
                    "onMotionAt: x$x, y$y, tablePosition$tablePosition, cellSize${cellSize}"
                )
                val xOffset = x - tablePosition.x
                val yOffset = y - tablePosition.y
                val col =
                    floor(xOffset / cellSize).toInt().coerceIn(0, matrix.colNum - 1)
                val row =
                    floor(yOffset / cellSize).toInt().coerceIn(0, matrix.rowNum - 1)

                // 插值处理
                if (lastSelectedCell != Pair(row, col)) {
                    fillPath(lastSelectedCell, Pair(row, col)) { r, c ->
                        onClick(r, c, activate)
                    }
                    lastSelectedCell = Pair(row, col)
                }
            }
        }
    }
}

private fun fillPath(
    startIndex: Pair<Int, Int>?,
    endIndex: Pair<Int, Int>,
    fillAction: (Int, Int) -> Unit
) {
    if (startIndex == null) {
        fillAction(endIndex.first, endIndex.second)
        return
    }
    val (x0, y0) = startIndex
    val (x1, y1) = endIndex
    val dx = abs(x1 - x0)
    val dy = -abs(y1 - y0)
    var err = dx + dy
    var e2: Int
    var x = x0
    var y = y0

    while (true) {
        fillAction(x, y)
        if (x == x1 && y == y1) break
        e2 = 2 * err
        if (e2 >= dy) {
            err += dy
            x += if (x0 < x1) 1 else -1
        }
        if (e2 <= dx) {
            err += dx
            y += if (y0 < y1) 1 else -1
        }
    }

}

@Composable
fun Dp.toPx(): Float {
    // 将dp值转换为px值
    return with(LocalDensity.current) { toPx() }
}

@Composable
fun GameBoardRow(
    currentRow: Int,
    matrix: Matrix<Boolean>,
    onDragStart: (isAlive: Boolean) -> Unit,
    onDrag: (Int, Int) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {

        (0 until matrix.colNum).forEach { currentColumn ->


            var boxPosition = Offset.Zero
            val isAlive by rememberUpdatedState(newValue = matrix[currentRow, currentColumn])

            Box(
                modifier = Modifier
                    .width(CELL_SIZE_DP.dp)
                    .height(CELL_SIZE_DP.dp)
                    .background(color = if (isAlive) Color.Black else Color.White)
                    .onGloballyPositioned {
                        boxPosition = it.positionInWindow()
                    }
                    .border(
                        width = 1.dp,
                        color = Color(android.graphics.Color.parseColor("#EEEEEE"))
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                onDragStart(isAlive)
                            },
                            onDrag = { change, _ ->

                                // 计算触摸点相对于 Column 的局部坐标
                                var (x, y) = change.position

                                // 坐标矫正，获取全局坐标
                                x += boxPosition.x
                                y += boxPosition.y

                                onDrag(x.toInt(), y.toInt())
                            })
                    },
            )
        }
    }
}