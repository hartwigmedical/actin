package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class Not(private val function: EvaluationFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluation: Evaluation = function.evaluate(record)

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
            passSpecificMessages = evaluation.failSpecificMessages,
            passGeneralMessages = evaluation.failGeneralMessages,
            failSpecificMessages = evaluation.passSpecificMessages,
            failGeneralMessages = evaluation.passGeneralMessages,
        )
    }
}