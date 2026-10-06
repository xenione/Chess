package com.tb.chess.engine

import com.tb.chess.model.MoveRecord

object OpeningDetector {

    fun detectOpening(moveHistory: List<MoveRecord>): String {
        if (moveHistory.isEmpty()) return "Nueva partida (Preparando apertura)"

        val history = moveHistory.map { it.to.algebraic.lowercase() }
        val historySeq = history.joinToString(" ")

        return when {
            historySeq.startsWith("e4 e5 nf3 nc6 bc4 bc5") -> "Apertura Italiana (Giuoco Piano)"
            historySeq.startsWith("e4 e5 nf3 nc6 bc4") -> "Apertura Italiana"
            historySeq.startsWith("e4 e5 nf3 nc6 bb5 a6") -> "Apertura Ruy Lopez (Española)"
            historySeq.startsWith("e4 e5 nf3 nc6 bb5") -> "Apertura Ruy Lopez"
            historySeq.startsWith("e4 e5 nf3 nc6 d4") -> "Apertura Escocesa"
            historySeq.startsWith("e4 e5 nf3 nc6") -> "Apertura de los Tres Caballos"
            historySeq.startsWith("e4 e5 nf3") -> "Apertura del Caballo de Rey"
            historySeq.startsWith("e4 e5 f4") -> "Gambito de Rey"
            historySeq.startsWith("e4 e5 bc4") -> "Apertura del Alfil"
            historySeq.startsWith("e4 e5") -> "Apertura de Peón de Rey"
            historySeq.startsWith("e4 c5") -> "Defensa Siciliana"
            historySeq.startsWith("e4 e6") -> "Defensa Francesa"
            historySeq.startsWith("e4 c6") -> "Defensa Caro-Kann"
            historySeq.startsWith("e4 nf6") -> "Defensa Alekhine"
            historySeq.startsWith("e4") -> "Apertura de Peón de Rey"

            historySeq.startsWith("d4 d5 c4 e6") -> "Gambito de Dama Rehusado"
            historySeq.startsWith("d4 d5 c4 dxc4") -> "Gambito de Dama Aceptado"
            historySeq.startsWith("d4 d5 c4") -> "Gambito de Dama"
            historySeq.startsWith("d4 d5 bf4") -> "Sistema Londres"
            historySeq.startsWith("d4 d5") -> "Apertura de Peón de Dama"
            historySeq.startsWith("d4 nf6 c4 g6") -> "Defensa India de Rey / Grünfeld"
            historySeq.startsWith("d4 nf6 c4 e6") -> "Defensa Nimzoindia / Indio de Dama"
            historySeq.startsWith("d4 nf6") -> "Defensa India"
            historySeq.startsWith("d4") -> "Apertura de Peón de Dama"

            historySeq.startsWith("c4 e5") -> "Apertura Inglesa (Siciliana Invertida)"
            historySeq.startsWith("c4 nf6") -> "Apertura Inglesa"
            historySeq.startsWith("c4") -> "Apertura Inglesa"

            historySeq.startsWith("nf3 d5") -> "Apertura Reti"
            historySeq.startsWith("nf3") -> "Apertura Reti"

            moveHistory.size <= 8 -> "Apertura en desarrollo"
            else -> "Medio juego / Posición compleja"
        }
    }
}
