package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.junit.Test

class HasSufficientLabValueULNTest {

    @Test
    fun canEvaluate() {
        val function = HasSufficientLabValueULN(0.5)
        val record = TestPatientFactory.createMinimalTestPatientRecord()
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 120.0, refLimitUp = 200.0))
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 80.0))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 50.0, refLimitUp = 150.0))
        )
    }
}