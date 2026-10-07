package com.tb.chess.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tb.chess.engine.evaluator.ChessEvaluator
import com.tb.chess.engine.MinimaxAI
import com.tb.chess.engine.OpeningBook
import com.tb.chess.engine.OpeningDetector
import com.tb.chess.model.movements.rules.MoveValidator
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

data class GameStateSnapshot(
    val board: ChessBoard,
    val currentTurn: PieceColor,
    val moveHistory: List<MoveRecord>,
    val lastMoveFrom: Position?,
    val lastMoveTo: Position?,
    val isCheckmate: Boolean,
    val isStalemate: Boolean,
    val isDrawByRepetition: Boolean,
    val winner: PieceColor?,
    val pendingPromotion: PromotionState?,
    val boardStateHistory: Map<String, Int>,
    val capturedByWhite: List<ChessPiece>,
    val capturedByBlack: List<ChessPiece>
)

class ChessGame(
    private val chessEvaluator: ChessEvaluator,
    private val ai: MinimaxAI
) {
    val board = ChessBoard()
    private val moveValidator = MoveValidator(board)

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

    var isDrawByRepetition by mutableStateOf(false)
        private set

    var capturedByWhite by mutableStateOf<List<ChessPiece>>(emptyList())
        private set

    var capturedByBlack by mutableStateOf<List<ChessPiece>>(emptyList())
        private set

    private val undoStack = mutableListOf<GameStateSnapshot>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    private fun saveSnapshot() {
        undoStack.add(
            GameStateSnapshot(
                board = board.copy(),
                currentTurn = currentTurn,
                moveHistory = moveHistory,
                lastMoveFrom = lastMoveFrom,
                lastMoveTo = lastMoveTo,
                isCheckmate = isCheckmate,
                isStalemate = isStalemate,
                isDrawByRepetition = isDrawByRepetition,
                winner = winner,
                pendingPromotion = pendingPromotion,
                boardStateHistory = boardStateHistory.toMap(),
                capturedByWhite = capturedByWhite.toList(),
                capturedByBlack = capturedByBlack.toList()
            )
        )
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val snapshot = undoStack.removeAt(undoStack.lastIndex)
        board.restore(snapshot.board)
        currentTurn = snapshot.currentTurn
        moveHistory = snapshot.moveHistory
        lastMoveFrom = snapshot.lastMoveFrom
        lastMoveTo = snapshot.lastMoveTo
        isCheckmate = snapshot.isCheckmate
        isStalemate = snapshot.isStalemate
        isDrawByRepetition = snapshot.isDrawByRepetition
        winner = snapshot.winner
        pendingPromotion = snapshot.pendingPromotion
        boardStateHistory.clear()
        boardStateHistory.putAll(snapshot.boardStateHistory)
        capturedByWhite = snapshot.capturedByWhite
        capturedByBlack = snapshot.capturedByBlack
        selectedPosition = null
        boardVersion++
    }

    private val boardStateHistory = mutableMapOf<String, Int>()

    init {
        recordInitialState()
    }

    private fun getBoardSignature(): String {
        val squaresSig = board.getAllPiecesState()
            .sortedBy { it.position.row * 8 + it.position.col }
            .joinToString(";") { "${it.position.row},${it.position.col}:${it.piece.color}:${it.piece.type}" }

        val whiteKingMoved = board.getPieceState(Position(7, 4))?.hasMoved ?: true
        val whiteRookKMoved = board.getPieceState(Position(7, 7))?.hasMoved ?: true
        val whiteRookQMoved = board.getPieceState(Position(7, 0))?.hasMoved ?: true
        val blackKingMoved = board.getPieceState(Position(0, 4))?.hasMoved ?: true
        val blackRookKMoved = board.getPieceState(Position(0, 7))?.hasMoved ?: true
        val blackRookQMoved = board.getPieceState(Position(0, 0))?.hasMoved ?: true

        return "$squaresSig|$currentTurn|${board.enPassantTarget}|$whiteKingMoved|$whiteRookKMoved|$whiteRookQMoved|$blackKingMoved|$blackRookKMoved|$blackRookQMoved"
    }

    private fun recordInitialState() {
        boardStateHistory.clear()
        val sig = getBoardSignature()
        boardStateHistory[sig] = 1
    }

    private fun checkRepetition() {
        val sig = getBoardSignature()
        val count = (boardStateHistory[sig] ?: 0) + 1
        boardStateHistory[sig] = count
        if (count >= 3) {
            isDrawByRepetition = true
        }
    }

    var winner by mutableStateOf<PieceColor?>(null)
        private set

    var pendingPromotion by mutableStateOf<PromotionState?>(null)
        private set

    val isKingInCheck: Boolean
        get() {
            val v = boardVersion
            return moveValidator.isKingInCheck(board, currentTurn)
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
            return moveValidator.getLegalMoves(board, selected)
        }

    val evaluationCentipawns: Int
        get() {
            val v = boardVersion
            return chessEvaluator.evaluate(board)
        }

    fun toggleVsAi() {
        isVsAi = !isVsAi
        undoStack.clear()
        if (isVsAi && currentTurn == PieceColor.BLACK && !isCheckmate && !isStalemate && !isDrawByRepetition && pendingPromotion == null) {
            makeAiMove()
        }
    }

    fun onSquareClicked(position: Position) {
        if (isCheckmate || isStalemate || isDrawByRepetition || pendingPromotion != null) return
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
                    if (moveValidator.isCompletelyLegalMove(board, selected, position)) {
                        val piece = board.getPiece(selected)
                        
                        // Check for pawn promotion
                        val isPromotion = piece?.type == PieceType.PAWN && 
                            ((piece.color == PieceColor.WHITE && position.row == 0) || 
                             (piece.color == PieceColor.BLACK && position.row == 7))

                        if (isPromotion) {
                            pendingPromotion = PromotionState(selected, position, piece.color)
                            selectedPosition = null
                        } else {
                            saveSnapshot()
                            val capturingColor = currentTurn
                            val result = board.movePiece(selected, position)
                            if (result is MoveResult.Success && piece != null) {
                                if (result.capturedPiece != null) {
                                    if (capturingColor == PieceColor.WHITE) {
                                        capturedByWhite = capturedByWhite + result.capturedPiece
                                    } else {
                                        capturedByBlack = capturedByBlack + result.capturedPiece
                                    }
                                }
                                lastMoveFrom = selected
                                lastMoveTo = position
                                moveHistory = moveHistory + MoveRecord(piece, selected, position, currentTurn == PieceColor.WHITE)
                                currentTurn = currentTurn.opposite()
                                selectedPosition = null
                                boardVersion++

                                checkGameStatus()
                                checkRepetition()

                                if (!isCheckmate && !isStalemate && !isDrawByRepetition && isVsAi && currentTurn == PieceColor.BLACK) {
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
        saveSnapshot()
        val capturingColor = currentTurn
        val result = board.movePiece(promo.from, promo.to)
        if (result is MoveResult.Success) {
            if (result.capturedPiece != null) {
                if (capturingColor == PieceColor.WHITE) {
                    capturedByWhite = capturedByWhite + result.capturedPiece
                } else {
                    capturedByBlack = capturedByBlack + result.capturedPiece
                }
            }
            val promotedPiece = ChessPiece(promo.to, selectedType, promo.color)
            board.setPiece(promo.to, promotedPiece)

            lastMoveFrom = promo.from
            lastMoveTo = promo.to
            moveHistory = moveHistory + MoveRecord(promotedPiece, promo.from, promo.to, currentTurn == PieceColor.WHITE)
            currentTurn = currentTurn.opposite()
            pendingPromotion = null
            boardVersion++

            checkGameStatus()
            checkRepetition()

            if (!isCheckmate && !isStalemate && !isDrawByRepetition && isVsAi && currentTurn == PieceColor.BLACK) {
                makeAiMove()
            }
        }
    }

    private fun checkGameStatus() {
        if (isDrawByRepetition) return
        val hasMoves = moveValidator.hasAnyLegalMoves(board, currentTurn)
        if (!hasMoves) {
            if (moveValidator.isKingInCheck(board, currentTurn)) {
                isCheckmate = true
                winner = currentTurn.opposite()
            } else {
                isStalemate = true
            }
        }
    }

    private fun makeAiMove() {
        if (isCheckmate || isStalemate || isDrawByRepetition || pendingPromotion != null) return
        coroutineScope.launch {
            delay(1500.milliseconds)

            val bookMove = OpeningBook.getBookMove(moveHistory)
            val bestMove = bookMove ?: withContext(Dispatchers.Default) {
                ai.findBestMove(board, PieceColor.BLACK)
            }

            if (bestMove != null && currentTurn == PieceColor.BLACK && !isCheckmate && !isStalemate && !isDrawByRepetition && pendingPromotion == null) {
                if (moveValidator.isCompletelyLegalMove(board, bestMove.first, bestMove.second)) {
                    val piece = board.getPiece(bestMove.first)
                    val isPromotion = piece?.type == PieceType.PAWN && bestMove.second.row == 7

                    val capturingColor = currentTurn
                    val result = board.movePiece(bestMove.first, bestMove.second)
                    if (result is MoveResult.Success && piece != null) {
                        if (result.capturedPiece != null) {
                            if (capturingColor == PieceColor.WHITE) {
                                capturedByWhite = capturedByWhite + result.capturedPiece
                            } else {
                                capturedByBlack = capturedByBlack + result.capturedPiece
                            }
                        }
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
                        checkRepetition()
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
        isDrawByRepetition = false
        winner = null
        pendingPromotion = null
        capturedByWhite = emptyList()
        capturedByBlack = emptyList()
        undoStack.clear()
        boardVersion++
        recordInitialState()

        if (isVsAi && currentTurn == PieceColor.BLACK) {
            makeAiMove()
        }
    }
}
