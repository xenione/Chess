package com.tb.chess.model

import com.tb.chess.model.movements.rules.MoveValidator
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoveValidatorTest {

    private fun createEmptyBoard(): ChessBoard {
        val board = ChessBoard()
        for (pos in board.getAllPositions()) {
            board.setPiece(pos, null)
        }
        return board
    }

    // --- PAWN TESTS ---

    @Test
    fun testPawnInitialOneSquareAdvanceIsLegal() {
        val board = ChessBoard()
        val moveValidator = MoveValidator(board)

        val e2 = Position('e', 2)
        val e3 = Position('e', 3)
        assertTrue(moveValidator.isCompletelyLegalMove(board, e2, e3))
    }

    @Test
    fun testPawnInitialTwoSquareAdvanceIsLegal() {
        val board = ChessBoard()
        val moveValidator = MoveValidator(board)

        val e2 = Position('e', 2)
        val e4 = Position('e', 4)
        assertTrue(moveValidator.isCompletelyLegalMove(board, e2, e4))
    }

    @Test
    fun testPawnThreeSquareAdvanceIsIllegal() {
        val board = ChessBoard()
        val moveValidator = MoveValidator(board)

        val e2 = Position('e', 2)
        val e5 = Position('e', 5)
        assertFalse(moveValidator.isCompletelyLegalMove(board, e2, e5))
    }

    @Test
    fun testPawnBlockedByPieceInFrontCannotMove() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val e2 = Position('e', 2) // White Pawn
        val e3 = Position('e', 3) // Blocking piece
        val e8 = Position('e', 8) // Black King

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(e2, ChessPiece(e2, PieceType.PAWN, PieceColor.WHITE))
        board.setPiece(e3, ChessPiece(e3, PieceType.KNIGHT, PieceColor.BLACK))

        assertFalse(moveValidator.isCompletelyLegalMove(board, e2, e3), "Pawn cannot move forward into occupied square")
    }

    @Test
    fun testPawnDiagonalCaptureOfEnemyPieceIsLegal() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val e2 = Position('e', 2) // White Pawn
        val d3 = Position('d', 3) // Black Pawn to capture
        val e8 = Position('e', 8) // Black King

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(e2, ChessPiece(e2, PieceType.PAWN, PieceColor.WHITE))
        board.setPiece(d3, ChessPiece(d3, PieceType.PAWN, PieceColor.BLACK))

        assertTrue(moveValidator.isCompletelyLegalMove(board, e2, d3), "Pawn should be able to capture enemy diagonally")
    }

    @Test
    fun testPawnDiagonalMoveToEmptySquareIsIllegal() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1)
        val e2 = Position('e', 2)
        val d3 = Position('d', 3) // Empty square
        val e8 = Position('e', 8)

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(e2, ChessPiece(e2, PieceType.PAWN, PieceColor.WHITE))

        assertFalse(moveValidator.isCompletelyLegalMove(board, e2, d3), "Pawn cannot move diagonally without capturing")
    }

    @Test
    fun testPawnCannotCaptureFriendlyPieceDiagonally() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1)
        val e2 = Position('e', 2)
        val f3 = Position('f', 3) // Friendly Pawn
        val e8 = Position('e', 8)

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(e2, ChessPiece(e2, PieceType.PAWN, PieceColor.WHITE))
        board.setPiece(f3, ChessPiece(f3, PieceType.PAWN, PieceColor.WHITE))

        assertFalse(moveValidator.isCompletelyLegalMove(board, e2, f3), "Pawn cannot capture friendly piece")
    }

    @Test
    fun testPawnEnPassantCaptureIsLegal() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val e8 = Position('e', 8) // Black King
        val d5 = Position('d', 5) // White Pawn
        val e5 = Position('e', 5) // Black Pawn that just moved two squares
        val e6 = Position('e', 6) // En Passant target square

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(d5, ChessPiece(d5, PieceType.PAWN, PieceColor.WHITE))
        board.setPiece(e5, ChessPiece(e5, PieceType.PAWN, PieceColor.BLACK))
        board.enPassantTarget = e6

        assertTrue(moveValidator.isCompletelyLegalMove(board, d5, e6), "White pawn should be able to capture en passant at e6")
    }

    @Test
    fun testPinnedPawnMoveLeavingKingInCheckIsIllegal() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val f2 = Position('f', 2) // White Pawn pinned on the h4-e1 diagonal
        val h4 = Position('h', 4) // Black Bishop checking king if f2 moves
        val f3 = Position('f', 3)

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(f2, ChessPiece(f2, PieceType.PAWN, PieceColor.WHITE))
        board.setPiece(h4, ChessPiece(h4, PieceType.BISHOP, PieceColor.BLACK))

        assertFalse(moveValidator.isCompletelyLegalMove(board, f2, f3), "Moving a pinned pawn off its diagonal should be illegal as it leaves king in check")
    }

    // --- KNIGHT TESTS ---

    @Test
    fun testKnightValidLMoveIsLegal() {
        val board = ChessBoard()
        val moveValidator = MoveValidator(board)

        val b1 = Position('b', 1)
        val c3 = Position('c', 3)
        assertTrue(moveValidator.isCompletelyLegalMove(board, b1, c3))
    }

    @Test
    fun testKnightNonLMoveIsIllegal() {
        val board = ChessBoard()
        val moveValidator = MoveValidator(board)

        val b1 = Position('b', 1)
        val b3 = Position('b', 3)
        assertFalse(moveValidator.isCompletelyLegalMove(board, b1, b3))
    }

    @Test
    fun testKnightCannotLandOnFriendlyPiece() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val b1 = Position('b', 1) // White Knight
        val c3 = Position('c', 3) // White Pawn (friendly)
        val e8 = Position('e', 8) // Black King

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(b1, ChessPiece(b1, PieceType.KNIGHT, PieceColor.WHITE))
        board.setPiece(c3, ChessPiece(c3, PieceType.PAWN, PieceColor.WHITE))

        assertFalse(moveValidator.isCompletelyLegalMove(board, b1, c3), "Knight cannot land on friendly piece")
    }

    // --- BISHOP TESTS ---

    @Test
    fun testBishopDiagonalMoveIsLegal() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val c1 = Position('c', 1) // White Bishop
        val d2 = Position('d', 2) // Unobstructed target
        val e8 = Position('e', 8) // Black King

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(c1, ChessPiece(c1, PieceType.BISHOP, PieceColor.WHITE))

        assertTrue(moveValidator.isCompletelyLegalMove(board, c1, d2))
    }

    @Test
    fun testBishopCannotJumpOverObstacle() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val c1 = Position('c', 1) // White Bishop
        val d2 = Position('d', 2) // Obstacle
        val e3 = Position('e', 3) // Target behind obstacle
        val e8 = Position('e', 8) // Black King

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(c1, ChessPiece(c1, PieceType.BISHOP, PieceColor.WHITE))
        board.setPiece(d2, ChessPiece(d2, PieceType.PAWN, PieceColor.WHITE))

        assertFalse(moveValidator.isCompletelyLegalMove(board, c1, e3), "Bishop cannot jump over piece at d2")
    }

    // --- ROOK TESTS ---

    @Test
    fun testRookStraightMoveIsLegal() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val a1 = Position('a', 1) // White Rook
        val a5 = Position('a', 5) // Empty target
        val e8 = Position('e', 8) // Black King

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(a1, ChessPiece(a1, PieceType.ROOK, PieceColor.WHITE))

        assertTrue(moveValidator.isCompletelyLegalMove(board, a1, a5))
    }

    @Test
    fun testRookCaptureEnemyPieceIsLegal() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val a1 = Position('a', 1) // White Rook
        val a8 = Position('a', 8) // Enemy Rook
        val e8 = Position('e', 8) // Black King

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(a1, ChessPiece(a1, PieceType.ROOK, PieceColor.WHITE))
        board.setPiece(a8, ChessPiece(a8, PieceType.ROOK, PieceColor.BLACK))

        assertTrue(moveValidator.isCompletelyLegalMove(board, a1, a8), "Rook should be able to capture enemy at a8")
    }

    @Test
    fun testRookCannotJumpOverObstacle() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val a1 = Position('a', 1) // White Rook
        val a3 = Position('a', 3) // Obstacle
        val a5 = Position('a', 5) // Target behind obstacle
        val e8 = Position('e', 8) // Black King

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(a1, ChessPiece(a1, PieceType.ROOK, PieceColor.WHITE))
        board.setPiece(a3, ChessPiece(a3, PieceType.PAWN, PieceColor.WHITE))

        assertFalse(moveValidator.isCompletelyLegalMove(board, a1, a5), "Rook cannot jump over obstacle")
    }

    // --- QUEEN TESTS ---

    @Test
    fun testQueenStraightAndDiagonalMoveIsLegal() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)

        val e1 = Position('e', 1) // White King
        val d1 = Position('d', 1) // White Queen
        val d5 = Position('d', 5) // Straight move
        val h5 = Position('h', 5) // Diagonal move
        val e8 = Position('e', 8) // Black King

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.KING, PieceColor.BLACK))
        board.setPiece(d1, ChessPiece(d1, PieceType.QUEEN, PieceColor.WHITE))

        assertTrue(moveValidator.isCompletelyLegalMove(board, d1, d5))
        assertTrue(moveValidator.isCompletelyLegalMove(board, d1, h5))
    }

    // --- KING, CHECK & PIN TESTS ---

    @Test
    fun testKingCannotMoveToAttackedSquare() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)
        val e1 = Position('e', 1)
        val d8 = Position('d', 8)
        val d1 = Position('d', 1)

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(d8, ChessPiece(d8, PieceType.ROOK, PieceColor.BLACK))

        // d1 is attacked by the rook on d8
        assertFalse(moveValidator.isCompletelyLegalMove(board, e1, d1))
    }

    @Test
    fun testKingMoveToUnattackedSquareIsLegal() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)
        val e1 = Position('e', 1)
        val d8 = Position('d', 8)
        val f1 = Position('f', 1)

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(d8, ChessPiece(d8, PieceType.ROOK, PieceColor.BLACK))

        // f1 is unattacked and safe for King
        assertTrue(moveValidator.isCompletelyLegalMove(board, e1, f1))
    }

    @Test
    fun testPinnedPieceCannotMove() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)
        val e1 = Position('e', 1)
        val e2 = Position('e', 2)
        val e8 = Position('e', 8)

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(e2, ChessPiece(e2, PieceType.BISHOP, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.ROOK, PieceColor.BLACK))

        // Bishop at e2 is pinned to King at e1 by Rook at e8
        val legalMoves = moveValidator.getLegalMoves(board, e2)
        assertTrue(legalMoves.isEmpty(), "Pinned bishop should have no legal moves")
    }

    @Test
    fun testUnrelatedPieceCannotMoveWhenKingInCheck() {
        val board = createEmptyBoard()
        val moveValidator = MoveValidator(board)
        val e1 = Position('e', 1)
        val a2 = Position('a', 2)
        val a3 = Position('a', 3)
        val e8 = Position('e', 8)

        board.setPiece(e1, ChessPiece(e1, PieceType.KING, PieceColor.WHITE))
        board.setPiece(a2, ChessPiece(a2, PieceType.PAWN, PieceColor.WHITE))
        board.setPiece(e8, ChessPiece(e8, PieceType.ROOK, PieceColor.BLACK))

        // White King at e1 is in check from Black Rook at e8.
        // White Pawn at a2 cannot block or capture the rook on e8, so moving a2 should be illegal.
        assertFalse(moveValidator.isCompletelyLegalMove(board, a2, a3), "Moving unrelated pawn when in check should be illegal")
        val legalMovesForPawn = moveValidator.getLegalMoves(board, a2)
        assertTrue(legalMovesForPawn.isEmpty(), "Unrelated pawn should have no legal moves when king is in check")
    }
}
