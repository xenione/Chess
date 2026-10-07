package com.tb.chess.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tb.chess.model.*
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.exp

data class MovePair(
    val turnNumber: Int,
    val whiteMove: MoveRecord?,
    val blackMove: MoveRecord?
)

@Composable
fun PromotionDialog(color: PieceColor, onSelected: (PieceType) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(360.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF383838)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¡Promoción de Peón!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Elige la pieza a la que deseas promocionar:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val pieces =
                        listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)
                    for (type in pieces) {
                        val pieceSkin = ChessPieceSkin.getSkin(color, type)
                        Button(
                            onClick = { onSelected(type) },
                            modifier = Modifier.size(64.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A2511)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Image(
                                painter = painterResource(pieceSkin),
                                contentDescription = type.name,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluationBar(scoreCentipawns: Int) {
    val whiteRatio = remember(scoreCentipawns) {
        val clamped = scoreCentipawns.coerceIn(-1000, 1000)
        1f / (1f + exp(-clamped / 300f))
    }

    Card(
        modifier = Modifier
            .width(36.dp)
            .height(640.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Black advantage section (top)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f - whiteRatio)
                        .background(Color(0xFF262626))
                )
                // White advantage section (bottom)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(whiteRatio)
                        .background(Color(0xFFE0E0E0))
                )
            }

            // Score text overlay in the center
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val integerPart = scoreCentipawns / 100
                val decimalPart = abs(scoreCentipawns % 100) / 10
                val formatted = if (scoreCentipawns >= 0) "+$integerPart.$decimalPart" else "$integerPart.$decimalPart"

                Surface(
                    color = Color(0xAA000000),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = formatted,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChessGameScreen() {
    val game = koinInject<ChessGame>()
    val boardVersion = game.boardVersion

    val files = listOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h')
    val ranks = listOf('8', '7', '6', '5', '4', '3', '2', '1')

    val movePairs = remember(game.moveHistory) {
        val pairs = mutableListOf<MovePair>()
        for (i in game.moveHistory.indices step 2) {
            val white = game.moveHistory[i]
            val black = if (i + 1 < game.moveHistory.size) game.moveHistory[i + 1] else null
            pairs.add(MovePair((i / 2) + 1, white, black))
        }
        pairs
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2C2C))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Board and controls (Scaled up for larger display)
            Column(
                modifier = Modifier.width(580.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title & Turn Indicator Card with Check / Checkmate / Stalemate alerts and Undo button inside header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            game.isCheckmate -> Color(0xFFB71C1C)
                            game.isStalemate || game.isDrawByRepetition -> Color(0xFFE65100)
                            else -> Color(0xFF383838)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { game.undo() },
                                enabled = game.canUndo,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE65100),
                                    disabledContainerColor = Color(0xFF555555)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("⟲ Deshacer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Text(
                                text = when {
                                    game.isCheckmate -> "¡JAQUE MATE! Ganan las ${if (game.winner == PieceColor.WHITE) "Blancas ♔" else "Negras ♚"}"
                                    game.isStalemate -> "¡TABLAS POR AHOGADO! Empate 🤝"
                                    game.isDrawByRepetition -> "¡TABLAS POR REPETICIÓN! Empate 🤝"
                                    game.isKingInCheck -> "¡JAQUE! ⚠️"
                                    else -> if (game.isVsAi) "Modo: vs Computadora 🤖" else "Modo: 2 Jugadores 👥"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = if (game.isCheckmate || game.isStalemate || game.isDrawByRepetition || game.isKingInCheck) Color.White else Color(0xFFD2B48C),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = if (game.isCheckmate || game.isStalemate || game.isDrawByRepetition) "Fin del juego" else "Turno: ${if (game.currentTurn == PieceColor.WHITE) "Blancas ♔" else "Negras ♚"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Chess Board with Margins (Larger size: 580.dp)
                Box(
                    modifier = Modifier
                        .size(580.dp)
                        .shadow(10.dp, RoundedCornerShape(8.dp))
                        .background(Color(0xFF4A2511), RoundedCornerShape(8.dp))
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Board rows with Left Rank labels (8-1) and 8x8 grid
                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            // Left Ranks (8-1)
                            Column(
                                modifier = Modifier
                                    .width(28.dp)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceAround,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                for (rank in ranks) {
                                    Text(
                                        text = rank.toString(),
                                        color = Color(0xFFD2B48C),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            // Actual 8x8 Grid
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(Color(0xFFB58863))
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    for (row in 0..7) {
                                        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                            for (col in 0..7) {
                                                val pos = Position(row, col)
                                                val piece = game.board.getPiece(pos)
                                                val isLight = (row + col) % 2 == 0
                                                val isSelected = game.selectedPosition == pos
                                                val isLastMove = pos == game.lastMoveFrom || pos == game.lastMoveTo
                                                val isLegalMove = pos in game.currentLegalMoves

                                                val baseColor = if (isLight) Color(0xFFF0D9B5) else Color(0xFFB58863)
                                                val squareColor = if (isSelected) {
                                                    Color(0xFFBBCB2B)
                                                } else if (isLastMove) {
                                                    val green = Color(0xFFCDDC39)
                                                    Color(
                                                        red = green.red * 0.45f + baseColor.red * 0.55f,
                                                        green = green.green * 0.45f + baseColor.green * 0.55f,
                                                        blue = green.blue * 0.45f + baseColor.blue * 0.55f
                                                    )
                                                } else if (isLegalMove && piece != null) {
                                                    val captureRed = Color(0xFFE53935)
                                                    Color(
                                                        red = captureRed.red * 0.4f + baseColor.red * 0.6f,
                                                        green = captureRed.green * 0.4f + baseColor.green * 0.6f,
                                                        blue = captureRed.blue * 0.4f + baseColor.blue * 0.6f
                                                    )
                                                } else {
                                                    baseColor
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .background(squareColor)
                                                        .clickable {
                                                            game.onSquareClicked(pos)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (piece != null) {
                                                        // Larger piece size relative to square (48.dp)
                                                        Image(
                                                            painter = painterResource(piece.drawableResource()),
                                                            contentDescription = "${piece.color} ${piece.type}",
                                                            modifier = Modifier.size(48.dp)
                                                        )
                                                    } else if (isLegalMove) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0x55000000))
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom File labels (A-H)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .padding(start = 28.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (file in files) {
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = file.toString(),
                                        color = Color(0xFFD2B48C),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { game.toggleVsAi() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (game.isVsAi) Color(0xFF1976D2) else Color(0xFF555555)
                        )
                    ) {
                        Text(
                            text = if (game.isVsAi) "VS IA (Activo)" else "Modo IA",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { game.reset() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Reiniciar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Evaluation Bar (Scaled up height to 640.dp)
            EvaluationBar(scoreCentipawns = game.evaluationCentipawns)

            Spacer(modifier = Modifier.width(16.dp))

            // Right Column: Move History Panel (Scaled up height to 640.dp)
            Card(
                modifier = Modifier
                    .width(300.dp)
                    .height(640.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF383838)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Historial de Movimientos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = game.currentOpening,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD2B48C),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)

                    Spacer(modifier = Modifier.height(8.dp))

                    if (movePairs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no hay movimientos",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(movePairs) { pair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF2C2C2C), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${pair.turnNumber}.",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(32.dp)
                                    )

                                    // White Move
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (pair.whiteMove != null) {
                                            Image(
                                                painter = painterResource(pair.whiteMove.piece.drawableResource()),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = pair.whiteMove.to.algebraic,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    // Black Move
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (pair.blackMove != null) {
                                            Image(
                                                painter = painterResource(pair.blackMove.piece.drawableResource()),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = pair.blackMove.to.algebraic,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Promotion Dialog Overlay
        val promo = game.pendingPromotion
        if (promo != null) {
            PromotionDialog(color = promo.color) { selectedType ->
                game.promotePawn(selectedType)
            }
        }
    }
}
