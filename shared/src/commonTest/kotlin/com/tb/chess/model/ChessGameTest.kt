package com.tb.chess.model

import com.tb.chess.engine.MinimaxAI
import com.tb.chess.engine.evaluator.ChessEvaluator
import com.tb.chess.engine.evaluator.criteria.ControlStaticPositionCriteria
import com.tb.chess.engine.evaluator.criteria.MaterialCriteria
import com.tb.chess.engine.evaluator.criteria.PawnStructureCriteria
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChessGameTest {

    @Test
    fun testDrawByRepetition() {
        val criteria = listOf(MaterialCriteria(), ControlStaticPositionCriteria(), PawnStructureCriteria())
        val evaluator = ChessEvaluator(criteria)
        val ai = MinimaxAI(evaluator)
        val game = ChessGame(evaluator, ai)

        // Ensure 2-player mode
        if (game.isVsAi) {
            game.toggleVsAi()
        }

        assertFalse(game.isDrawByRepetition)

        // 1st cycle
        game.onSquareClicked(Position('g', 1))
        game.onSquareClicked(Position('f', 3))

        game.onSquareClicked(Position('g', 8))
        game.onSquareClicked(Position('f', 6))

        game.onSquareClicked(Position('f', 3))
        game.onSquareClicked(Position('g', 1))

        game.onSquareClicked(Position('f', 6))
        game.onSquareClicked(Position('g', 8))

        assertFalse(game.isDrawByRepetition)

        // 2nd cycle
        game.onSquareClicked(Position('g', 1))
        game.onSquareClicked(Position('f', 3))

        game.onSquareClicked(Position('g', 8))
        game.onSquareClicked(Position('f', 6))

        game.onSquareClicked(Position('f', 3))
        game.onSquareClicked(Position('g', 1))

        game.onSquareClicked(Position('f', 6))
        game.onSquareClicked(Position('g', 8))

        assertTrue(game.isDrawByRepetition, "Game should be drawn by threefold repetition")
    }

    @Test
    fun testUndo() {
        val criteria = listOf(MaterialCriteria(), ControlStaticPositionCriteria(), PawnStructureCriteria())
        val evaluator = ChessEvaluator(criteria)
        val ai = MinimaxAI(evaluator)
        val game = ChessGame(evaluator, ai)

        if (game.isVsAi) {
            game.toggleVsAi()
        }

        assertFalse(game.canUndo)

        // Move e2 to e4
        val e2 = Position('e', 2)
        val e4 = Position('e', 4)
        game.onSquareClicked(e2)
        game.onSquareClicked(e4)

        assertTrue(game.canUndo)
        assertEquals(PieceColor.BLACK, game.currentTurn)
        assertEquals(null, game.board.getPiece(e2))
        assertEquals(PieceType.PAWN, game.board.getPiece(e4)?.type)

        // Undo move
        game.undo()

        assertFalse(game.canUndo)
        assertEquals(PieceColor.WHITE, game.currentTurn)
        assertEquals(PieceType.PAWN, game.board.getPiece(e2)?.type)
        assertEquals(null, game.board.getPiece(e4))
    }
}
