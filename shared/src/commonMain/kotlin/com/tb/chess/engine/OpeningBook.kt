package com.tb.chess.engine

import com.tb.chess.model.MoveRecord
import com.tb.chess.model.Position

object OpeningBook {

    fun getBookMove(moveHistory: List<MoveRecord>): Pair<Position, Position>? {
        val history = moveHistory.map { it.to.algebraic.lowercase() }

        // 1. Responses to White's 1st move (history.size == 1)
        if (history.size == 1) {
            return when (history[0]) {
                "e4" -> Pair(Position('e', 7), Position('e', 5)) // 1... e5 (King's Pawn Game)
                "d4" -> Pair(Position('d', 7), Position('d', 5)) // 1... d5 (Queen's Pawn Game)
                "c4" -> Pair(Position('g', 8), Position('f', 6)) // 1... Nf6 (English / Indian)
                "nf3" -> Pair(Position('d', 7), Position('d', 5)) // 1. Nf3 d5
                else -> null
            }
        }

        // 2. Responses to White's 2nd move (history.size == 3)
        if (history.size == 3) {
            val firstWhite = history[0]
            val firstBlack = history[1]
            val secondWhite = history[2]

            if (firstWhite == "e4" && firstBlack == "e5") {
                return when (secondWhite) {
                    "nf3" -> Pair(Position('b', 8), Position('c', 6)) // 2. Nf3 Nc6 (Italian / Ruy Lopez)
                    "bc4" -> Pair(Position('g', 8), Position('f', 6)) // 2. Bc4 Nf6 (Bishop's Opening)
                    "f4" -> Pair(Position('e', 7), Position('e', 6))   // 2. f4 e6 (King's Gambit Declined)
                    "nc3" -> Pair(Position('g', 8), Position('f', 6)) // 2. Nc3 Nf6 (Vienna Game)
                    else -> null
                }
            }
            if (firstWhite == "d4" && firstBlack == "d5") {
                return when (secondWhite) {
                    "c4" -> Pair(Position('e', 7), Position('e', 6))   // 2. c4 e6 (Queen's Gambit Declined)
                    "nf3" -> Pair(Position('g', 8), Position('f', 6)) // 2. Nf3 Nf6
                    "bf4" -> Pair(Position('g', 8), Position('f', 6)) // London System -> Nf6
                    else -> null
                }
            }
        }

        // 3. Responses to White's 3rd move (history.size == 5)
        if (history.size == 5) {
            val firstWhite = history[0]
            val firstBlack = history[1]
            val secondWhite = history[2]
            val secondBlack = history[3]
            val thirdWhite = history[4]

            if (firstWhite == "e4" && firstBlack == "e5" && secondWhite == "nf3" && secondBlack == "nc6") {
                return when (thirdWhite) {
                    "bc4" -> Pair(Position('f', 8), Position('c', 5)) // 3. Bc4 Bc5 (Italian Game)
                    "bb5" -> Pair(Position('a', 7), Position('a', 6))   // 3. Bb5 a6 (Ruy Lopez / Spanish)
                    "d4" -> Pair(Position('e', 5), Position('d', 4))  // 3. d4 exd4 (Scotch Game capture)
                    else -> null
                }
            }
            if (firstWhite == "d4" && firstBlack == "d5" && secondWhite == "c4" && secondBlack == "e6") {
                return when (thirdWhite) {
                    "nc3" -> Pair(Position('g', 8), Position('f', 6)) // 3. Nc3 Nf6 (QGD)
                    "nf3" -> Pair(Position('g', 8), Position('f', 6)) // 3. Nf3 Nf6
                    else -> null
                }
            }
        }

        // 4. Responses to White's 4th move (history.size == 7)
        if (history.size == 7) {
            val historySeq = history.joinToString(" ")
            if (historySeq == "e4 e5 nf3 nc6 bb5 a6") {
                return Pair(Position('b', 8), Position('d', 7)) // 4... Nbd7
            }
            if (historySeq == "e4 e5 nf3 nc6 bc4 bc5") {
                return Pair(Position('c', 7), Position('c', 6)) // 4... c6 (Giuoco Piano)
            }
        }

        return null
    }
}
