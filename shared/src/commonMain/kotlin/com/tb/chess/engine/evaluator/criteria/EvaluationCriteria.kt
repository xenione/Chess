package com.tb.chess.engine.evaluator.criteria

import com.tb.chess.model.ChessBoard
import com.tb.chess.model.PieceColor

interface EvaluationCriteria {

    fun evaluate(board: ChessBoard, color: PieceColor): Int

    fun evaluateForWhite(board: ChessBoard): Int =
        evaluate(board, PieceColor.WHITE) - evaluate(board, PieceColor.BLACK)

}