package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.evaluatePatientForMaximumBodyWeight
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.evaluatePatientForMinimumBodyWeight
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.selectMedianBodyWeightPerDay
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class BodyWeightFunctionsTest {

    private val minimalValidDate = LocalDate.of(2023, 12, 1)
    private val referenceDateTime = minimalValidDate.atStartOfDay().plusDays(1)

    @Test
    fun `Should evaluate to undetermined on no body weight documented`() {
        val weights: List<BodyWeight> = emptyList()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimalValidDate)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimalValidDate)
        )
    }

    @Test
    fun `Should evaluate to undetermined when weight measurement invalid`() {
        val weights = listOf(
            weight().date(referenceDateTime).value(148.0).unit("pounds").valid(false).build()
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimalValidDate)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimalValidDate)
        )
    }


    @Test
    fun `Should fail on median weight above max`() {
        val weights = listOf(
            weight().date(referenceDateTime).value(148.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(1)).value(155.0).valid(true).build()
        )
        assertEvaluation(EvaluationResult.FAIL, evaluatePatientForMaximumBodyWeight(
            VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimalValidDate
        )
        )
    }

    @Test
    fun `Should pass on median weight below max`() {
        val weights = listOf(
            weight().date(referenceDateTime).value(151.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(1)).value(148.0).valid(true).build()
        )
        assertEvaluation(EvaluationResult.PASS, evaluatePatientForMaximumBodyWeight(
            VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimalValidDate
        )
        )
    }

    @Test
    fun `Should pass on median weight equal to max`() {
        val weights = listOf(
            weight().date(referenceDateTime).value(152.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(1)).value(148.0).valid(true).build()
        )
        assertEvaluation(EvaluationResult.PASS, evaluatePatientForMaximumBodyWeight(
            VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimalValidDate
        )
        )
    }

    @Test
    fun `Should fail on median weight below min`() {
        val weights = listOf(
            weight().date(referenceDateTime).value(41.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(1)).value(38.0).valid(true).build()
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimalValidDate)
        )
    }

    @Test
    fun `Should pass on median weight above min`() {
        val weights = listOf(
            weight().date(referenceDateTime).value(38.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(1)).value(43.0).valid(true).build()
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimalValidDate)
        )
    }

    @Test
    fun `Should pass on median weight equal to min`() {
        val weights = listOf(
            weight().date(referenceDateTime).value(39.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(1)).value(41.0).valid(true).build()
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0, minimalValidDate)
        )
    }

    @Test
    fun `Should take most recent and max 5 entries`() {
        val weights = listOf(
            weight().date(referenceDateTime).value(150.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(1)).value(150.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(2)).value(150.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(3)).value(150.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(4)).value(150.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(5)).value(280.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(6)).value(280.0).valid(true).build()
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimalValidDate)
        )
    }

    @Test
    fun `Should take only one and the correct median per day`() {
        val weights = listOf(
            weight().date(referenceDateTime).value(125.0).valid(true).build(),
            weight().date(referenceDateTime).value(175.0).valid(true).build(),
            weight().date(referenceDateTime.plusDays(1)).value(150.0).valid(true).build()
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0, minimalValidDate)
        )
    }

    // Test of fun selectMedianBodyWeightPerDay

    @Test
    fun `Should return null if no valid measurements present`() {
        val weights = listOf(
            weight().date(referenceDateTime).value(1250.0).valid(false).build(),
            weight().date(referenceDateTime).value(125.0).unit("pounds").valid(false).build()
        )
        Assert.assertEquals(null, selectMedianBodyWeightPerDay(VitalFunctionTestFactory.withBodyWeights(weights), minimalValidDate))
    }

    companion object {
        private fun weight(): ImmutableBodyWeight.Builder {
            return VitalFunctionTestFactory.bodyWeight().unit(BodyWeightFunctions.EXPECTED_UNIT)
        }
    }
}

