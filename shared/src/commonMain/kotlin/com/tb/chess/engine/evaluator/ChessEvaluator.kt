package com.tb.chess.engine.evaluator

import com.tb.chess.engine.evaluator.criteria.MaterialCriteria
import com.tb.chess.engine.evaluator.criteria.PawnStructureCriteria
import com.tb.chess.engine.evaluator.criteria.ControlStaticPositionCriteria
import com.tb.chess.engine.evaluator.criteria.EvaluationCriteria
import com.tb.chess.model.ChessBoard
import com.tb.chess.model.PieceColor

class ChessEvaluator(private val evalCriteriaList: List<EvaluationCriteria> = emptyList()) {

    // Evaluates board from a specific player's perspective
    fun evaluate(board: ChessBoard, perspectiveColor: PieceColor = PieceColor.WHITE): Int {
        val whiteScore = evaluateForWhite(board)
        return if (perspectiveColor == PieceColor.WHITE) whiteScore else -whiteScore
    }

    // Evaluates board from White's perspective (positive = White winning, negative = Black winning)
    private fun evaluateForWhite(board: ChessBoard): Int {
        var score = 0
        evalCriteriaList.forEach { criteria ->
            score += criteria.evaluateForWhite(board)
        }
        return score
    }
}
