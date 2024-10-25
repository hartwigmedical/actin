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
        isMissingGenes: Boolean = false,
        index: Int = 1
    ): EvaluationFunction {
        val evaluation = Evaluation(
            result = result,
            recoverable = recoverable,
            passSpecificMessages = setOf("pass specific $index"),
            passGeneralMessages = setOf("pass general $index"),
            warnSpecificMessages = setOf("warn specific $index"),
            warnGeneralMessages = setOf("warn general $index"),
            undeterminedSpecificMessages = setOf("undetermined specific $index"),
            undeterminedGeneralMessages = setOf("undetermined general $index"),
            failSpecificMessages = setOf("fail specific $index"),
            failGeneralMessages = setOf("fail general $index"),
            inclusionMolecularEvents = if (includeMolecular) setOf("inclusion event $index") else emptySet(),
            exclusionMolecularEvents = if (includeMolecular) setOf("exclusion event $index") else emptySet(),
            isMissingGenesForSufficientEvaluation = isMissingGenes
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