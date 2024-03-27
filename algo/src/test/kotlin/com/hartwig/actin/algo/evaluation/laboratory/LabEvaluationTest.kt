package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.datamodel.LabValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LabEvaluationTest {
    @Test
    fun `Should evaluate correctly versus minimum LLN without margin`() {
        assertThat(LabEvaluation.evaluateVersusMinLLN(LabTestFactory.create(), 2.0, false)).isEqualTo(EvaluationResult.UNDETERMINED)
        val labValue = LabTestFactory.create(value = 80.0, refLimitLow = 30.0)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue, 2.0, false)).isEqualTo(EvaluationResult.PASS)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue.copy(value = 50.0), 2.0, false))
            .isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should evaluate correctly versus minimum LLN with margin`() {
        assertThat(LabEvaluation.evaluateVersusMinLLN(LabTestFactory.create(), 2.0, true)).isEqualTo(EvaluationResult.UNDETERMINED)
        val labValue = LabTestFactory.create(value = 57.5, refLimitLow = 30.0)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue, 2.0, true)).isEqualTo(EvaluationResult.PASS)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue.copy(value = 50.0), 2.0, true))
            .isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should evaluate correctly versus minimum ULN without margin`() {
        assertThat(LabEvaluation.evaluateVersusMinULN(LabTestFactory.create(), 2.0, false)).isEqualTo(EvaluationResult.UNDETERMINED)
        val labValue = LabTestFactory.create(value = 55.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue, 1.0, false)).isEqualTo(EvaluationResult.PASS)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue.copy(value = 40.0), 1.0, false)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should evaluate correctly versus minimum ULN with margin`() {
        assertThat(LabEvaluation.evaluateVersusMinULN(LabTestFactory.create(), 2.0, true)).isEqualTo(EvaluationResult.UNDETERMINED)
        val labValue = LabTestFactory.create(value = 47.5, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue, 1.0, true)).isEqualTo(EvaluationResult.PASS)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue.copy(value = 40.0), 1.0, true)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should evaluate correctly versus maximum ULN without margin`() {
        assertThat(LabEvaluation.evaluateVersusMaxULN(LabTestFactory.create(), 2.0, false)).isEqualTo(EvaluationResult.UNDETERMINED)
        val labValue = LabTestFactory.create(value = 70.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue, 2.0, false)).isEqualTo(EvaluationResult.PASS)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue.copy(value = 120.0), 2.0, false)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun `Should evaluate correctly versus maximum ULN with margin`() {
        assertThat(LabEvaluation.evaluateVersusMaxULN(LabTestFactory.create(), 2.0, true)).isEqualTo(EvaluationResult.UNDETERMINED)
        val labValue = LabTestFactory.create(value = 105.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue, 2.0, true)).isEqualTo(EvaluationResult.PASS)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue.copy(value = 120.0), 2.0, true)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun canUseOverridesForRefLimitUp() {
        val firstCode = LabEvaluation.REF_LIMIT_UP_OVERRIDES.keys.iterator().next()
        val overrideRefLimitUp: Double = LabEvaluation.REF_LIMIT_UP_OVERRIDES[firstCode]!!
        val value: LabValue = LabTestFactory.create(value = 1.8 * overrideRefLimitUp).copy(code = firstCode)
        assertThat(LabEvaluation.evaluateVersusMaxULN(value, 2.0, false)).isEqualTo(EvaluationResult.PASS)
    }
}