package com.hartwig.actin.algo.evaluation

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.EvaluationTestFactory

object TestEvaluationFunctionFactory {
    fun pass(): EvaluationFunction {
        return fixedEvaluation(EvaluationResult.PASS)
    }

    fun warn(): EvaluationFunction {
        return fixedEvaluation(EvaluationResult.WARN)
    }

    fun fail(): EvaluationFunction {
        return fixedEvaluation(EvaluationResult.FAIL)
    }

    fun undetermined(): EvaluationFunction {
        return fixedEvaluation(EvaluationResult.UNDETERMINED)
    }

    fun notEvaluated(): EvaluationFunction {
        return fixedEvaluation(EvaluationResult.NOT_EVALUATED)
    }

    private fun fixedEvaluation(output: EvaluationResult): EvaluationFunction {
        return object : EvaluationFunction {
            override fun evaluate(record: PatientRecord): Evaluation {
                return EvaluationTestFactory.withResult(output)
            }
        }
    }
}