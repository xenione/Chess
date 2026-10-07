package com.tb.chess.model.movements

import com.tb.chess.model.PieceType
import com.tb.chess.model.movements.rules.MoveRule

interface RuleProvider {

    fun provide(type: PieceType): MoveRule


}