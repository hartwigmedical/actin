package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class WarnIf(private val function: EvaluationFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluation: Evaluation = function.evaluate(record)
        if (evaluation.result == EvaluationResult.PASS) {
            return Evaluation(
                result = EvaluationResult.WARN,
                recoverable = evaluation.recoverable,
                inclusionMolecularEvents = emptySet(),
                exclusionMolecularEvents = emptySet(),
                warnSpecificMessages = evaluation.passSpecificMessages,
                warnGeneralMessages = evaluation.passGeneralMessages,
                isMissingGenesForSufficientEvaluation = evaluation.isMissingGenesForSufficientEvaluation
            )
        } else if (evaluation.result == EvaluationResult.WARN) {
            return evaluation.copy(
                inclusionMolecularEvents = emptySet(),
                exclusionMolecularEvents = emptySet(),
                isMissingGenesForSufficientEvaluation = evaluation.isMissingGenesForSufficientEvaluation
            )
        }

        return Evaluation(
            result = EvaluationResult.PASS,
            recoverable = evaluation.recoverable,
            inclusionMolecularEvents = emptySet(),
            exclusionMolecularEvents = emptySet(),
            passSpecificMessages = (
                    evaluation.passSpecificMessages + evaluation.warnSpecificMessages + evaluation.undeterminedSpecificMessages
                            + evaluation.failSpecificMessages
                    ),
            passGeneralMessages = (
                    evaluation.passGeneralMessages + evaluation.warnGeneralMessages + evaluation.undeterminedGeneralMessages
                            + evaluation.failGeneralMessages
                    ),
            isMissingGenesForSufficientEvaluation = evaluation.isMissingGenesForSufficientEvaluation
        )
    }
}