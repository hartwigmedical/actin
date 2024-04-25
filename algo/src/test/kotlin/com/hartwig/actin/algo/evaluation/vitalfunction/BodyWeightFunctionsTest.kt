package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.evaluatePatientForMaximumBodyWeight
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.evaluatePatientForMinimumBodyWeight
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.selectMedianBodyWeightPerDay
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.weight
import com.hartwig.actin.clinical.datamodel.BodyWeight
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

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
            weight(referenceDateTime, 148.0, "pounds")
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
    fun `Should fail on median weight above max and outside margin of error`() {
        val weights = listOf(
            weight(referenceDateTime, 160.0),
            weight(referenceDateTime.plusDays(1), 175.0)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
    }

    @Test
    fun `Should evaluate to recoverable undetermined on median weight above max but inside margin of error`() {
        val weights = listOf(
            weight(referenceDateTime, 151.0),
            weight(referenceDateTime.plusDays(1), 152.0)
        )
        val evaluation = evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should pass on median weight below max`() {
        val weights = listOf(
            weight(referenceDateTime, 151.0),
            weight(referenceDateTime.plusDays(1), 148.0)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
    }

    @Test
    fun `Should pass on median weight below max and unit kilograms instead of kilogram`() {
        val weights = listOf(
            weight(referenceDateTime, 151.0, unit = "Kilograms"),
            weight(referenceDateTime.plusDays(1), 148.0, unit = "Kilograms")
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
    }

    @Test
    fun `Should pass on median weight equal to max`() {
        val weights = listOf(
            weight(referenceDateTime, 152.0),
            weight(referenceDateTime.plusDays(1), 148.0)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
    }

    @Test
    fun `Should fail on median weight below min and outside margin of error`() {
        val weights = listOf(
            weight(referenceDateTime, 35.0),
            weight(referenceDateTime.plusDays(1), 30.0)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimumValidDate)
        )
    }

    @Test
    fun `Should evaluate to recoverable undetermined on median weight below min but inside margin of error`() {
        val weights = listOf(
            weight(referenceDateTime, 38.0),
            weight(referenceDateTime.plusDays(1), 40.0)
        )
        val evaluation = evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimumValidDate)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should pass on median weight above min`() {
        val weights = listOf(
            weight(referenceDateTime, 38.0),
            weight(referenceDateTime.plusDays(1), 43.0)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimumValidDate)
        )
    }

    @Test
    fun `Should pass on median weight equal to min`() {
        val weights = listOf(
            weight(referenceDateTime, 39.0),
            weight(referenceDateTime.plusDays(1), 41.0)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimumValidDate)
        )
    }

    @Test
    fun `Should take most recent and max 5 entries`() {
        val weights = listOf(
            weight(referenceDateTime, 150.0),
            weight(referenceDateTime.plusDays(1), 150.0),
            weight(referenceDateTime.plusDays(2), 150.0),
            weight(referenceDateTime.plusDays(3), 150.0),
            weight(referenceDateTime.plusDays(4), 150.0),
            weight(referenceDateTime.plusDays(5), 280.0),
            weight(referenceDateTime.plusDays(6), 280.0)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimumValidDate)
        )
    }

    @Test
    fun `Should take only one and the correct median per day`() {
        val weights = listOf(
            weight(referenceDateTime, 125.0),
            weight(referenceDateTime, 175.0),
            weight(referenceDateTime.plusDays(1), 150.0)
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
            weight(referenceDateTime, 1250.0, "wrong"),
            weight(referenceDateTime, 125.0, "pounds")
        )
        assertThat(selectMedianBodyWeightPerDay(VitalFunctionTestFactory.withBodyWeights(weights), minimumValidDate)).isNull()
    }

    @Test
    fun `Should not take body weight measurements outside of date cutoff`() {
        val weights = listOf(
            weight(referenceDateTime.plusDays(1), 110.0),
            weight(referenceDateTime, 120.0),
            weight(referenceDateTime.minusDays(3), 130.0)
        )
        assertThat(
            selectMedianBodyWeightPerDay(VitalFunctionTestFactory.withBodyWeights(weights), minimumValidDate)
                ?.map(BodyWeight::value)
        )
            .isEqualTo(listOf(110.0, 120.0))
    }
}

