package com.hartwig.actin.algo.evaluation.composite

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.EvaluationTestFactory
import org.junit.Test

class FallbackTest {
    @Test
    fun canEvaluate() {

        val pass = Fallback(evaluationFunction(EvaluationResult.PASS), evaluationFunction(EvaluationResult.FAIL))
        assertEvaluation(EvaluationResult.PASS, pass.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
        val fallback = Fallback(evaluationFunction(EvaluationResult.UNDETERMINED), evaluationFunction(EvaluationResult.FAIL))
        assertEvaluation(EvaluationResult.FAIL, fallback.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    private fun evaluationFunction(result: EvaluationResult): EvaluationFunction {
        return object : EvaluationFunction {
            override fun evaluate(record: PatientRecord): Evaluation {
                return EvaluationTestFactory.withResult(result)
            }
        }
    }
}