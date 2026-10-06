package com.tb.chess.engine.evaluator.criteria

import com.tb.chess.model.ChessBoard
import com.tb.chess.model.PieceColor
import com.tb.chess.model.PieceType
import com.tb.chess.model.Position
import kotlin.collections.plusAssign

class MaterialCriteria : EvaluationCriteria {

    override fun evaluate(board: ChessBoard, color: PieceColor): Int {
        var score = 0
        board.getAllPiecesState(color).forEach { pieceState ->
            score += getMaterialValue(pieceState.piece.type)
        }
        return score
    }

    private fun getMaterialValue(type: PieceType): Int {
        return when (type) {
            PieceType.PAWN -> 100
            PieceType.KNIGHT -> 320
            PieceType.BISHOP -> 330
            PieceType.ROOK -> 500
            PieceType.QUEEN -> 900
            PieceType.KING -> 20000
        }
    }
}


