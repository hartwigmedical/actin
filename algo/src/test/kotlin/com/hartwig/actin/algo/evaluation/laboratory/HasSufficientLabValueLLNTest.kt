package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.AbstractBooleanAssert
import org.assertj.core.api.AbstractComparableAssert
import org.assertj.core.api.Assertions
import org.junit.Test

class HasSufficientLabValueLLNTest {
    private val function = HasSufficientLabValueLLN(2.0)
    private val record = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should pass when lab value is above requested fold of LLN`() {
        assertUndetermined(80.0, 35.0, EvaluationResult.PASS)
    }

    @Test
    fun `Should evaluate to recoverable undetermined if lab value is under requested fold of LLN but within margin of error`() {
        assertUndetermined(67.0, 35.0, EvaluationResult.UNDETERMINED)
        assertRecoverable(67.0, 35.0)
    }

    @Test
    fun `Should evaluate to undetermined if comparison to LLN cannot be made due to missing reference limit`() {
        assertUndetermined(80.0, null, EvaluationResult.UNDETERMINED)
        assertRecoverable(80.0, null)
    }

    @Test
    fun `Should fail if lab value is under requested fold of LLN and outside margin of error`() {
        assertUndetermined(50.0, 35.0, EvaluationResult.FAIL)
        assertRecoverable(50.0, 35.0)
    }

    private fun assertUndetermined(
        labValue: Double, referenceLimitLow: Double?, result: EvaluationResult
    ): AbstractComparableAssert<*, *> {
        val evaluation =
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = labValue, refLimitLow = referenceLimitLow))
        return Assertions.assertThat(evaluation.result).isEqualTo(result)
    }

    private fun assertRecoverable(
        labValue: Double, referenceLimitLow: Double?
    ): AbstractBooleanAssert<*> {
        val evaluation =
            function.evaluate(record, LabMeasurement.CREATININE, LabTestFactory.create(value = labValue, refLimitLow = referenceLimitLow))
        return Assertions.assertThat(evaluation.recoverable).isTrue()
    }
}