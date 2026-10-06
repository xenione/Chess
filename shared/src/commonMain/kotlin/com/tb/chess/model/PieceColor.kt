package com.tb.chess.model

enum class PieceColor {
    WHITE,
    BLACK;

    fun opposite(): PieceColor = when (this) {
        WHITE -> BLACK
        BLACK -> WHITE
    }
}
