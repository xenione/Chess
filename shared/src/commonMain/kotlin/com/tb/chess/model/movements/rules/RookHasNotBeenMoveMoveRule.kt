package com.tb.chess.model.movements.rules

import com.tb.chess.model.ChessBoard
import com.tb.chess.model.ChessPiece
import com.tb.chess.model.PieceColor
import com.tb.chess.model.PieceType
import com.tb.chess.model.Position

class RookHasNotBeenMoveMoveRule(private val board: ChessBoard) :
    Rule {

    override fun isValid(piece: ChessPiece, from: Position, to: Position): Boolean {
        val isKingSide: Boolean = to.col > from.col
        val algebraicNotation: String =
            when (piece.color) {
                PieceColor.WHITE -> if (isKingSide) "h1" else "a1"
                PieceColor.BLACK -> if (isKingSide) "h8" else "a8"
            }

        val rookState = board.getPieceState(Position.fromAlgebraic(algebraicNotation)!!)
        return (rookState != null && rookState.piece.type == PieceType.ROOK && !rookState.hasMoved)
    }
}