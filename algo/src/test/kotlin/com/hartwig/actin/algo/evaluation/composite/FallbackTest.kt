package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.EvaluationTestFactory
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import org.junit.Test

class FallbackTest {
    @Test
    fun canEvaluate() {

        val pass = Fallback(evaluationFunction(EvaluationResult.PASS), evaluationFunction(EvaluationResult.FAIL))
        assertEvaluation(EvaluationResult.PASS, pass.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
        val fallback = Fallback(evaluationFunction(EvaluationResult.UNDETERMINED), evaluationFunction(EvaluationResult.FAIL))
        assertEvaluation(EvaluationResult.FAIL, fallback.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }

    private fun evaluationFunction(result: EvaluationResult): EvaluationFunction {
        return object : EvaluationFunction {
            override fun evaluate(record: PatientRecord): Evaluation {
                return EvaluationTestFactory.withResult(result)
            }
        }
    }
}