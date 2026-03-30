package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class Not(private val function: EvaluationFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val childEvaluation = function.evaluate(record)

        return when (childEvaluation.result) {
            EvaluationResult.PASS -> swapEvaluationMessagesAndMolecularEventsWithResult(childEvaluation, EvaluationResult.FAIL)
            EvaluationResult.FAIL -> swapEvaluationMessagesAndMolecularEventsWithResult(childEvaluation, EvaluationResult.PASS)
            else -> {
                childEvaluation.copy(
                    inclusionMolecularEvents = childEvaluation.exclusionMolecularEvents,
                    exclusionMolecularEvents = childEvaluation.inclusionMolecularEvents
                )
            }
        }.copy(childEvaluations = listOf(childEvaluation))
    }

    private fun swapEvaluationMessagesAndMolecularEventsWithResult(evaluation: Evaluation, negatedResult: EvaluationResult): Evaluation {
        return evaluation.copy(
            result = negatedResult,
            inclusionMolecularEvents = evaluation.exclusionMolecularEvents,
            exclusionMolecularEvents = evaluation.inclusionMolecularEvents,
            passMessages = evaluation.failMessages,
            failMessages = evaluation.passMessages,
        )
    }
}