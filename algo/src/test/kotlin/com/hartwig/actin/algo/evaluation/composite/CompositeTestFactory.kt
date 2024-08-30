package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult

internal object CompositeTestFactory {

    private val DEFAULT_RESULT = EvaluationResult.PASS
    private const val DEFAULT_RECOVERABLE = false
    private const val DEFAULT_INCLUDE_MOLECULAR = false
    private const val DEFAULT_INDEX = 1

    fun create(result: EvaluationResult, includeMolecular: Boolean): EvaluationFunction {
        return create(result, DEFAULT_RECOVERABLE, includeMolecular, DEFAULT_INDEX)
    }

    fun create(recoverable: Boolean, index: Int): EvaluationFunction {
        return create(DEFAULT_RESULT, recoverable, DEFAULT_INCLUDE_MOLECULAR, index)
    }

    fun create(result: EvaluationResult, index: Int): EvaluationFunction {
        return create(result, DEFAULT_RECOVERABLE, DEFAULT_INCLUDE_MOLECULAR, index)
    }

    fun create(result: EvaluationResult, includeMolecular: Boolean, index: Int): EvaluationFunction {
        return create(result, DEFAULT_RECOVERABLE, includeMolecular, index)
    }

    private fun create(result: EvaluationResult, recoverable: Boolean, includeMolecular: Boolean, index: Int): EvaluationFunction {
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
            exclusionMolecularEvents = if (includeMolecular) setOf("exclusion event $index") else emptySet()
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