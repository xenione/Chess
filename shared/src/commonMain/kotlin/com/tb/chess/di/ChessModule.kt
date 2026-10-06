package com.tb.chess.di

import com.tb.chess.engine.MinimaxAI
import com.tb.chess.engine.evaluator.ChessEvaluator
import com.tb.chess.engine.evaluator.criteria.ControlStaticPositionCriteria
import com.tb.chess.engine.evaluator.criteria.EvaluationCriteria
import com.tb.chess.engine.evaluator.criteria.MaterialCriteria
import com.tb.chess.engine.evaluator.criteria.PawnStructureCriteria
import com.tb.chess.model.ChessGame
import org.koin.core.qualifier.named
import org.koin.dsl.module

val chessModule = module {
    // 1. The three evaluation criteria
    single<EvaluationCriteria>(named("material")) { MaterialCriteria() }
    single<EvaluationCriteria>(named("position")) { ControlStaticPositionCriteria() }
    single<EvaluationCriteria>(named("pawn")) { PawnStructureCriteria() }

    // 2. ChessEvaluator injecting the three criteria dependencies
    single {
        ChessEvaluator(
            evalCriteriaList = listOf(
                get(named("material")),
                get(named("position")),
                get(named("pawn"))
            )
        )
    }

    // 3. Minimax AI engine
    factory { MinimaxAI(chessEvaluator = get(),maxDepth = 4 ) }

    // 4. Chess game state holder
    factory { ChessGame(get(), get()) }
}
