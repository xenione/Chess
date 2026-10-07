package com.tb.chess.engine.evaluator.criteria

import com.tb.chess.model.ChessBoard
import com.tb.chess.model.PieceColor
import com.tb.chess.model.PieceType
import com.tb.chess.model.Position

class ControlStaticPositionCriteria : EvaluationCriteria {

    override fun evaluate(board: ChessBoard, color: PieceColor): Int {
        var score = 0
        board.getAllPiecesState(color).forEach { pieceState ->
            val position = pieceState.position
            val pieceType = pieceState.piece.type
            score += squareWeight(position) * pieceWeight(pieceType)
        }

        return score
    }

    private val squareImportance = arrayOf(
        intArrayOf(1, 1, 2, 2, 2, 2, 1, 1),
        intArrayOf(1, 2, 2, 3, 3, 2, 2, 1),
        intArrayOf(2, 2, 3, 4, 4, 3, 2, 2),
        intArrayOf(2, 3, 4, 5, 5, 4, 3, 2),
        intArrayOf(2, 3, 4, 5, 5, 4, 3, 2),
        intArrayOf(2, 2, 3, 4, 4, 3, 2, 2),
        intArrayOf(1, 2, 2, 3, 3, 2, 2, 1),
        intArrayOf(1, 1, 2, 2, 2, 2, 1, 1)
    )

    private fun squareWeight(position: Position): Int {
        return squareImportance[position.row][position.col]

    }

    private fun pieceWeight(pieceType: PieceType): Int {
        return when (pieceType) {
            PieceType.PAWN -> 1
            PieceType.KNIGHT -> 2
            PieceType.BISHOP -> 2
            PieceType.ROOK -> 3
            PieceType.QUEEN -> 4
            PieceType.KING -> 2
        }
    }

}



