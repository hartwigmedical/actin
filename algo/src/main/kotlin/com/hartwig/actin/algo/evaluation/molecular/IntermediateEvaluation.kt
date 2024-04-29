package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory

class IntermediateEvaluation(
    val result: EvaluationResult,
    val specificMessage: String,
    val generalMessage: String,
    val event: String? = null,
) {
    companion object {
        fun aggregateToEvaluation(result: EvaluationResult, intermediateEvaluations: List<IntermediateEvaluation>): List<Evaluation> {

            return intermediateEvaluations.filter { it.result == result }
                .groupBy { Pair(it.specificMessage, it.generalMessage) }
                .map { (messages, evaluations) ->
                    val events = evaluations.mapNotNull { it.event }.joinToString(",")
                    val aggregateSpecificMessage = "${messages.first} $events"
                    val aggregateGeneralMessage = "${messages.second} $events"

                    when (result) {
                        EvaluationResult.PASS -> {
                            EvaluationFactory.pass(aggregateSpecificMessage, aggregateGeneralMessage)
                        }

                        EvaluationResult.WARN -> {
                            EvaluationFactory.warn(aggregateSpecificMessage, aggregateGeneralMessage)
                        }

                        EvaluationResult.UNDETERMINED -> {
                            EvaluationFactory.undetermined(aggregateSpecificMessage, aggregateGeneralMessage)
                        }

                        EvaluationResult.FAIL -> {
                            EvaluationFactory.fail(aggregateSpecificMessage, aggregateGeneralMessage)
                        }

                        else -> throw IllegalStateException("Unexpected evaluation result type $result")
                    }
                }
        }
    }
}
