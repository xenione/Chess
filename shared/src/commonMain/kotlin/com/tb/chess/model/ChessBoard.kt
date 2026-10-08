package com.tb.chess.model

import kotlin.math.abs

class ChessBoard(
    private val squares: MutableMap<Position, ChessPiece> = mutableMapOf(),
    private val piecesHasBeenMoved: MutableSet<ChessPiece> = hashSetOf(),
    var enPassantTarget: Position? = null
) {
    data class PiecePositionState(val position: Position, val piece: ChessPiece, val hasMoved: Boolean)

    private lateinit var positions: List<Position>

    init {
        if (squares.isEmpty()) {
            setupInitialPosition()
        }

        fillPositions()

    }

    fun getAllPositions() = positions

    fun isOccupied(position: Position) = getPiece(position)!=null

    fun getPiece(position: Position): ChessPiece? = squares[position]

    fun getPieceState(position: Position): PiecePositionState? {
        val piece = squares[position] ?: return null
        return PiecePositionState(position, piece, hasBeenMoved(piece))
    }

    fun isSquareAttacked(square: Position, attackerColor: PieceColor): Boolean {
        val pieces = this.getAllPiecesState(attackerColor)
        for (pieceState in pieces) {
            if (canPieceAttack(pieceState.position, square, pieceState.piece)) {
                return true
            }
        }
        return false
    }

    private fun canPieceAttack(
        from: Position, to: Position, piece: ChessPiece
    ): Boolean {
        if (!piece.canBeAttack(from, to)) return false
        if (piece.type == PieceType.KNIGHT) return true
        return this.isPathClear(from, to)
    }

    fun getAllPiecesState(): List<PiecePositionState> =
        squares.map { (position, piece) ->
            PiecePositionState(
                position,
                piece,
                hasBeenMoved(piece)
            )
        }

    fun getAllPiecesState(color: PieceColor): List<PiecePositionState> =
        squares.filter { it.value.color == color }
            .map { (position, piece) -> PiecePositionState(position, piece, hasBeenMoved(piece)) }

    fun setPiece(position: Position, piece: ChessPiece?) {
        if (piece == null) {
            squares.remove(position)
            return
        }
        squares[position] = piece
    }

    fun movePiece(from: Position, to: Position): MoveResult {
        val piece = getPiece(from) ?: return MoveResult.Invalid
        var targetPiece = getPiece(to)

        // Check for castling
        if (piece.type == PieceType.KING && abs(to.col - from.col) == 2) {
            val isKingside = to.col > from.col
            val rookCol = if (isKingside) 7 else 0
            val newRookCol = if (isKingside) 5 else 3
            val rookPos = Position(from.row, rookCol)
            val newRookPos = Position(from.row, newRookCol)

            val rook = getPiece(rookPos)
            if (rook != null && !hasBeenMoved(rook) && !hasBeenMoved(piece)) {
                squares.remove(from)
                squares.remove(rookPos)
                squares[to] = piece
                markAsMoved(piece)
                squares[newRookPos] = rook
                markAsMoved(rook)
                enPassantTarget = null
                return MoveResult.Success(isCastling = true)
            }
            return MoveResult.Invalid
        }

        // Check for en passant capture
        if (piece.type == PieceType.PAWN && to == enPassantTarget) {
            val capturedRow = if (piece.color == PieceColor.WHITE) to.row + 1 else to.row - 1
            val capturedPos = Position(capturedRow, to.col)
            targetPiece = squares.remove(capturedPos)
        }

        // Standard move execution
        squares.remove(from)
        markAsMoved(piece)
        squares[to] = piece

        // Update enPassantTarget for next move
        enPassantTarget = null
        if (piece.type == PieceType.PAWN && abs(to.row - from.row) == 2) {
            val midRow = (from.row + to.row) / 2
            enPassantTarget = Position(midRow, from.col)
        }

        return MoveResult.Success(capturedPiece = targetPiece)
    }

    fun copy(): ChessBoard {
        return ChessBoard(
            squares.toMutableMap(),
            piecesHasBeenMoved.toMutableSet(),
            enPassantTarget
        )
    }

    private fun getStep(from: Int, to: Int): Int = when {
        to > from -> 1
        to < from -> -1
        else -> 0
    }

    fun isPathSafe(from: Position, to: Position, color: PieceColor): Boolean {
        val rowStep = getStep(from.row, to.row)
        val colStep = getStep(from.col, to.col)

        var currentRow = from.row + rowStep
        var currentCol = from.col + colStep

        val enemyColor = color.opposite()
        while (currentRow != to.row || currentCol != to.col) {
            if (this.isSquareAttacked(Position(currentRow, currentCol), enemyColor)) {
                return false
            }
            currentRow += rowStep
            currentCol += colStep
        }
        return true
    }

    fun isPathSafeFromStartToEnd(from: Position, to: Position, color: PieceColor): Boolean {
        val enemyColor = color.opposite()
        if (this.isSquareAttacked(from, enemyColor) || this.isSquareAttacked(to, enemyColor)) return false
        return this.isPathSafe(from, to, color)
    }


    fun isPathClear(from: Position, to: Position): Boolean {
        val rowStep = getStep(from.row, to.row)
        val colStep = getStep(from.col, to.col)

        var currentRow = from.row + rowStep
        var currentCol = from.col + colStep

        while (currentRow != to.row || currentCol != to.col) {
            if (this.getPiece(Position(currentRow, currentCol)) != null) {
                return false
            }
            currentRow += rowStep
            currentCol += colStep
        }

        return true
    }

    fun isPathClearFromStartToEnd(from: Position, to: Position, color: PieceColor): Boolean {
        if (this.isOccupied(from) || this.isOccupied(to)) return false
        return this.isPathClear(from, to)
    }

    fun restore(other: ChessBoard) {
        squares.clear()
        squares.putAll(other.squares)
        piecesHasBeenMoved.clear()
        piecesHasBeenMoved.addAll(other.piecesHasBeenMoved)
        enPassantTarget = other.enPassantTarget
    }

     fun hasBeenMoved(piece: ChessPiece): Boolean = piecesHasBeenMoved.contains(piece)

    private fun markAsMoved(piece: ChessPiece) {
        piecesHasBeenMoved.add(piece)
    }

    fun setupInitialPosition() {
        squares.clear()
        piecesHasBeenMoved.clear()
        enPassantTarget = null
        // Pawns
        for (col in 0..7) {
            squares[Position(6, col)] =
                ChessPiece(Position(6, col), PieceType.PAWN, PieceColor.WHITE)

            squares[Position(1, col)] =
                ChessPiece(Position(1, col), PieceType.PAWN, PieceColor.BLACK)
        }

        val backRankTypes = listOf(
            PieceType.ROOK,
            PieceType.KNIGHT,
            PieceType.BISHOP,
            PieceType.QUEEN,
            PieceType.KING,
            PieceType.BISHOP,
            PieceType.KNIGHT,
            PieceType.ROOK
        )

        for (col in 0..7) {
            squares[Position(7, col)] =
                ChessPiece(Position(7, col), backRankTypes[col], PieceColor.WHITE)
            squares[Position(0, col)] =
                ChessPiece(Position(0, col), backRankTypes[col], PieceColor.BLACK)
        }
    }

    fun fillPositions() {
        positions = (0..7).flatMap { row ->
            (0..7).map { col ->
                Position(row, col)
            }
        }
    }
}

sealed class MoveResult {
    object Invalid : MoveResult()
    data class Success(
        val isCastling: Boolean = false,
        val capturedPiece: ChessPiece? = null
    ) : MoveResult()
}
