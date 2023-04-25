package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class IsTreatedInHospitalTest {
    @Test
    fun canEvaluate() {
        val function = IsTreatedInHospital()
        EvaluationAssert.assertEvaluation(
            EvaluationResult.NOT_EVALUATED,
            function.evaluate(TestDataFactory.createMinimalTestPatientRecord())
        )
    }
}