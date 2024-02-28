package com.merelythis.compose.conway.model

import androidx.compose.ui.res.stringResource
import com.merelythis.compose.conway.R

/**
 * @author : zhangjing
 * @date : 星期二 2/27/24
 */
data class GameState(
    val isPaused: Boolean = false,
    val generation: Int = 0,
    val isStillLife: Boolean = false,
    val cells: Matrix<Boolean>
) {

    companion object {
        const val STATUS_READY = 0
        const val STATUS_PROGRESSING = 1
        const val STATUS_PAUSED = 2
        const val STATUS_DIED = 3
    }

    val population: Int by lazy {
        cells.count { it }
    }

    val status: Int
        get() {
            return when {
                isStillLife -> STATUS_READY
                !isPaused -> STATUS_PROGRESSING
                isPaused && generation == 0 && population == 0 -> STATUS_READY
                isPaused && generation == 0 && population > 0 -> STATUS_PAUSED
                isPaused && generation > 0 && population == 0 -> STATUS_DIED
                else -> STATUS_PAUSED
            }
        }
}