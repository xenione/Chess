package com.tb.chess.model

import kotlin.math.abs

enum class PieceType(val displayName: String, val fenChar: Char) {
    PAWN("Pawn", 'p') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return position.rowDiff == 1 && position.colDiff == 0
        }

        override fun moveSetFrom(position: RelativePosition): List<Position> {
            return listOf()
        }

        override fun canBeAttack(position: RelativePosition): Boolean {
           return position.absColDiff() == 1 &&  position.rowDiff==1
        }
    },
    KNIGHT("Knight", 'n') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return (position.absRowDiff() == 2 && position.absColDiff() == 1) || (position.absRowDiff() == 1 && position.absColDiff() == 2)
        }
        override fun moveSetFrom(position: RelativePosition): List<Position> {
            return listOf()
        }
    },
    BISHOP("Bishop", 'b') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return position.absRowDiff() == position.absColDiff()
        }
        override fun moveSetFrom(position: RelativePosition): List<Position> {
            return listOf()
        }
    },
    ROOK("Rook", 'r') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return position.absRowDiff() == 0 || position.absColDiff() == 0
        }
        override fun moveSetFrom(position: RelativePosition): List<Position> {
            return listOf()
        }
    },
    QUEEN("Queen", 'q') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return ROOK.isIntrinsicalMoveValid(position) || BISHOP.isIntrinsicalMoveValid(position)
        }
        override fun moveSetFrom(position: RelativePosition): List<Position> {
            return listOf()
        }
    },
    KING("King", 'k') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return position.absRowDiff() <= 1 && position.absColDiff() <= 1
        }
        override fun moveSetFrom(position: RelativePosition): List<Position> {
            return listOf()
        }
    };

    abstract fun moveSetFrom(position: RelativePosition):List<Position>

    abstract fun isIntrinsicalMoveValid(position: RelativePosition) : Boolean

    open fun canBeAttack(position: RelativePosition) = isIntrinsicalMoveValid(position)
}
