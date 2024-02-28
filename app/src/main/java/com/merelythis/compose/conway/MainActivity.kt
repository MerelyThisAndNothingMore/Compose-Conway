package com.merelythis.compose.conway

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.merelythis.compose.conway.ui.theme.ConwayComposeTheme
import com.merelythis.compose.conway.ui.view.GameBoard

class MainActivity : ComponentActivity() {

    companion object {
        const val CELL_SIZE_DP = 15
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConwayComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels
                    val cellWidth =
                        CELL_SIZE_DP * LocalContext.current.resources.displayMetrics.density

                    val col = (screenWidth / cellWidth).toInt()
                    val row = (col * 1.25).toInt()
                    GameBoard(row, col)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ConwayComposeTheme {
        GameBoard(30, 30)
    }
}