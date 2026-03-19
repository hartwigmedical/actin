package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class WarnIf(private val function: EvaluationFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val childEvaluation = function.evaluate(record)
        return when (childEvaluation.result) {
            EvaluationResult.PASS -> {
                Evaluation(
                    result = EvaluationResult.WARN,
                    recoverable = childEvaluation.recoverable,
                    warnMessages = childEvaluation.passMessages,
                    childEvaluations = listOf(childEvaluation)
                )
            }

            EvaluationResult.WARN -> childEvaluation.copy(
                inclusionMolecularEvents = emptySet(),
                exclusionMolecularEvents = emptySet(),
                childEvaluations = listOf(childEvaluation)
            )

            else -> {
                Evaluation(
                    result = EvaluationResult.PASS,
                    recoverable = childEvaluation.recoverable,
                    passMessages = (childEvaluation.passMessages + childEvaluation.warnMessages + childEvaluation.undeterminedMessages + childEvaluation.failMessages),
                    isMissingMolecularResultForEvaluation = childEvaluation.isMissingMolecularResultForEvaluation,
                    childEvaluations = listOf(childEvaluation)
                )
            }
        }
    }
}