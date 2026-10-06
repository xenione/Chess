package com.tb.chess.model

import myapplication.shared.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource

data class ChessPiece(
    val initialPosition: Position,
    val type: PieceType,
    val color: PieceColor,
) {

    fun isIntrinsicalMoveValid(from: Position, to: Position): Boolean {
        val rPosition = if (color == PieceColor.WHITE)
            RelativePosition.position(to, from)
        else RelativePosition.position(from, to)
        return type.isIntrinsicalMoveValid(rPosition)
    }

    fun canBeAttack(from: Position, to: Position): Boolean {
        val rPosition = if (color == PieceColor.WHITE)
            RelativePosition.position(to, from)
        else RelativePosition.position(from, to)
        return type.canBeAttack(rPosition)
    }

    val symbol: Char
        get() = if (color == PieceColor.WHITE) type.fenChar.uppercaseChar() else type.fenChar

    // TODO eliminar de aqui
    fun drawableResource(): DrawableResource {
        return when (color) {
            PieceColor.WHITE -> when (type) {
                PieceType.PAWN -> Res.drawable.ic_wp
                PieceType.KNIGHT -> Res.drawable.ic_wn
                PieceType.BISHOP -> Res.drawable.ic_wb
                PieceType.ROOK -> Res.drawable.ic_wr
                PieceType.QUEEN -> Res.drawable.ic_wq
                PieceType.KING -> Res.drawable.ic_wk
            }
            PieceColor.BLACK -> when (type) {
                PieceType.PAWN -> Res.drawable.ic_bp
                PieceType.KNIGHT -> Res.drawable.ic_bn
                PieceType.BISHOP -> Res.drawable.ic_bb
                PieceType.ROOK -> Res.drawable.ic_br
                PieceType.QUEEN -> Res.drawable.ic_bq
                PieceType.KING -> Res.drawable.ic_bk
            }
        }
    }
}
