package com.tb.chess.engine.evaluator.criteria

import com.tb.chess.model.ChessBoard
import com.tb.chess.model.PieceColor
import com.tb.chess.model.PieceType
import com.tb.chess.model.Position
import kotlin.collections.plusAssign

class PawnStructureCriteria : EvaluationCriteria {

    override fun evaluate(board: ChessBoard, color: PieceColor): Int {

        var score = 0
        val pawns = mutableListOf<Position>()

        val pieces = board.getAllPiecesState()
        pieces.forEach { pieceState ->
            if (pieceState.piece.type == PieceType.PAWN && pieceState.piece.color == color) {
                pawns.add(pieceState.position)
            }
        }

        for (pawn in pawns) {
            // 1. Advancement bonus (closer to promotion rank 0 for white, 7 for black)
            val advancement = if (color == PieceColor.WHITE) (6 - pawn.row) else (pawn.row - 1)
            score += passedPawnBonus(advancement)

            // 2. Isolated pawn penalty (no friendly pawns on adjacent files col-1 and col+1)
            val hasNeighborLeft = pawns.any { it.col == pawn.col - 1 }
            val hasNeighborRight = pawns.any { it.col == pawn.col + 1 }
            if (!hasNeighborLeft && !hasNeighborRight) {
                score -= 15 // Penalty for isolated pawn
            }

            // 3. Pawn chain / support bonus (defended by a friendly pawn diagonally behind)
            val supportRow = if (color == PieceColor.WHITE) pawn.row + 1 else pawn.row - 1
            val isSupported =
                pawns.any { it.row == supportRow && (it.col == pawn.col - 1 || it.col == pawn.col + 1) }
            if (isSupported) {
                score += 10 // Bonus for supported pawn / chain
            }
        }

        return score
    }

    private fun passedPawnBonus(advancement: Int): Int =
        when (advancement) {
            0 -> 0
            1 -> 5
            2 -> 10
            3 -> 20
            4 -> 35
            5 -> 60
            6 -> 100
            else -> 0
        }
}


