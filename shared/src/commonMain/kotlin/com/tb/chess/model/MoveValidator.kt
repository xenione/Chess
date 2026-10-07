package com.tb.chess.model

import kotlin.math.abs

object MoveValidator {

    fun isLegalMove(board: ChessBoard, from: Position, to: Position): Boolean {
        if (from == to) return false
        val piece = board.getPiece(from) ?: return false


        val targetPiece = board.getPiece(to)

        // Cannot capture own piece
        if (targetPiece != null && targetPiece.color == piece.color) {
            return false
        }

        // Check for Castling
        if (piece.type == PieceType.KING && abs(to.col - from.col) == 2 && from.row == to.row) {
            return isValidCastling(board, from, to)
        }

        return when (piece.type) {
            PieceType.PAWN -> isValidPawnMove(board, from, to, piece.color)
            PieceType.KNIGHT -> {
                if(!piece.isIntrinsicalMoveValid(from, to)) return false
                true
            }
            PieceType.BISHOP -> {
                if(!piece.isIntrinsicalMoveValid(from, to)) return false
                isPathClear(board, from, to)
            }
            PieceType.ROOK ->   {
                if(!piece.isIntrinsicalMoveValid(from, to)) return false
                isPathClear(board, from, to)
            }
            PieceType.QUEEN -> {
                if(!piece.isIntrinsicalMoveValid(from, to)) return false
                isPathClear(board, from, to)
            }
            PieceType.KING ->   {
                if(!piece.isIntrinsicalMoveValid(from, to)) return false
                true
            }
        }
    }

    fun isCompletelyLegalMove(board: ChessBoard, from: Position, to: Position): Boolean {
        val piece = board.getPiece(from) ?: return false
        if (!isLegalMove(board, from, to)) return false

        // Simulate move to ensure it doesn't leave king in check
        val simulatedBoard = board.copy()
        val result = simulatedBoard.movePiece(from, to)
        if (result !is MoveResult.Success) return false

        return !isKingInCheck(simulatedBoard, piece.color)
    }

    fun getLegalMoves(board: ChessBoard, from: Position): List<Position> {
        return board.getAllPositions().filter { to -> isCompletelyLegalMove(board, from, to) }
    }

    fun findKingPosition(board: ChessBoard, color: PieceColor): Position? {
        val pieces = board.getAllPiecesState(color)
        for (pieceState in pieces) {
            if (pieceState.piece.type == PieceType.KING) {
                return pieceState.position
            }
        }
        return null
    }

    fun isKingInCheck(board: ChessBoard, color: PieceColor): Boolean {
        val kingPos = findKingPosition(board, color) ?: return false
        return isSquareAttacked(board, kingPos, color.opposite())
    }

    fun isSquareAttacked(board: ChessBoard, square: Position, attackerColor: PieceColor): Boolean {
        val pieces = board.getAllPiecesState(attackerColor)
        for (pieceState in pieces) {
            if (canPieceAttack(board, pieceState.position, square, pieceState.piece)) {
                return true
            }
        }
        return false
    }

    private fun canPieceAttack(board: ChessBoard, from: Position, to: Position, piece: ChessPiece): Boolean {
        //returbn piece.canBeAttack(from, to) // es necesario aplicar isClearPath
        return when (piece.type) {
            PieceType.PAWN -> {
                val direction = if (piece.color == PieceColor.WHITE) -1 else 1
                val rowDiff = to.row - from.row
                val colDiff = abs(to.col - from.col)
                colDiff == 1 && rowDiff == direction
            }
            PieceType.KNIGHT -> isValidKnightMove(from, to)
            PieceType.BISHOP -> isValidBishopMove(board, from, to)
            PieceType.ROOK -> isValidRookMove(board, from, to)
            PieceType.QUEEN -> isValidQueenMove(board, from, to)
            PieceType.KING -> isValidKingMove(from, to)
        }
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
        if (isKingInCheck(board, king.piece.color)) return false

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
            if (abs(currentCol - from.col) <= 2 && isSquareAttacked(board, checkPos, king.piece.color.opposite())) {
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

        // Forward move 1 square
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

    private fun isValidKnightMove(from: Position, to: Position): Boolean {
        val rowDiff = abs(to.row - from.row)
        val colDiff = abs(to.col - from.col)
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)
    }

    private fun isValidBishopMove(board: ChessBoard, from: Position, to: Position): Boolean {
        val rowDiff = to.row - from.row
        val colDiff = to.col - from.col
        if (abs(rowDiff) != abs(colDiff)) return false

        return isPathClear(board, from, to)
    }

    private fun isValidRookMove(board: ChessBoard, from: Position, to: Position): Boolean {
        val rowDiff = to.row - from.row
        val colDiff = to.col - from.col
        if (rowDiff != 0 && colDiff != 0) return false

        return isPathClear(board, from, to)
    }

    private fun isValidQueenMove(board: ChessBoard, from: Position, to: Position): Boolean {
        val rowDiff = to.row - from.row
        val colDiff = to.col - from.col
        val isDiagonal = abs(rowDiff) == abs(colDiff)
        val isStraight = rowDiff == 0 || colDiff == 0
        if (!isDiagonal && !isStraight) return false

        return isPathClear(board, from, to)
    }

    private fun isValidKingMove(from: Position, to: Position): Boolean {
        val rowDiff = abs(to.row - from.row)
        val colDiff = abs(to.col - from.col)
        return rowDiff <= 1 && colDiff <= 1
    }

    private fun isPathClear(board: ChessBoard, from: Position, to: Position): Boolean {
        val rowStep = compareValues(to.row, from.row).let { if (it == 0) 0 else it / abs(it) }
        val colStep = compareValues(to.col, from.col).let { if (it == 0) 0 else it / abs(it) }

        var currentRow = from.row + rowStep
        var currentCol = from.col + colStep

        while (currentRow != to.row || currentCol != to.col) {
            if (board.getPiece(Position(currentRow, currentCol)) != null) {
                return false
            }
            currentRow += rowStep
            currentCol += colStep
        }

        return true
    }
}
