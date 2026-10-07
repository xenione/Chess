package com.tb.chess.model.movements.rules

import com.tb.chess.model.ChessPiece
import com.tb.chess.model.Position

class AndRule private constructor(private val rules: List<MoveRule>) : MoveRule {


    override fun isValid(piece: ChessPiece, from: Position, to: Position): Boolean {
        return rules.all { it.isValid(piece, from, to) }
    }

    class Builder (rules: List<MoveRule>) {

        constructor(rule:MoveRule) : this(listOf(rule))

        constructor() : this(emptyList())

        private val rules = mutableListOf<MoveRule>().apply { addAll(rules) }


        fun and(rule: MoveRule): Builder {
            rules.add(rule)
            return this
        }

        fun build(): MoveRule {
            return AndRule(rules)
        }
    }
}