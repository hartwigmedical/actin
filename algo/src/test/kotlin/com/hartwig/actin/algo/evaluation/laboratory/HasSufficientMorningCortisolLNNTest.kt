package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import org.assertj.core.api.AbstractBooleanAssert
import org.assertj.core.api.AbstractComparableAssert
import org.assertj.core.api.Assertions
import org.junit.Test

class HasSufficientMorningCortisolLNNTest {
    private val function = HasSufficientMorningCortisol(1.0)
    private val record = TestPatientFactory.createMinimalTestWGSPatientRecord()

    @Test
    fun `Should pass when cortisol is above requested fold of LLN`() {
        assertUndetermined(300.0, 200.0, EvaluationResult.PASS)
    }

    @Test
    fun `Should evaluate to recoverable undetermined if cortisol is under requested fold of LLN but within margin of error`() {
        assertUndetermined(199.0, 200.0, EvaluationResult.UNDETERMINED)
        assertRecoverable(199.0, 200.0)
    }

    @Test
    fun `Should evaluate to undetermined if comparison to LLN cannot be made due to missing reference limit`() {
        assertUndetermined(80.0, null, EvaluationResult.UNDETERMINED)
        assertRecoverable(80.0, null)
    }

    @Test
    fun `Should evaluate to recoverable undetermined if cortisol is under requested fold of LLN and outside margin of error`() {
        assertUndetermined(100.0, 200.0, EvaluationResult.UNDETERMINED)
        assertRecoverable(100.0, 200.0)
    }

    private fun assertUndetermined(
        labValue: Double, referenceLimitLow: Double?, result: EvaluationResult
    ): AbstractComparableAssert<*, *> {
        val evaluation =
            function.evaluate(record, LabMeasurement.CORTISOL, LabTestFactory.create(value = labValue, refLimitLow = referenceLimitLow))
        return Assertions.assertThat(evaluation.result).isEqualTo(result)
    }

    private fun assertRecoverable(
        labValue: Double, referenceLimitLow: Double?
    ): AbstractBooleanAssert<*> {
        val evaluation =
            function.evaluate(record, LabMeasurement.CORTISOL, LabTestFactory.create(value = labValue, refLimitLow = referenceLimitLow))
        return Assertions.assertThat(evaluation.recoverable).isTrue()
    }
}