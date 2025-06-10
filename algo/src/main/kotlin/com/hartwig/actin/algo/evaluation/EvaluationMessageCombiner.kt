package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationMessage

private class EmptyMessage : EvaluationMessage {
    override fun combineBy(): String {
        throw UnsupportedOperationException()
    }

    override fun combine(other: EvaluationMessage): EvaluationMessage {
        return other
    }
}

object EvaluationMessageCombiner {

    fun combineMessages(evaluation: Evaluation): Evaluation {
        return evaluation.copy(
            passMessages = combineMessages(evaluation.passMessages),
            warnMessages = combineMessages(evaluation.warnMessages),
            undeterminedMessages = combineMessages(evaluation.undeterminedMessages),
            failMessages = combineMessages(evaluation.failMessages)
        )
    }

    private fun combineMessages(evaluations: Set<EvaluationMessage>): Set<EvaluationMessage> {
        return evaluations.groupBy { it.combineBy() }
            .mapValues { it.value.fold(EmptyMessage() as EvaluationMessage) { i, r -> i.combine(r) } }.values.toSet()
    }
}