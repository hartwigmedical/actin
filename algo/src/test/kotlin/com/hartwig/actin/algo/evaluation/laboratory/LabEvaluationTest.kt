package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.datamodel.LabValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LabEvaluationTest {

    @Test
    fun `Should return undetermined for minimum LLN evaluation when reference limit not provided`() {
        assertThat(LabEvaluation.evaluateVersusMinLLN(LabTestFactory.create(), 2.0, false)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(LabEvaluation.evaluateVersusMinLLN(LabTestFactory.create(), 2.0, true)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @Test
    fun `Should pass if value is above fold of minimum LLN`() {
        val labValue = LabTestFactory.create(value = 80.0, refLimitLow = 30.0)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue, 2.0, false)).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should fail if value is under fold of minimum LLN and outside margin of error`() {
        val labValue = LabTestFactory.create(value = 80.0, refLimitLow = 30.0)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue.copy(value = 50.0), 2.0, false))
            .isEqualTo(EvaluationResult.FAIL)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue.copy(value = 50.0), 2.0, true))
            .isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should warn if value is under fold of minimum LLN but inside margin of error`() {
        val labValue = LabTestFactory.create(value = 57.5, refLimitLow = 30.0)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue, 2.0, true)).isEqualTo(EvaluationResult.WARN)
    }

    @Test
    fun `Should return undetermined for minimum ULN evaluation when reference limit not provided`() {
        assertThat(LabEvaluation.evaluateVersusMinULN(LabTestFactory.create(), 2.0, false)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(LabEvaluation.evaluateVersusMinULN(LabTestFactory.create(), 2.0, true)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @Test
    fun `Should pass if value is above fold of minimum ULN`() {
        val labValue = LabTestFactory.create(value = 55.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue, 1.0, false)).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should fail if value is under fold of minimum ULN and outside margin of error`() {
        val labValue = LabTestFactory.create(value = 55.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue.copy(value = 40.0), 1.0, false)).isEqualTo(EvaluationResult.FAIL)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue.copy(value = 40.0), 1.0, true)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should warn if value is under fold of minimum ULN but inside margin of error`() {
        val labValue = LabTestFactory.create(value = 47.5, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue, 1.0, true)).isEqualTo(EvaluationResult.WARN)
    }

    @Test
    fun `Should return undetermined for maximum ULN evaluation when reference limit not provided`() {
        assertThat(LabEvaluation.evaluateVersusMaxULN(LabTestFactory.create(), 2.0, false)).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(LabEvaluation.evaluateVersusMaxULN(LabTestFactory.create(), 2.0, true)).isEqualTo(EvaluationResult.UNDETERMINED)
    }

    @Test
    fun `Should pass if value is under fold of maximum ULN`() {
        val labValue = LabTestFactory.create(value = 70.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue, 2.0, false)).isEqualTo(EvaluationResult.PASS)
    }

    @Test
    fun `Should fail if value is above fold of maximum ULN and outside margin of error`() {
        val labValue = LabTestFactory.create(value = 70.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue.copy(value = 120.0), 2.0, false)).isEqualTo(EvaluationResult.FAIL)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue.copy(value = 120.0), 2.0, true)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should warn if value is above fold of maximum ULN but inside margin of error`() {
        val labValue = LabTestFactory.create(value = 105.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue, 2.0, true)).isEqualTo(EvaluationResult.WARN)
    }

    @Test
    fun `Should be able to use overrides for refLimitUp`() {
        val firstCode = LabEvaluation.REF_LIMIT_UP_OVERRIDES.keys.iterator().next()
        val overrideRefLimitUp: Double = LabEvaluation.REF_LIMIT_UP_OVERRIDES[firstCode]!!
        val value: LabValue = LabTestFactory.create(value = 1.8 * overrideRefLimitUp).copy(code = firstCode)
        assertThat(LabEvaluation.evaluateVersusMaxULN(value, 2.0, false)).isEqualTo(EvaluationResult.PASS)
    }
}