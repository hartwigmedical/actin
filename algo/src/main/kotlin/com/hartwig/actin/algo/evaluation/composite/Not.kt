package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class Not(private val function: EvaluationFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluation: Evaluation = function.evaluate(record)

        return if (evaluation.result in listOf(EvaluationResult.PASS, EvaluationResult.FAIL, EvaluationResult.NOT_EVALUATED)) {
            swapEvaluationMessagesAndMolecularEventsWithResult(evaluation, evaluation.result)
        } else {
            evaluation.copy(
                inclusionMolecularEvents = evaluation.exclusionMolecularEvents,
                exclusionMolecularEvents = evaluation.inclusionMolecularEvents
            )
        }
    }

    private fun swapEvaluationMessagesAndMolecularEventsWithResult(evaluation: Evaluation, result: EvaluationResult): Evaluation {
        val negatedResult = when (result) {
            EvaluationResult.PASS -> EvaluationResult.FAIL
            EvaluationResult.FAIL -> EvaluationResult.PASS
            else -> result
        }

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