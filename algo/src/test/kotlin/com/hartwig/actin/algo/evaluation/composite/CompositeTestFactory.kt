package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction

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
        val builder = ImmutableEvaluation.builder()
            .result(result)
            .recoverable(recoverable)
            .addPassSpecificMessages("pass specific $index")
            .addPassGeneralMessages("pass general $index")
            .addWarnSpecificMessages("warn specific $index")
            .addWarnGeneralMessages("warn general $index")
            .addUndeterminedSpecificMessages("undetermined specific $index")
            .addUndeterminedGeneralMessages("undetermined general $index")
            .addFailSpecificMessages("fail specific $index")
            .addFailGeneralMessages("fail general $index")
        if (includeMolecular) {
            builder.addInclusionMolecularEvents("inclusion event $index")
            builder.addExclusionMolecularEvents("exclusion event $index")
        }
        val evaluation = builder.build()
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