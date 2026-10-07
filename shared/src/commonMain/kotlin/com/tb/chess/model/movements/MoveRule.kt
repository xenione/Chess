package com.tb.chess.model.movements

import com.tb.chess.model.ChessPiece
import com.tb.chess.model.Position

interface MoveRule {

    fun isValid(piece: ChessPiece, from: Position, to: Position): Boolean
}