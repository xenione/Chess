package com.tb.chess.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tb.chess.engine.evaluator.ChessEvaluator
import com.tb.chess.engine.MinimaxAI
import com.tb.chess.engine.OpeningBook
import com.tb.chess.engine.OpeningDetector
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.milliseconds

data class MoveRecord(
    val piece: ChessPiece,
    val from: Position,
    val to: Position,
    val isWhite: Boolean
)

data class PromotionState(
    val from: Position,
    val to: Position,
    val color: PieceColor
)

class ChessGame(
    private val chessEvaluator: ChessEvaluator,
    private val ai: MinimaxAI
) {
    val board = ChessBoard()

    var currentTurn by mutableStateOf(PieceColor.WHITE)
        private set

    var selectedPosition by mutableStateOf<Position?>(null)
        private set

    var boardVersion by mutableStateOf(0)
        private set

    var lastMoveFrom by mutableStateOf<Position?>(null)
        private set

    var lastMoveTo by mutableStateOf<Position?>(null)
        private set

    var moveHistory by mutableStateOf<List<MoveRecord>>(emptyList())
        private set

    var isVsAi by mutableStateOf(true)
        private set

    var isCheckmate by mutableStateOf(false)
        private set

    var isStalemate by mutableStateOf(false)
        private set

    var winner by mutableStateOf<PieceColor?>(null)
        private set

    var pendingPromotion by mutableStateOf<PromotionState?>(null)
        private set

    val isKingInCheck: Boolean
        get() {
            val v = boardVersion
            return MoveValidator.isKingInCheck(board, currentTurn)
        }

    val currentOpening: String
        get() {
            val v = boardVersion
            return OpeningDetector.detectOpening(moveHistory)
        }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val currentLegalMoves: List<Position>
        get() {
            val selected = selectedPosition ?: return emptyList()
            return MoveValidator.getLegalMoves(board, selected)
        }

    val evaluationCentipawns: Int
        get() {
            val v = boardVersion
            return chessEvaluator.evaluate(board)
        }

    fun toggleVsAi() {
        isVsAi = !isVsAi
        if (isVsAi && currentTurn == PieceColor.BLACK && !isCheckmate && !isStalemate && pendingPromotion == null) {
            makeAiMove()
        }
    }

    fun onSquareClicked(position: Position) {
        if (isCheckmate || isStalemate || pendingPromotion != null) return
        if (isVsAi && currentTurn == PieceColor.BLACK) return // Wait for AI

        val selected = selectedPosition
        if (selected == null) {
            val piece = board.getPiece(position)
            if (piece != null && piece.color == currentTurn) {
                selectedPosition = position
            }
        } else {
            if (selected == position) {
                selectedPosition = null
            } else {
                val pieceAtTarget = board.getPiece(position)
                if (pieceAtTarget != null && pieceAtTarget.color == currentTurn) {
                    selectedPosition = position
                } else {
                    // Validate move with isCompletelyLegalMove (ensures king not left in check)
                    if (MoveValidator.isCompletelyLegalMove(board, selected, position)) {
                        val piece = board.getPiece(selected)
                        
                        // Check for pawn promotion
                        val isPromotion = piece?.type == PieceType.PAWN && 
                            ((piece.color == PieceColor.WHITE && position.row == 0) || 
                             (piece.color == PieceColor.BLACK && position.row == 7))

                        if (isPromotion) {
                            pendingPromotion = PromotionState(selected, position, piece.color)
                            selectedPosition = null
                        } else {
                            val result = board.movePiece(selected, position)
                            if (result is MoveResult.Success && piece != null) {
                                lastMoveFrom = selected
                                lastMoveTo = position
                                moveHistory = moveHistory + MoveRecord(piece, selected, position, currentTurn == PieceColor.WHITE)
                                currentTurn = currentTurn.opposite()
                                selectedPosition = null
                                boardVersion++

                                checkGameStatus()

                                if (!isCheckmate && !isStalemate && isVsAi && currentTurn == PieceColor.BLACK) {
                                    makeAiMove()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun promotePawn(selectedType: PieceType) {
        val promo = pendingPromotion ?: return
        val result = board.movePiece(promo.from, promo.to)
        if (result is MoveResult.Success) {
            val promotedPiece = ChessPiece(promo.to, selectedType, promo.color)
            board.setPiece(promo.to, promotedPiece)

            lastMoveFrom = promo.from
            lastMoveTo = promo.to
            moveHistory = moveHistory + MoveRecord(promotedPiece, promo.from, promo.to, currentTurn == PieceColor.WHITE)
            currentTurn = currentTurn.opposite()
            pendingPromotion = null
            boardVersion++

            checkGameStatus()

            if (!isCheckmate && !isStalemate && isVsAi && currentTurn == PieceColor.BLACK) {
                makeAiMove()
            }
        }
    }

    private fun checkGameStatus() {
        val hasMoves = MoveValidator.hasAnyLegalMoves(board, currentTurn)
        if (!hasMoves) {
            if (MoveValidator.isKingInCheck(board, currentTurn)) {
                isCheckmate = true
                winner = currentTurn.opposite()
            } else {
                isStalemate = true
            }
        }
    }

    private fun makeAiMove() {
        if (isCheckmate || isStalemate || pendingPromotion != null) return
        coroutineScope.launch {
            delay(1500.milliseconds)

            val bookMove = OpeningBook.getBookMove(moveHistory)
            val bestMove = bookMove ?: withContext(Dispatchers.Default) {
                ai.findBestMove(board, PieceColor.BLACK)
            }

            if (bestMove != null && currentTurn == PieceColor.BLACK && !isCheckmate && !isStalemate && pendingPromotion == null) {
                if (MoveValidator.isCompletelyLegalMove(board, bestMove.first, bestMove.second)) {
                    val piece = board.getPiece(bestMove.first)
                    val isPromotion = piece?.type == PieceType.PAWN && bestMove.second.row == 7

                    val result = board.movePiece(bestMove.first, bestMove.second)
                    if (result is MoveResult.Success && piece != null) {
                        val finalPiece = if (isPromotion) {
                            val queen = ChessPiece(bestMove.second, PieceType.QUEEN, PieceColor.BLACK)
                            board.setPiece(bestMove.second, queen)
                            queen
                        } else {
                            piece
                        }

                        lastMoveFrom = bestMove.first
                        lastMoveTo = bestMove.second
                        moveHistory = moveHistory + MoveRecord(finalPiece, bestMove.first, bestMove.second, false)
                        currentTurn = currentTurn.opposite()
                        boardVersion++

                        checkGameStatus()
                    }
                }
            }
        }
    }

    fun reset() {
        board.setupInitialPosition()
        currentTurn = PieceColor.WHITE
        selectedPosition = null
        lastMoveFrom = null
        lastMoveTo = null
        moveHistory = emptyList()
        isCheckmate = false
        isStalemate = false
        winner = null
        pendingPromotion = null
        boardVersion++

        if (isVsAi && currentTurn == PieceColor.BLACK) {
            makeAiMove()
        }
    }
}
