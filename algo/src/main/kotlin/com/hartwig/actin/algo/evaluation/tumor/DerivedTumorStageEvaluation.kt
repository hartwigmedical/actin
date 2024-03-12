package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.clinical.datamodel.TumorStage

internal object DerivedTumorStageEvaluation {
   
    fun create(derived: Map<TumorStage, Evaluation>, createEvaluation: (String, String) -> Evaluation): Evaluation {
        val worstEvaluation = worstEvaluation(derived)
        return createEvaluation(allSpecificMessagesFrom(derived, worstEvaluation), allGeneralMessagesFrom(worstEvaluation))
    }

    private fun worstEvaluation(derived: Map<TumorStage, Evaluation>): Evaluation {
        return derived.values.minBy(Evaluation::result)
    }

    private fun allSpecificMessagesFrom(derived: Map<TumorStage, Evaluation>, worstEvaluation: Evaluation): String {
        val aggregatedMessage = listOf(
            worstEvaluation.passSpecificMessages, worstEvaluation.warnSpecificMessages,
            worstEvaluation.failSpecificMessages, worstEvaluation.undeterminedSpecificMessages
        )
            .flatten()
            .joinToString(". ")
        return String.format("%s. Tumor stage has been implied to be %s", aggregatedMessage, stagesFrom(derived.keys))
    }

    private fun allGeneralMessagesFrom(worstEvaluation: Evaluation): String {
        return listOf(
            worstEvaluation.passGeneralMessages, worstEvaluation.warnGeneralMessages, worstEvaluation.failGeneralMessages,
            worstEvaluation.undeterminedGeneralMessages
        )
            .flatten()
            .joinToString(". ")
    }

    private fun stagesFrom(stages: Collection<TumorStage>): String {
        return stages.sorted().joinToString(" or ") { it.toString() }
    }
}