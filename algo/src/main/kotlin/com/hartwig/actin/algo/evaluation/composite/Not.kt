package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class Not(private val function: EvaluationFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluation: Evaluation = function.evaluate(record)
        val negatedResult: EvaluationResult
        val inclusionMolecularEvents: Set<String>
        val exclusionMolecularEvents: Set<String>
        val passSpecificMessages: Set<String>
        val passGeneralMessages: Set<String>
        val failSpecificMessages: Set<String>
        val failGeneralMessages: Set<String>
        when (evaluation.result) {
            EvaluationResult.PASS -> {
                negatedResult = EvaluationResult.FAIL
                inclusionMolecularEvents = evaluation.exclusionMolecularEvents
                exclusionMolecularEvents = evaluation.inclusionMolecularEvents
                passSpecificMessages = evaluation.failSpecificMessages
                passGeneralMessages = evaluation.failGeneralMessages
                failSpecificMessages = evaluation.passSpecificMessages
                failGeneralMessages = evaluation.passGeneralMessages
            }

            in listOf(EvaluationResult.FAIL, EvaluationResult.WARN, EvaluationResult.UNDETERMINED, EvaluationResult.NOT_EVALUATED) -> {
                negatedResult = EvaluationResult.PASS
                inclusionMolecularEvents = evaluation.exclusionMolecularEvents
                exclusionMolecularEvents = evaluation.inclusionMolecularEvents
                passSpecificMessages = evaluation.failSpecificMessages
                passGeneralMessages = evaluation.failGeneralMessages
                failSpecificMessages = evaluation.passSpecificMessages
                failGeneralMessages = evaluation.passGeneralMessages
            }

            else -> {
                negatedResult = evaluation.result
                inclusionMolecularEvents = evaluation.inclusionMolecularEvents
                exclusionMolecularEvents = evaluation.exclusionMolecularEvents
                passSpecificMessages = evaluation.passSpecificMessages
                passGeneralMessages = evaluation.passGeneralMessages
                failSpecificMessages = evaluation.failSpecificMessages
                failGeneralMessages = evaluation.failGeneralMessages
            }
        }
        return Evaluation(
            result = negatedResult,
            recoverable = evaluation.recoverable,
            inclusionMolecularEvents = inclusionMolecularEvents,
            exclusionMolecularEvents = exclusionMolecularEvents,
            passSpecificMessages = passSpecificMessages,
            passGeneralMessages = passGeneralMessages,
            warnSpecificMessages = evaluation.warnSpecificMessages,
            warnGeneralMessages = evaluation.warnGeneralMessages,
            undeterminedSpecificMessages = evaluation.undeterminedSpecificMessages,
            undeterminedGeneralMessages = evaluation.undeterminedGeneralMessages,
            failSpecificMessages = failSpecificMessages,
            failGeneralMessages = failGeneralMessages
        )
    }
}