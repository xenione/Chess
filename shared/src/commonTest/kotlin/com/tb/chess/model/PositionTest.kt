package com.tb.chess.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PositionTest {

    @Test
    fun testPositionCreation() {
        val pos = Position('e', 4)
        assertEquals('e', pos.file)
        assertEquals(4, pos.rank)
        assertEquals("e4", pos.algebraic)
        assertEquals(4, pos.row)
        assertEquals(4, pos.col)
    }

    @Test
    fun testFromAlgebraic() {
        val pos = Position.fromAlgebraic("a1")
        assertNotNull(pos)
        assertEquals('a', pos.file)
        assertEquals(1, pos.rank)
        assertEquals(7, pos.row)
        assertEquals(0, pos.col)

        val posH8 = Position.fromAlgebraic("h8")
        assertNotNull(posH8)
        assertEquals('h', posH8.file)
        assertEquals(8, posH8.rank)
        assertEquals(0, posH8.row)
        assertEquals(7, posH8.col)

        assertNull(Position.fromAlgebraic("i9"))
        assertNull(Position.fromAlgebraic("e"))
    }
}
