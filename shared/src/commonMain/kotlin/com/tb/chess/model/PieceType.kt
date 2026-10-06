package com.tb.chess.model

import kotlin.math.abs

enum class PieceType(val displayName: String, val fenChar: Char) {
    PAWN("Pawn", 'p') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return position.rowDiff == 1 && position.colDiff == 0
        }

        override fun canBeAttack(position: RelativePosition): Boolean {
           return position.absColDiff() == 1 &&  position.rowDiff==1
        }
    },
    KNIGHT("Knight", 'n') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return (position.absRowDiff() == 2 && position.absColDiff() == 1) || (position.absRowDiff() == 1 && position.absColDiff() == 2)
        }
    },
    BISHOP("Bishop", 'b') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return position.absRowDiff() == position.absColDiff()
        }
    },
    ROOK("Rook", 'r') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return position.absRowDiff() == 0 || position.absColDiff() == 0
        }
    },
    QUEEN("Queen", 'q') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return ROOK.isIntrinsicalMoveValid(position) || BISHOP.isIntrinsicalMoveValid(position)
        }
    },
    KING("King", 'k') {
        override fun isIntrinsicalMoveValid(position: RelativePosition): Boolean {
            return position.absRowDiff() <= 1 && position.absColDiff() <= 1
        }
    };

    abstract fun isIntrinsicalMoveValid(position: RelativePosition) : Boolean

    open fun canBeAttack(position: RelativePosition) = isIntrinsicalMoveValid(position)
}
