package com.tb.chess.model

import com.tb.chess.model.movements.MoveValidator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoveValidatorTest {

    @Test
    fun testPawnInitialMoves() {
        val board = ChessBoard()

        val e2 = Position('e', 2)
        val e4 = Position('e', 4)
        assertTrue(MoveValidator.isLegalMove(board, e2, e4))

        val e3 = Position('e', 3)
        assertTrue(MoveValidator.isLegalMove(board, e2, e3))

        val e5 = Position('e', 5)
        assertFalse(MoveValidator.isLegalMove(board, e2, e5))
    }

    @Test
    fun testKnightMoves() {
        val board = ChessBoard()

        val b1 = Position('b', 1)
        val c3 = Position('c', 3)
        assertTrue(MoveValidator.isLegalMove(board, b1, c3))

        val a3 = Position('a', 3)
        assertTrue(MoveValidator.isLegalMove(board, b1, a3))

        val b3 = Position('b', 3)
        assertFalse(MoveValidator.isLegalMove(board, b1, b3))
    }

    private fun createEmptyBoard(): ChessBoard {
        val board = ChessBoard()
        for (pos in board.getAllPositions()) {
            board.setPiece(pos, null)
        }
        return board
    }

    @Test
    fun testKingCannotMoveToAttackedSquare() {
        val board = createEmptyBoard()
        val e1 = Position('e', 1)
        val d8 = Position('d', 8)
        val d1 = Position('d', 1)
        val d2 = Position('d', 2)
        val e2 = Position('e', 2)
        val f1 = Position('f', 1)

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(d8, ChessPiece(d8, PieceType.ROOK, PieceColor.BLACK))

        // d1 and d2 are attacked by the rook on d8
        assertFalse(MoveValidator.isCompletelyLegalMove(board, e1, d1))
        assertFalse(MoveValidator.isCompletelyLegalMove(board, e1, d2))

        val legalMoves = MoveValidator.getLegalMoves(board, e1)
        assertFalse(d1 in legalMoves, "d1 should not be a legal move for king as it is attacked")
        assertFalse(d2 in legalMoves, "d2 should not be a legal move for king as it is attacked")
        assertTrue(e2 in legalMoves, "e2 should be a legal move for king")
        assertTrue(f1 in legalMoves, "f1 should be a legal move for king")
    }

    @Test
    fun testPinnedPieceCannotMove() {
        val board = createEmptyBoard()
        val e1 = Position('e', 1)
        val e2 = Position('e', 2)
        val e8 = Position('e', 8)

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e2, ChessPiece(e2, PieceType.BISHOP, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.ROOK, PieceColor.BLACK))

        // Bishop at e2 is pinned to King at e1 by Rook at e8
        val legalMoves = MoveValidator.getLegalMoves(board, e2)
        assertTrue(legalMoves.isEmpty(), "Pinned bishop should have no legal moves")
    }
}
