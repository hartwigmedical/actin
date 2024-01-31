package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.evaluatePatientForMaximumBodyWeight
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.evaluatePatientForMinimumBodyWeight
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.selectMedianBodyWeightPerDay
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.weight
import com.hartwig.actin.clinical.datamodel.BodyWeight
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class BodyWeightFunctionsTest {

    private val minimumValidDate = LocalDate.of(2023, 12, 1)
    private val referenceDateTime = minimumValidDate.atStartOfDay().plusDays(1)

    @Test
    fun `Should evaluate to undetermined on no body weight documented`() {
        val weights: List<BodyWeight> = emptyList()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimumValidDate)
        )
    }

    @Test
    fun `Should evaluate to undetermined when weight measurement invalid`() {
        val weights = listOf(
            weight(referenceDateTime, 148.0, false, "pounds")
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimumValidDate)
        )
    }


    @Test
    fun `Should fail on median weight above max`() {
        val weights = listOf(
            weight(referenceDateTime, 148.0, true),
            weight(referenceDateTime.plusDays(1), 155.0, true)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
    }

    @Test
    fun `Should pass on median weight below max`() {
        val weights = listOf(
            weight(referenceDateTime, 151.0, true),
            weight(referenceDateTime.plusDays(1), 148.0, true)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
    }

    @Test
    fun `Should pass on median weight equal to max`() {
        val weights = listOf(
            weight(referenceDateTime, 152.0, true),
            weight(referenceDateTime.plusDays(1), 148.0, true)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
    }

    @Test
    fun `Should fail on median weight below min`() {
        val weights = listOf(
            weight(referenceDateTime, 41.0, true),
            weight(referenceDateTime.plusDays(1), 38.0, true)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimumValidDate)
        )
    }

    @Test
    fun `Should pass on median weight above min`() {
        val weights = listOf(
            weight(referenceDateTime, 38.0, true),
            weight(referenceDateTime.plusDays(1), 43.0, true)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimumValidDate)
        )
    }

    @Test
    fun `Should pass on median weight equal to min`() {
        val weights = listOf(
            weight(referenceDateTime, 39.0, true),
            weight(referenceDateTime.plusDays(1), 41.0, true)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimumValidDate)
        )
    }

    @Test
    fun `Should take most recent and max 5 entries`() {
        val weights = listOf(
            weight(referenceDateTime, 150.0, true),
            weight(referenceDateTime.plusDays(1), 150.0, true),
            weight(referenceDateTime.plusDays(2), 150.0, true),
            weight(referenceDateTime.plusDays(3), 150.0, true),
            weight(referenceDateTime.plusDays(4), 150.0, true),
            weight(referenceDateTime.plusDays(5), 280.0, true),
            weight(referenceDateTime.plusDays(6), 280.0, true)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
    }

    @Test
    fun `Should take only one and the correct median per day`() {
        val weights = listOf(
            weight(referenceDateTime, 125.0, true),
            weight(referenceDateTime, 175.0, true),
            weight(referenceDateTime.plusDays(1), 150.0, true)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
    }

    // Test of fun selectMedianBodyWeightPerDay

    @Test
    fun `Should return null if no valid measurements present`() {
        val weights = listOf(
            weight(referenceDateTime, 1250.0, false),
            weight(referenceDateTime, 125.0, false, "pounds")
        )
        assertThat(selectMedianBodyWeightPerDay(VitalFunctionTestFactory.withBodyWeights(weights), minimumValidDate)).isNull()
    }

    @Test
    fun `Should not take body weight measurements outside of date cutoff`() {
        val weights = listOf(
            weight(referenceDateTime.plusDays(1), 110.0, true),
            weight(referenceDateTime, 120.0, true),
            weight(referenceDateTime.minusDays(3), 130.0, true)
        )
        assertThat(
            selectMedianBodyWeightPerDay(VitalFunctionTestFactory.withBodyWeights(weights), minimumValidDate)
                ?.map(BodyWeight::value)
        )
            .isEqualTo(listOf(110.0, 120.0))
    }
}

