package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

internal object CompositeTestFactory {

    fun create(
        result: EvaluationResult = EvaluationResult.PASS,
        recoverable: Boolean = false,
        includeMolecular: Boolean = false,
        isMissingMolecularResultForEvaluation: Boolean = false,
        index: Int = 1
    ): EvaluationFunction {
        val evaluation = Evaluation(
            result = result,
            recoverable = recoverable,
            passMessages = setOf("pass $index"),
            warnMessages = setOf("warn $index"),
            undeterminedMessages = setOf("undetermined $index"),
            failMessages = setOf("fail $index"),
            inclusionMolecularEvents = if (includeMolecular) setOf("inclusion event $index") else emptySet(),
            exclusionMolecularEvents = if (includeMolecular) setOf("exclusion event $index") else emptySet(),
            isMissingMolecularResultForEvaluation = isMissingMolecularResultForEvaluation
        )
        return evaluationFunction { evaluation }
    }

    private fun evaluationFunction(function: (PatientRecord) -> Evaluation): EvaluationFunction {
        return object : EvaluationFunction {
            override fun evaluate(record: PatientRecord): Evaluation {
                return function(record)
            }
        }
    }
}