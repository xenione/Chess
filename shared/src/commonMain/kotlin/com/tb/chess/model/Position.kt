package com.tb.chess.model

/**
 * Represents a position on the chess board.
 *
 * Note on coordinate systems:
 * - Internal Matrix (`row`, `col`): 0..7 where row 0 is the top of the board (Black pieces start)
 *   and row 7 is the bottom of the board (White pieces start).
 * - Chess Notation (`file`, `rank`): files are 'a'..'h' (columns left to right) and
 *   ranks are 1..8 (rows bottom to top). Because matrix rows go top-to-bottom while
 *   chess ranks go bottom-to-top, `rank` is inverted relative to `row` (`rank = 8 - row`).
 */
data class Position(val row: Int, val col: Int) {
    init {
        require(row in 0..7 && col in 0..7) { "Row and col must be within 0..7, got row=$row, col=$col" }
    }

    // Constructor using chess notation: file ('a'..'h') and rank (1..8)
    constructor(file: Char, rank: Int) : this(
        row = 8 - rank,
        col = file.lowercaseChar() - 'a'
    ) {
        require(file.lowercaseChar() in 'a'..'h') { "File must be between 'a' and 'h', got $file" }
        require(rank in 1..8) { "Rank must be between 1 and 8, got $rank" }
    }

    val file: Char
        get() = 'a' + col

    val rank: Int
        get() = 8 - row

    val algebraic: String
        get() = "$file$rank"

    companion object {
        fun fromAlgebraic(algebraic: String): Position? {
            if (algebraic.length != 2) return null
            val fileChar = algebraic[0].lowercaseChar()
            val rankChar = algebraic[1]
            if (fileChar !in 'a'..'h' || rankChar !in '1'..'8') return null
            return Position(fileChar, rankChar - '0')
        }
    }
}
