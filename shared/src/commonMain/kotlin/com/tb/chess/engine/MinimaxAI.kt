package com.tb.chess.engine

import com.tb.chess.engine.evaluator.ChessEvaluator
import com.tb.chess.model.*
import com.tb.chess.model.movements.MoveValidator
import kotlin.math.max
import kotlin.math.min

data class ScoredMove(
    val from: Position,
    val to: Position,
    val score: Int
)

class MinimaxAI(
    private val chessEvaluator: ChessEvaluator,
    private val maxDepth: Int = 4
) {

    fun findBestMove(board: ChessBoard, aiColor: PieceColor): Pair<Position, Position>? {
        var bestMove: Pair<Position, Position>? = null
        var bestScore = Int.MIN_VALUE

        val allMoves = getAllPossibleMoves(board, aiColor)

        for (move in allMoves) {
            val simulatedBoard = board.copy()
            val result = simulatedBoard.movePiece(move.from, move.to)
            if (result is MoveResult.Success) {
                val score = minimax(simulatedBoard, maxDepth - 1, false, aiColor, Int.MIN_VALUE, Int.MAX_VALUE)
                if (score > bestScore) {
                    bestScore = score
                    bestMove = Pair(move.from, move.to)
                }
            }
        }

        return bestMove
    }

    private fun minimax(
        board: ChessBoard,
        depth: Int,
        isMaximizing: Boolean,
        aiColor: PieceColor,
        alphaParam: Int,
        betaParam: Int
    ): Int {
        var alpha = alphaParam
        var beta = betaParam

        if (depth == 0) {
            return chessEvaluator.evaluate(board, aiColor)
        }

        val currentColor = if (isMaximizing) aiColor else aiColor.opposite()
        val allMoves = getAllPossibleMoves(board, currentColor)

        if (allMoves.isEmpty()) {
            val inCheck = MoveValidator.isKingInCheck(board, currentColor)
            if (inCheck) {
                return if (isMaximizing) -15000 + depth else 15000 - depth
            } else {
                val materialEval = chessEvaluator.evaluate(board, aiColor)
                return if (materialEval > 200) -10000 else 0
            }
        }

        if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in allMoves) {
                val simulatedBoard = board.copy()
                val result = simulatedBoard.movePiece(move.from, move.to)
                if (result is MoveResult.Success) {
                    val eval = minimax(simulatedBoard, depth - 1, false, aiColor, alpha, beta)
                    maxEval = max(maxEval, eval)
                    alpha = max(alpha, eval)
                    if (beta <= alpha) break
                }
            }
            return maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in allMoves) {
                val simulatedBoard = board.copy()
                val result = simulatedBoard.movePiece(move.from, move.to)
                if (result is MoveResult.Success) {
                    val eval = minimax(simulatedBoard, depth - 1, true, aiColor, alpha, beta)
                    minEval = min(minEval, eval)
                    beta = min(beta, eval)
                    if (beta <= alpha) break
                }
            }
            return minEval
        }
    }

    private fun getAllPossibleMoves(board: ChessBoard, color: PieceColor): List<ScoredMove> {
        val moves = mutableListOf<ScoredMove>()
        val pieces = board.getAllPiecesState(color)
        for (pieceState in pieces) {
            val legalTargets = MoveValidator.getLegalMoves(board, pieceState.position)
            for (to in legalTargets) {
                val targetPiece = board.getPiece(to)
                val captureBonus = when (targetPiece?.type) {
                    PieceType.QUEEN -> 90
                    PieceType.ROOK -> 50
                    PieceType.BISHOP, PieceType.KNIGHT -> 30
                    PieceType.PAWN -> 10
                    else -> 0
                }
                moves.add(ScoredMove(pieceState.position, to, captureBonus))
            }
        }
        return moves.sortedByDescending { it.score }
    }
}
