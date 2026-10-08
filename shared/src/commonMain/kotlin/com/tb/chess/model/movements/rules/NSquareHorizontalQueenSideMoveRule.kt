package com.tb.chess.model.movements.rules

import com.tb.chess.model.ChessPiece
import com.tb.chess.model.Position

class NSquareHorizontalQueenSideMoveRule(private val n: Int) : MoveRule {

    override fun isValid(piece: ChessPiece, from: Position, to: Position): Boolean {
        val rPosition = piece.relativePosition(from, to)
        return rPosition.colDiff == -n && rPosition.rowDiff == 0
    }
}