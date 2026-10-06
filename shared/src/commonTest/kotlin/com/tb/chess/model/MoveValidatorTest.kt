package com.tb.chess.model

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
}
