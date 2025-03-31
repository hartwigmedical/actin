package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

class WarnIf(private val function: EvaluationFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val evaluation = function.evaluate(record)
        return when (evaluation.result) {
            EvaluationResult.PASS -> {
                Evaluation(
                    result = EvaluationResult.WARN,
                    recoverable = evaluation.recoverable,
                    warnMessages = evaluation.passMessages
                )
            }

            EvaluationResult.WARN -> evaluation.copy(inclusionMolecularEvents = emptySet(), exclusionMolecularEvents = emptySet())

            else -> {
                Evaluation(
                    result = EvaluationResult.PASS,
                    recoverable = evaluation.recoverable,
                    passMessages = (evaluation.passMessages + evaluation.warnMessages + evaluation.undeterminedMessages + evaluation.failMessages),
                    isMissingMolecularResultForEvaluation = evaluation.isMissingMolecularResultForEvaluation
                )
            }
        }
    }
}