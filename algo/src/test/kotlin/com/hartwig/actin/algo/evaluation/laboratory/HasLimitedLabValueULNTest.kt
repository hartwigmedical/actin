package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasLimitedLabValueULNTest {

    private val function = HasLimitedLabValueULN(1.2)
    private val record = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should pass when lab value is under requested fold of ULN`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 110.0, refLimitUp = 100.0))
        )
    }

    @Test
    fun `Should evaluate to recoverable undetermined if lab value is above requested fold of ULN but within margin of error`() {
        val evaluation = function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 125.0, refLimitUp = 100.0))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue
    }

    @Test
    fun `Should evaluate to undetermined if comparison to ULN cannot be made due to missing reference limit`() {
        val evaluation = function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 100.0))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue
    }

    @Test
    fun `Should fail if lab value is above requested fold of ULN and outside margin of error`() {
        val actual = function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = 135.0, refLimitUp = 100.0))
        assertEvaluation(EvaluationResult.FAIL, actual)
        assertThat(actual.recoverable).isTrue
    }
}