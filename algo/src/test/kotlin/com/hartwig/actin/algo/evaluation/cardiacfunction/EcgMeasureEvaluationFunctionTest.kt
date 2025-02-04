package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withEcg
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withEcgs
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withValueAndUnit
import com.hartwig.actin.algo.evaluation.cardiacfunction.EcgMeasureEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val THRESHOLD = 450.0

class EcgMeasureEvaluationFunctionTest {

    @Test
    fun `Should evaluate to recoverable undetermined when no ECG present`() {
        val evaluation = withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withEcg(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should evaluate to undetermined when unit is wrong`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withValueAndUnit(400, "wrong unit"))
        )
    }

    @Test
    fun `Should pass when value below max threshold`() {
        val evaluation = assertResultForCriteriaAndValueIgnoringNoise(ThresholdCriteria.MAXIMUM, 300, EvaluationResult.PASS)
        assertThat(evaluation.passMessages).containsExactly("QTCF of 300 ms does not exceed max threshold of 450.0")
    }

    @Test
    fun `Should pass when value equals max threshold`() {
        val evaluation = assertResultForCriteriaAndValueIgnoringNoise(ThresholdCriteria.MAXIMUM, 450, EvaluationResult.PASS)
        assertThat(evaluation.passMessages).containsExactly("QTCF of 450 ms does not exceed max threshold of 450.0")
    }

    @Test
    fun `Should fail when value above max threshold`() {
        val evaluation = assertResultForCriteriaAndValueIgnoringNoise(ThresholdCriteria.MAXIMUM, 500, EvaluationResult.FAIL)
        assertThat(evaluation.failMessages).containsExactly("QTCF of 500 ms is above or equal to max threshold of 450.0")
    }

    @Test
    fun `Should pass when value above min threshold`() {
        val evaluation = assertResultForCriteriaAndValueIgnoringNoise(ThresholdCriteria.MINIMUM, 500, EvaluationResult.PASS)
        assertThat(evaluation.passMessages).containsExactly("QTCF of 500 ms exceeds min threshold of 450.0")
    }

    @Test
    fun `Should pass when value equals min threshold`() {
        val evaluation = assertResultForCriteriaAndValueIgnoringNoise(ThresholdCriteria.MINIMUM, 450, EvaluationResult.PASS)
        assertThat(evaluation.passMessages).containsExactly("QTCF of 450 ms exceeds min threshold of 450.0")
    }

    @Test
    fun `Should fail when value below min threshold`() {
        val evaluation = assertResultForCriteriaAndValueIgnoringNoise(ThresholdCriteria.MINIMUM, 300, EvaluationResult.FAIL)
        assertThat(evaluation.failMessages).containsExactly("QTCF of 300 ms is below or equal to min threshold of 450.0")
    }

    @Test
    fun `Should return undetermined when multiple evaluations are produced with some unknown dates`() {
        val ecgs = listOf(THRESHOLD / 2, THRESHOLD * 2).map { Ecg("test", EcgMeasure(it.toInt(), EcgUnit.MILLISECONDS.symbol()), null) }
        val evaluation = withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withEcgs(ecgs))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }

    private fun assertResultForCriteriaAndValueIgnoringNoise(
        thresholdCriteria: ThresholdCriteria, value: Int, expectedResult: EvaluationResult
    ): Evaluation {
        val patient = withValueAndUnit(value)
        val measurement = patient.ecgs.single()
        val irrelevant = Ecg(null, null, EcgMeasure(1, "irrelevant"))
        val wrongUnit = Ecg(null, EcgMeasure(1, "incorrect"), null)
        val function = withThresholdCriteria(thresholdCriteria)

        val evaluations = listOf(
            patient,
            withEcgs(listOf(measurement, irrelevant, wrongUnit)),
            withEcgs(
                listOf(
                    measurement.copy(year = 2025, month = 1),
                    measurement.copy(year = 2024, month = 12, qtcfMeasure = measurement.qtcfMeasure?.copy(value = value / 2)),
                    measurement.copy(year = 2024, month = 11, qtcfMeasure = measurement.qtcfMeasure?.copy(value = value * 2))
                )
            )
        ).map { function.evaluate(it) }

        evaluations.forEach { assertEvaluation(expectedResult, it) }
        return evaluations.first()
    }

    private fun withThresholdCriteria(thresholdCriteria: ThresholdCriteria): EcgMeasureEvaluationFunction {
        return EcgMeasureEvaluationFunction(
            EcgMeasureName.QTCF,
            THRESHOLD,
            EcgUnit.MILLISECONDS,
            Ecg::qtcfMeasure,
            thresholdCriteria
        )
    }
}