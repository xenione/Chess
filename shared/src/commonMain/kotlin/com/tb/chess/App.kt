package com.tb.chess

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.tb.chess.ui.ChessGameScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        ChessGameScreen()
    }
}
