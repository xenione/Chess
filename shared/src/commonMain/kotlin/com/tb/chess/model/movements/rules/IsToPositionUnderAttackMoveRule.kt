package com.tb.chess.model.movements.rules

import com.tb.chess.model.ChessBoard
import com.tb.chess.model.ChessPiece
import com.tb.chess.model.Position

class IsToPositionUnderAttackMoveRule(private val board: ChessBoard) : Rule {

    override fun isValid(piece: ChessPiece, from: Position, to: Position): Boolean {
        return board.isSquareAttacked(to, piece.color.opposite())
    }
}