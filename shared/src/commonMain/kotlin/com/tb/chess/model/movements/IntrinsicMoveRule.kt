package com.tb.chess.model.movements

import com.tb.chess.model.ChessPiece
import com.tb.chess.model.Position

class IntrinsicMoveRule: MoveRule {

    override fun isValid(piece: ChessPiece, from: Position, to: Position): Boolean {
        return piece.isIntrinsicalMoveValid(from, to)
    }
}