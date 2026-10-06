package com.tb.chess.ui

import androidx.compose.ui.window.Notification
import com.tb.chess.model.PieceColor
import com.tb.chess.model.PieceType
import myapplication.shared.generated.resources.Res
import myapplication.shared.generated.resources.ic_bb
import myapplication.shared.generated.resources.ic_bk
import myapplication.shared.generated.resources.ic_bn
import myapplication.shared.generated.resources.ic_bp
import myapplication.shared.generated.resources.ic_bq
import myapplication.shared.generated.resources.ic_br
import myapplication.shared.generated.resources.ic_wb
import myapplication.shared.generated.resources.ic_wk
import myapplication.shared.generated.resources.ic_wn
import myapplication.shared.generated.resources.ic_wp
import myapplication.shared.generated.resources.ic_wq
import myapplication.shared.generated.resources.ic_wr
import org.jetbrains.compose.resources.DrawableResource

object ChessPieceSkin {

    fun getSkin(color: PieceColor, type: PieceType): DrawableResource {
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