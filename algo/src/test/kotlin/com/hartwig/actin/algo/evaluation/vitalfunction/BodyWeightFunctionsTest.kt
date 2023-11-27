package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.evaluatePatientForMaximumBodyWeight
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.evaluatePatientForMinimumBodyWeight
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.junit.Test
import java.time.LocalDate

class BodyWeightFunctionsTest {

    private val referenceDate = LocalDate.of(2023, 11, 10)

    @Test
    fun `Should evaluate to undetermined on no body weight documented`() {
        val weights: List<BodyWeight> = emptyList()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0)
        )
    }

    @Test
    fun `Should evaluate to undetermined on weight in wrong unit`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(4)).value(149.0).build(),
            weight().date(referenceDate.minusDays(3)).value(148.0).unit("pounds").build()
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMaximumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 150.0)
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0)
        )
    }

    @Test
    fun `Should fail on most recent weight above max`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(6)).value(148.0).build(),
            weight().date(referenceDate.minusDays(5)).value(155.0).build()
        )
        assertEvaluation(EvaluationResult.FAIL, evaluatePatientForMaximumBodyWeight(
            VitalFunctionTestFactory.withBodyWeights(weights), 150.0)
        )
    }

    @Test
    fun `Should pass on most recent weight below max`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(5)).value(155.0).build(),
            weight().date(referenceDate.minusDays(4)).value(148.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, evaluatePatientForMaximumBodyWeight(
            VitalFunctionTestFactory.withBodyWeights(weights), 150.0)
        )
    }

    @Test
    fun `Should pass on most recent weight equal to max`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(5)).value(155.0).build(),
            weight().date(referenceDate.minusDays(3)).value(150.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, evaluatePatientForMaximumBodyWeight(
            VitalFunctionTestFactory.withBodyWeights(weights), 150.0)
        )
    }

    @Test
    fun `Should fail on most recent weight below min`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(6)).value(42.0).build(),
            weight().date(referenceDate.minusDays(5)).value(38.0).build()
        )
        assertEvaluation(EvaluationResult.FAIL, evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0)
        )
    }

    @Test
    fun `Should pass on most recent weight above min`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(5)).value(38.0).build(),
            weight().date(referenceDate.minusDays(4)).value(41.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0)
        )
    }

    @Test
    fun `Should pass on most recent weight equal to min`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(5)).value(39.0).build(),
            weight().date(referenceDate.minusDays(3)).value(40.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, evaluatePatientForMinimumBodyWeight(VitalFunctionTestFactory.withBodyWeights(weights), 40.0)
        )
    }

    companion object {
        private fun weight(): ImmutableBodyWeight.Builder {
            return VitalFunctionTestFactory.bodyWeight().unit(BodyWeightFunctions.EXPECTED_UNIT)
        }
    }
}

