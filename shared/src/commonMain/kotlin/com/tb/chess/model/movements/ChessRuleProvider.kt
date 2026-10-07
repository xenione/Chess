package com.tb.chess.model.movements

import com.tb.chess.model.ChessBoard
import com.tb.chess.model.PieceType
import com.tb.chess.model.movements.rules.AndRule
import com.tb.chess.model.movements.rules.BlockPathMoveRule
import com.tb.chess.model.movements.rules.IntrinsicMoveRule
import com.tb.chess.model.movements.rules.MoveRule
import com.tb.chess.model.movements.rules.TargetNotOccupiedByFriendlyPieceMoveRule

class ChessRuleProvider(private val board: ChessBoard) : RuleProvider {

    private val commonMoveRule = AndRule.Builder()
        .and(IntrinsicMoveRule())
        .and(TargetNotOccupiedByFriendlyPieceMoveRule(board))
        .build()

    private val knightMoves = commonMoveRule

    private val bishopMoves =
        AndRule.Builder(commonMoveRule).and(BlockPathMoveRule(board)).build()

    private val rookMoves = AndRule.Builder(commonMoveRule).and(BlockPathMoveRule(board)).build()

    private val queenMoves =
        AndRule.Builder(commonMoveRule).and(BlockPathMoveRule(board)).build()

    private val kingMoves = commonMoveRule

    private val pawnMoves = commonMoveRule

    override fun provide(type: PieceType): MoveRule {
        return when (type) {
            PieceType.KNIGHT -> knightMoves
            PieceType.BISHOP -> bishopMoves
            PieceType.ROOK -> rookMoves
            PieceType.QUEEN -> queenMoves
            PieceType.KING -> kingMoves
            PieceType.PAWN -> pawnMoves
        }
    }
}