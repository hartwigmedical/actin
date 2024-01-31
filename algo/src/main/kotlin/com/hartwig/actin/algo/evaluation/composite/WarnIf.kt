package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class WarnIf(private val function: EvaluationFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluation: Evaluation = function.evaluate(record)
        if (evaluation.result == EvaluationResult.PASS) {
            return Evaluation(
                result = EvaluationResult.WARN,
                recoverable = evaluation.recoverable,
                inclusionMolecularEvents = evaluation.inclusionMolecularEvents,
                exclusionMolecularEvents = evaluation.exclusionMolecularEvents,
                warnSpecificMessages = evaluation.passSpecificMessages,
                warnGeneralMessages = evaluation.passGeneralMessages
            )
        } else if (evaluation.result == EvaluationResult.WARN) {
            return evaluation
        }
        val notImplementedMessages = if (evaluation.result == EvaluationResult.NOT_IMPLEMENTED) setOf("not implemented") else emptySet()

        return Evaluation(
            result = EvaluationResult.PASS,
            recoverable = evaluation.recoverable,
            inclusionMolecularEvents = evaluation.inclusionMolecularEvents,
            exclusionMolecularEvents = evaluation.exclusionMolecularEvents,
            passSpecificMessages = (
                    evaluation.passSpecificMessages + evaluation.warnSpecificMessages + evaluation.undeterminedSpecificMessages
                            + evaluation.failSpecificMessages + notImplementedMessages
                    ),
            passGeneralMessages = (
                    evaluation.passGeneralMessages + evaluation.warnGeneralMessages + evaluation.undeterminedGeneralMessages
                            + evaluation.failGeneralMessages + notImplementedMessages
                    )
        )
    }
}