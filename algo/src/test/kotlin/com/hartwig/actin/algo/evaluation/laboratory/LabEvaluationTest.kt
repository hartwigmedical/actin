package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.datamodel.LabValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LabEvaluationTest {
    @Test
    fun canEvaluateVersusMinLLN() {
        assertThat(LabEvaluation.evaluateVersusMinLLN(LabTestFactory.create(), 2.0)).isEqualTo(EvaluationResult.UNDETERMINED)
        val labValue = LabTestFactory.create(value = 80.0, refLimitLow = 30.0)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue, 2.0)).isEqualTo(EvaluationResult.PASS)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue.copy(value = 50.0), 2.0))
            .isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun canEvaluateVersusMinULN() {
        assertThat(LabEvaluation.evaluateVersusMinULN(LabTestFactory.create(), 2.0)).isEqualTo(EvaluationResult.UNDETERMINED)
        val labValue = LabTestFactory.create(value = 40.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue, 0.5)).isEqualTo(EvaluationResult.PASS)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue.copy(value = 20.0), 0.5)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun canEvaluateVersusMaxULN() {
        assertThat(LabEvaluation.evaluateVersusMaxULN(LabTestFactory.create(), 2.0)).isEqualTo(EvaluationResult.UNDETERMINED)
        val labValue = LabTestFactory.create(value = 70.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue, 2.0)).isEqualTo(EvaluationResult.PASS)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue.copy(value = 120.0), 2.0)).isEqualTo(EvaluationResult.FAIL)
    }

    @Test
    fun canUseOverridesForRefLimitUp() {
        val firstCode = LabEvaluation.REF_LIMIT_UP_OVERRIDES.keys.iterator().next()
        val overrideRefLimitUp: Double = LabEvaluation.REF_LIMIT_UP_OVERRIDES[firstCode]!!
        val value: LabValue = LabTestFactory.create(value = 1.8 * overrideRefLimitUp).copy(code = firstCode)
        assertThat(LabEvaluation.evaluateVersusMaxULN(value, 2.0)).isEqualTo(EvaluationResult.PASS)
    }
}