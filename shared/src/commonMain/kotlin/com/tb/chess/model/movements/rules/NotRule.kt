package com.tb.chess.model.movements.rules

import com.tb.chess.model.ChessPiece
import com.tb.chess.model.Position

class NotRule(private val rule: Rule) : Rule {

    override fun isValid(piece: ChessPiece, from: Position, to: Position): Boolean {
        return !rule.isValid(piece, from, to)
    }

}