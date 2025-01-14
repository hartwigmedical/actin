package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage

internal object DerivedTumorStageEvaluation {

    fun create(derived: Map<TumorStage, Evaluation>, createEvaluation: (String) -> Evaluation): Evaluation {
        val worstEvaluation = worstEvaluation(derived)
        return createEvaluation(allMessagesFrom(worstEvaluation))
    }

    private fun worstEvaluation(derived: Map<TumorStage, Evaluation>): Evaluation {
        return derived.values.minBy(Evaluation::result)
    }

    private fun allMessagesFrom(worstEvaluation: Evaluation): String {
        return listOf(
            worstEvaluation.passMessages,
            worstEvaluation.warnMessages,
            worstEvaluation.failMessages,
            worstEvaluation.undeterminedMessages
        )
            .flatten()
            .joinToString(". ")
    }
}