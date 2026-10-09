package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withEcg
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withHeartMeasurements
import com.hartwig.actin.algo.evaluation.cardiacfunction.CardiacFunctionTestFactory.withValueAndUnit
import com.hartwig.actin.algo.evaluation.cardiacfunction.HeartMeasurementEvaluationFunction.ThresholdCriteria
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.HeartMeasurement
import com.hartwig.actin.datamodel.clinical.HeartMeasurementType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val THRESHOLD = 450.0

class HeartMeasurementEvaluationFunctionTest {

    @Test
    fun `Should evaluate to recoverable undetermined when no ECG present`() {
        val evaluation = withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withEcg(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation, "No QTCF interval known")
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should evaluate to undetermined when unit is wrong`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withValueAndUnit(400.0, "wrong unit")),
            "QTCF measure in wrong unit instead of required ms"
        )
    }

    @Test
    fun `Should pass when value below max threshold`() {
        assertResultForCriteriaAndValueIgnoringNoise(
            ThresholdCriteria.MAXIMUM,
            300.0,
            EvaluationResult.PASS,
            "QTCF of 300.0 ms does not exceed max threshold of 450.0"
        )
    }

    @Test
    fun `Should pass when value equals max threshold`() {
        assertResultForCriteriaAndValueIgnoringNoise(
            ThresholdCriteria.MAXIMUM,
            450.0,
            EvaluationResult.PASS,
            "QTCF of 450.0 ms does not exceed max threshold of 450.0"
        )
    }

    @Test
    fun `Should fail when value above max threshold`() {
        assertResultForCriteriaAndValueIgnoringNoise(
            ThresholdCriteria.MAXIMUM,
            500.0,
            EvaluationResult.FAIL,
            "QTCF of 500.0 ms is above or equal to max threshold of 450.0"
        )
    }

    @Test
    fun `Should pass when value above min threshold`() {
        assertResultForCriteriaAndValueIgnoringNoise(
            ThresholdCriteria.MINIMUM,
            500.0,
            EvaluationResult.PASS,
            "QTCF of 500.0 ms exceeds min threshold of 450.0"
        )
    }

    @Test
    fun `Should pass when value equals min threshold`() {
        assertResultForCriteriaAndValueIgnoringNoise(
            ThresholdCriteria.MINIMUM,
            450.0,
            EvaluationResult.PASS,
            "QTCF of 450.0 ms exceeds min threshold of 450.0"
        )
    }

    @Test
    fun `Should fail when value below min threshold`() {
        assertResultForCriteriaAndValueIgnoringNoise(
            ThresholdCriteria.MINIMUM,
            300.0,
            EvaluationResult.FAIL,
            "QTCF of 300.0 ms is below or equal to min threshold of 450.0"
        )
    }

    @Test
    fun `Should return undetermined when multiple evaluations are produced with some unknown dates`() {
        val ecgs = listOf(THRESHOLD / 2, THRESHOLD * 2).map {
            HeartMeasurement("test", emptySet(), it, EcgUnit.MILLISECONDS.symbol(), HeartMeasurementType.OTHER_ECG)
        }
        val evaluation = withThresholdCriteria(ThresholdCriteria.MAXIMUM).evaluate(withHeartMeasurements(ecgs))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation, "No QTCF interval known")
    }

    private fun assertResultForCriteriaAndValueIgnoringNoise(
        thresholdCriteria: ThresholdCriteria, value: Double, expectedResult: EvaluationResult, expectedMessage: String
    ) {
        val patient = withValueAndUnit(value)
        val measurement = patient.heartMeasurements.single()
        val irrelevant = HeartMeasurement(null, emptySet(), 1.0, "irrelevant", HeartMeasurementType.JTC)
        val wrongUnit = HeartMeasurement(null, emptySet(), 1.0, "incorrect", HeartMeasurementType.QTCF)
        val function = withThresholdCriteria(thresholdCriteria)

        val evaluations = listOf(
            patient,
            withHeartMeasurements(listOf(measurement, irrelevant, wrongUnit)),
            withHeartMeasurements(
                listOf(
                    measurement.copy(year = 2025, month = 1),
                    measurement.copy(year = 2024, month = 12, value = measurement.value?.div(2)),
                    measurement.copy(year = 2024, month = 11, value = measurement.value?.times(2))
                )
            )
        ).map { function.evaluate(it) }

        evaluations.forEach { assertEvaluation(expectedResult, it, expectedMessage) }
    }

    private fun withThresholdCriteria(thresholdCriteria: ThresholdCriteria): HeartMeasurementEvaluationFunction {
        return HeartMeasurementEvaluationFunction(
            HeartMeasurementType.QTCF,
            THRESHOLD,
            EcgUnit.MILLISECONDS,
            thresholdCriteria
        )
    }
}