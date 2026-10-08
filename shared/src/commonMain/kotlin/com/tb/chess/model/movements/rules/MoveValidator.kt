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
        val piece = board.getPiece(from) ?: return false

        // Check for Castling
        if (piece.type == PieceType.KING && abs(to.col - from.col) == 2 && from.row == to.row) {
            return isValidCastling(board, from, to)
        }

        return chessRule.provide(piece.type).isValid(piece, from, to)
    }

    fun isCompletelyLegalMove(board: ChessBoard, from: Position, to: Position): Boolean {
        val pieceState = board.getPieceState(from) ?: return false
        if (!isLegalMove(board, from, to)) return false

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

        return candidates.filter { to -> isCompletelyLegalMove(board, from, to) }
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

    private fun isValidCastling(board: ChessBoard, from: Position, to: Position): Boolean {
        val king = board.getPieceState(from) ?: return false
        if (king.hasMoved) return false

        // Cannot castle while in check
        if (isKingInCheck(board,king)) return false

        val isKingside = to.col > from.col
        val rookCol = if (isKingside) 7 else 0
        val rookPos = Position(from.row, rookCol)
        val rook = board.getPieceState(rookPos)

        if (rook == null || rook.hasMoved || rook.piece.type != PieceType.ROOK || rook.piece.color != king.piece.color) {
            return false
        }

        // Check path is clear between king and rook
        val step = if (isKingside) 1 else -1
        var currentCol = from.col + step
        while (currentCol != rookCol) {
            val checkPos = Position(from.row, currentCol)
            if (board.getPiece(checkPos) != null) {
                return false
            }
            // Also check that squares the king passes through are not attacked (for kingside/queenside castling)
            if (abs(currentCol - from.col) <= 2 && board.isSquareAttacked( checkPos, king.piece.color.opposite())) {
                return false
            }
            currentCol += step
        }

        return true
    }

    private fun isValidPawnMove(board: ChessBoard, from: Position, to: Position, color: PieceColor): Boolean {
        val direction = if (color == PieceColor.WHITE) -1 else 1
        val startRow = if (color == PieceColor.WHITE) 6 else 1

        val rowDiff = to.row - from.row
        val colDiff = to.col - from.col


        // Forward move 1 square // este es el intrinseco
        if (colDiff == 0 && rowDiff == direction && board.getPiece(to) == null) { //  TODO board.getPiece(to) == null equivale a isPathClear
            return true
        }

        // Forward move 2 squares from start position
        if (colDiff == 0 && rowDiff == 2 * direction && from.row == startRow) {
            val intermediateRow = from.row + direction
            val intermediatePos = Position(intermediateRow, from.col)
            if (board.getPiece(intermediatePos) == null && board.getPiece(to) == null) {
                return true
            }
        }

        // Diagonal capture
        if (abs(colDiff) == 1 && rowDiff == direction) {
            val target = board.getPiece(to)
            if (target != null && target.color != color) {
                return true
            }
            // En Passant capture using board.enPassantTarget
            if (to == board.enPassantTarget) {
                return true
            }
        }

        return false
    }
}