package com.tb.chess

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.tb.chess.di.chessModule
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {
        modules(chessModule)
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "ChessApp - KMP",
    ) {
        App()
    }
}
