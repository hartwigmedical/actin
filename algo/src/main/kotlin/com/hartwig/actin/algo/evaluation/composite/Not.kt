package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class Not(private val function: EvaluationFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluation = function.evaluate(record)

        return when (evaluation.result) {
            EvaluationResult.PASS -> swapEvaluationMessagesAndMolecularEventsWithResult(evaluation, EvaluationResult.FAIL)
            EvaluationResult.FAIL -> swapEvaluationMessagesAndMolecularEventsWithResult(evaluation, EvaluationResult.PASS)
            EvaluationResult.NOT_EVALUATED -> swapEvaluationMessagesAndMolecularEventsWithResult(evaluation, EvaluationResult.NOT_EVALUATED)
            else -> {
                evaluation.copy(
                    inclusionMolecularEvents = evaluation.exclusionMolecularEvents,
                    exclusionMolecularEvents = evaluation.inclusionMolecularEvents
                )
            }
        }
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