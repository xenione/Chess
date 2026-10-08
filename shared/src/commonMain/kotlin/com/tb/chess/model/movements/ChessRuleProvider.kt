package com.tb.chess.model.movements

import com.tb.chess.model.ChessBoard
import com.tb.chess.model.PieceType
import com.tb.chess.model.movements.rules.AndRule
import com.tb.chess.model.movements.rules.ClearPathMoveRule
import com.tb.chess.model.movements.rules.ChessRule
import com.tb.chess.model.movements.rules.ChessRule.Case.CaseBuilder
import com.tb.chess.model.movements.rules.EnPassantMoveRule
import com.tb.chess.model.movements.rules.HasNotBeenMoveMoveRule
import com.tb.chess.model.movements.rules.IntrinsicMoveRule
import com.tb.chess.model.movements.rules.IsFromPositionUnderAttackMoveRule
import com.tb.chess.model.movements.rules.NSquareForwardDiagonalMoveRule
import com.tb.chess.model.movements.rules.NSquareForwardMoveRule
import com.tb.chess.model.movements.rules.NSquareHorizontalKingSideMoveRule
import com.tb.chess.model.movements.rules.NSquareHorizontalQueenSideMoveRule
import com.tb.chess.model.movements.rules.NotRule
import com.tb.chess.model.movements.rules.OrRule
import com.tb.chess.model.movements.rules.RookHasNotBeenMoveMoveRule
import com.tb.chess.model.movements.rules.Rule
import com.tb.chess.model.movements.rules.SafePathMoveRule
import com.tb.chess.model.movements.rules.TargetNotOccupiedByFriendlyPieceMoveRule
import com.tb.chess.model.movements.rules.TargetNotOccupiedByPieceMoveRule
import com.tb.chess.model.movements.rules.TargetOccupiedByEnemyPieceMoveRule

class ChessRuleProvider(private val board: ChessBoard) : RuleProvider {

    private val commonMoveRule = AndRule.Builder()
        .and(IntrinsicMoveRule())
        .and(TargetNotOccupiedByFriendlyPieceMoveRule(board))
        .build()


    private val commonMoveCase = CaseBuilder()
        .move(IntrinsicMoveRule())
        .condition(TargetNotOccupiedByFriendlyPieceMoveRule(board))
        .build()

    private val knightMoves = commonMoveRule

    private val bishopMoves =
        AndRule.Builder(commonMoveRule).and(ClearPathMoveRule(board)).build()

    private val rookMoves = AndRule.Builder(commonMoveRule).and(ClearPathMoveRule(board)).build()

    private val queenMoves =
        AndRule.Builder(commonMoveRule).and(ClearPathMoveRule(board)).build()

    private val kingMoves =
        ChessRule.Builder()
            .case(
                CaseBuilder()
                    .move(IntrinsicMoveRule())
                    .condition(TargetNotOccupiedByFriendlyPieceMoveRule(board))
                    .condition(NotRule(IsFromPositionUnderAttackMoveRule(board)))
                    .build()
            )
            .case(
                CaseBuilder()
                    .move(NSquareHorizontalKingSideMoveRule(2))
                    .condition(HasNotBeenMoveMoveRule(board))
                    .condition(RookHasNotBeenMoveMoveRule(board))
                    .condition(ClearPathMoveRule(board))
                    .condition(SafePathMoveRule(board))
                    .build()
            )
            .case(
                CaseBuilder()
                    .move(NSquareHorizontalQueenSideMoveRule(2))
                    .condition(HasNotBeenMoveMoveRule(board))
                    .condition(RookHasNotBeenMoveMoveRule(board))
                    .condition(ClearPathMoveRule(board))
                    .condition(SafePathMoveRule(board))
                    .build()
            )
            .build()

    private val pawnMoves =
        // movimento intrinseco de una casilla
        ChessRule.Builder()
            .case(
                CaseBuilder()
                    .move(IntrinsicMoveRule())
                    .condition(TargetNotOccupiedByPieceMoveRule(board))
                    .build()
            )
            .case(
                CaseBuilder()
                    .move(NSquareForwardMoveRule(2))
                    .condition(TargetNotOccupiedByPieceMoveRule(board))
                    .condition(HasNotBeenMoveMoveRule(board))
                    .build()
            )
            // captura diagonal
            .case(
                CaseBuilder()
                    .move(NSquareForwardDiagonalMoveRule(1))
                    .condition(
                        OrRule.Builder()
                            .or(TargetOccupiedByEnemyPieceMoveRule(board))
                            .or(EnPassantMoveRule(board))
                            .build()
                    ).build()
            )
            .build()


    override fun provide(type: PieceType): Rule {
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