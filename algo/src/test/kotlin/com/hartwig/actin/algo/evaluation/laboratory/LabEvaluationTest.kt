package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LabEvaluationTest {

    private val refDate = LocalDate.of(2024, 7, 5)
    private val minValidDate = refDate.minusDays(90)
    private val measurement = LabMeasurement.CREATININE
    private val labValue = LabTestFactory.create(LabMeasurement.CREATININE, value = 100.0, refDate, refLimitUp = 100.0)

    @Test
    fun `Should return cannot be determined result for minimum LLN evaluation when reference limit not provided`() {
        assertThat(LabEvaluation.evaluateVersusMinLLN(LabTestFactory.create(), 2.0)).isEqualTo(CANNOT_BE_DETERMINED)
    }

    @Test
    fun `Should return within threshold result if value is above fold of minimum LLN`() {
        val labValue = LabTestFactory.create(value = 80.0, refLimitLow = 30.0)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue, 2.0)).isEqualTo(WITHIN_THRESHOLD)
    }

    @Test
    fun `Should return exceeds threshold and outside margin result if value is under fold of minimum LLN and outside margin of error`() {
        val labValue = LabTestFactory.create(value = 80.0, refLimitLow = 30.0)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue.copy(value = 50.0), 2.0)).isEqualTo(EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN)
    }

    @Test
    fun `Should return exceeds threshold but inside margin result if value is under fold of minimum LLN but inside margin of error`() {
        val labValue = LabTestFactory.create(value = 57.5, refLimitLow = 30.0)
        assertThat(LabEvaluation.evaluateVersusMinLLN(labValue, 2.0)).isEqualTo(EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN)
    }

    @Test
    fun `Should return cannot be determined result for minimum ULN evaluation when reference limit not provided`() {
        assertThat(LabEvaluation.evaluateVersusMinULN(LabTestFactory.create(), 2.0)).isEqualTo(CANNOT_BE_DETERMINED)
    }

    @Test
    fun `Should return within threshold result if value is above fold of minimum ULN`() {
        val labValue = LabTestFactory.create(value = 55.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue, 1.0)).isEqualTo(WITHIN_THRESHOLD)
    }

    @Test
    fun `Should return exceeds threshold and outside margin result if value is under fold of minimum ULN and outside margin of error`() {
        val labValue = LabTestFactory.create(value = 55.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue.copy(value = 40.0), 1.0)).isEqualTo(EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN)
    }

    @Test
    fun `Should return exceeds threshold but inside margin result if value is under fold of minimum ULN but inside margin of error`() {
        val labValue = LabTestFactory.create(value = 47.5, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMinULN(labValue, 1.0)).isEqualTo(EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN)
    }

    @Test
    fun `Should return cannot be determined result for maximum ULN evaluation when reference limit not provided`() {
        assertThat(LabEvaluation.evaluateVersusMaxULN(LabTestFactory.create(), 2.0)).isEqualTo(CANNOT_BE_DETERMINED)
    }

    @Test
    fun `Should return within threshold result if value is under fold of maximum ULN`() {
        val labValue = LabTestFactory.create(value = 70.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue, 2.0)).isEqualTo(WITHIN_THRESHOLD)
    }

    @Test
    fun `Should return exceeds threshold and outside margin result if value is above fold of maximum ULN and outside margin of error`() {
        val labValue = LabTestFactory.create(value = 70.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue.copy(value = 120.0), 2.0)).isEqualTo(EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN)
    }

    @Test
    fun `Should return exceeds threshold but inside margin result if value is above fold of maximum ULN but inside margin of error`() {
        val labValue = LabTestFactory.create(value = 105.0, refLimitUp = 50.0)
        assertThat(LabEvaluation.evaluateVersusMaxULN(labValue, 2.0)).isEqualTo(EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN)
    }

    @Test
    fun `Should be able to use overrides for refLimitUp`() {
        val firstLabMeasurement = LabEvaluation.REF_LIMIT_UP_OVERRIDES.keys.iterator().next()
        val overrideRefLimitUp: Double = LabEvaluation.REF_LIMIT_UP_OVERRIDES[firstLabMeasurement]!!
        val value: LabValue = LabTestFactory.create(value = 1.8 * overrideRefLimitUp).copy(measurement = firstLabMeasurement)
        assertThat(LabEvaluation.evaluateVersusMaxULN(value, 2.0)).isEqualTo(WITHIN_THRESHOLD)
    }

    // Tests for fun isValid
    @Test
    fun `Should return true if lab value is valid`() {
        assertThat(LabEvaluation.isValid(labValue, measurement, minValidDate)).isTrue()
    }

    @Test
    fun `Should return false if lab value is null`() {
        assertThat(LabEvaluation.isValid(null, measurement, minValidDate)).isFalse()
    }

    @Test
    fun `Should return false if lab value unit is different from default unit`() {
        val value = labValue.copy(unit = LabUnit.GRAMS)
        assertThat(LabEvaluation.isValid(value, measurement, minValidDate)).isFalse()
    }

    @Test
    fun `Should return false if lab value date is before minimal valid date`() {
        val value = labValue.copy(date = minValidDate.minusDays(1))
        assertThat(LabEvaluation.isValid(value, measurement, minValidDate)).isFalse()
    }

    //Tests for fun evaluateInvalidLabValue
    @Test
    fun `Should evaluate to undetermined with specific message if lab value is null`() {
        val evaluation = LabEvaluation.evaluateInvalidLabValue(measurement, null, minValidDate)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("No measurement found for ${measurement.display}")
    }

    @Test
    fun `Should evaluate to undetermined with specific message if lab value unit is different from default`() {
        val evaluation = LabEvaluation.evaluateInvalidLabValue(measurement, labValue.copy(unit = LabUnit.GRAMS), minValidDate)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly(
            "Unexpected unit specified for ${measurement.display}: ${LabUnit.GRAMS.display()}"
        )
    }

    @Test
    fun `Should evaluate to undetermined with specific message if lab value is too old`() {
        val evaluation = LabEvaluation.evaluateInvalidLabValue(measurement, labValue.copy(date = minValidDate.minusDays(1)), minValidDate)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly(
            "Most recent measurement too old for ${measurement.display}"
        )
    }
}