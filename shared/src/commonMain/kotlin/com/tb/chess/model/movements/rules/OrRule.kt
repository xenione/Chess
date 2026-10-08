package com.tb.chess.model.movements.rules

import com.tb.chess.model.ChessPiece
import com.tb.chess.model.Position

class OrRule private constructor(private val rules: List<Rule>) : Rule {


    override fun isValid(piece: ChessPiece, from: Position, to: Position): Boolean {
        return rules.any { it.isValid(piece, from, to) }
    }

    class Builder (rules: List<Rule>) {

        constructor(rule:Rule) : this(listOf(rule))

        constructor() : this(emptyList())

        private val rules = mutableListOf<Rule>().apply { addAll(rules) }

        fun or(rule: Rule): Builder {
            rules.add(rule)
            return this
        }

        fun build(): Rule {
            return OrRule(rules)
        }
    }
}