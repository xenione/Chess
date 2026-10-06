package com.tb.chess.model

import kotlin.math.absoluteValue


data class RelativePosition(val rowDiff: Int, val colDiff: Int) {

    companion object {

        fun position(from: Position, to: Position): RelativePosition {
            return RelativePosition(to.row - from.row, to.col - from.col)
        }
    }

    fun absRowDiff() = rowDiff.absoluteValue

    fun absColDiff() = colDiff.absoluteValue

}
