package com.tb.chess.model.movements.rules

import com.tb.chess.model.ChessBoard
import com.tb.chess.model.ChessBoard.PiecePositionState
import com.tb.chess.model.MoveResult
import com.tb.chess.model.PieceColor
import com.tb.chess.model.PieceType
import com.tb.chess.model.Position
import com.tb.chess.model.movements.ChessRuleProvider
import kotlin.math.abs

class MoveValidator(board: ChessBoard) {

    private val chessRule = ChessRuleProvider(board)

    fun addMoveRule(pieceType: PieceType, moveRule: MoveRule) {

    }

    fun isLegalMove(board: ChessBoard, from: Position, to: Position): Boolean {
        if (from == to) return false
        val pieceState = board.getPieceState(from) ?: return false
        val piece = pieceState.piece
        if (!chessRule.provide(piece.type).isValid(piece, from, to)) return false

        // Simulate move to ensure it doesn't leave king in check
        val simulatedBoard = board.copy()
        val result = simulatedBoard.movePiece(from, to)
        if (result !is MoveResult.Success) return false

        return !isKingInCheck(simulatedBoard, pieceState.piece.color)
    }

    fun getLegalMoves(board: ChessBoard, from: Position): List<Position> {
        val piece = board.getPiece(from) ?: return emptyList()
        val candidates = mutableListOf<Position>()

        when (piece.type) {
            PieceType.PAWN -> {
                val dir = if (piece.color == PieceColor.WHITE) -1 else 1
                val r = from.row
                val c = from.col
                if (r + dir in 0..7) candidates.add(Position(r + dir, c))
                val startRow = if (piece.color == PieceColor.WHITE) 6 else 1
                if (r == startRow) candidates.add(Position(r + 2 * dir, c))
                if (r + dir in 0..7) {
                    if (c - 1 in 0..7) candidates.add(Position(r + dir, c - 1))
                    if (c + 1 in 0..7) candidates.add(Position(r + dir, c + 1))
                }
                if (board.enPassantTarget != null && board.enPassantTarget!!.row == r + dir && abs(board.enPassantTarget!!.col - c) == 1) {
                    candidates.add(board.enPassantTarget!!)
                }
            }
            PieceType.KNIGHT -> {
                val offsets = listOf(
                    Pair(-2, -1), Pair(-2, 1), Pair(-1, -2), Pair(-1, 2),
                    Pair(1, -2), Pair(1, 2), Pair(2, -1), Pair(2, 1)
                )
                for (offset in offsets) {
                    val nr = from.row + offset.first
                    val nc = from.col + offset.second
                    if (nr in 0..7 && nc in 0..7) {
                        candidates.add(Position(nr, nc))
                    }
                }
            }
            PieceType.KING -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr != 0 || dc != 0) {
                            val nr = from.row + dr
                            val nc = from.col + dc
                            if (nr in 0..7 && nc in 0..7) {
                                candidates.add(Position(nr, nc))
                            }
                        }
                    }
                }
                if (from.col == 4) {
                    candidates.add(Position(from.row, from.col + 2))
                    candidates.add(Position(from.row, from.col - 2))
                }
            }
            PieceType.ROOK, PieceType.BISHOP, PieceType.QUEEN -> {
                val directions = mutableListOf<Pair<Int, Int>>()
                if (piece.type == PieceType.ROOK || piece.type == PieceType.QUEEN) {
                    directions.addAll(listOf(Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1)))
                }
                if (piece.type == PieceType.BISHOP || piece.type == PieceType.QUEEN) {
                    directions.addAll(listOf(Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)))
                }
                for (dir in directions) {
                    var step = 1
                    while (true) {
                        val nr = from.row + dir.first * step
                        val nc = from.col + dir.second * step
                        if (nr !in 0..7 || nc !in 0..7) break
                        val pos = Position(nr, nc)
                        candidates.add(pos)
                        if (board.getPiece(pos) != null) break
                        step++
                    }
                }
            }
        }

        return candidates.filter { to -> isLegalMove(board, from, to) }
    }

    fun isKingInCheck(board: ChessBoard, pieceState: PiecePositionState): Boolean {
        return board.isSquareAttacked( pieceState.position, pieceState.piece.color.opposite())
    }

    fun isKingInCheck(board: ChessBoard, color: PieceColor): Boolean {
        val kingState = board.getAllPiecesState(color).find { it.piece.type == PieceType.KING } ?: return false
        return isKingInCheck(board, kingState)
    }

    fun hasAnyLegalMoves(board: ChessBoard, color: PieceColor): Boolean {
        val pieces = board.getAllPiecesState(color)
        for (pieceState in pieces) {
            val legalMoves = getLegalMoves(board, pieceState.position)
            if (legalMoves.isNotEmpty()) {
                return true
            }
        }
        return false
    }
}