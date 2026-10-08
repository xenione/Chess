package com.tb.chess.model.movements.rules

import com.tb.chess.model.ChessPiece
import com.tb.chess.model.Position

class ChessRule(private val rule: Rule) : Rule {

    class Builder() {

        private val orRule = OrRule.Builder()

        fun case(case: Case): Builder {
            orRule.or(case)
            return this
        }

        fun build(): ChessRule {
            return ChessRule(orRule.build())
        }
    }

    class Case(private val rule: Rule): Rule {

        class CaseBuilder{
            private lateinit var moveRule: MoveRule

            private val conds = mutableListOf<Rule>()

            fun move(move: MoveRule):CaseBuilder {
                moveRule = move
                return this
            }

            fun condition(cond: Rule):CaseBuilder {
                conds.add(cond)
                return this
            }

            fun build(): Case {
                return Case(
                    AndRule.Builder()
                        .and(moveRule)
                        .and(conds)
                        .build()
                )
            }
        }

        override fun isValid(piece: ChessPiece, from: Position, to: Position): Boolean {
            return rule.isValid(piece, from, to)
        }
    }

    override fun isValid(piece: ChessPiece, from: Position, to: Position): Boolean {
        return rule.isValid(piece, from, to)
    }
}