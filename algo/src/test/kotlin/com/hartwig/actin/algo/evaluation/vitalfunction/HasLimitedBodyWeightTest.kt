package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.junit.Test
import java.time.LocalDate

class HasLimitedBodyWeightTest {

    private val function = HasLimitedBodyWeight(150.0)
    private val referenceDate = LocalDate.of(2023, 11, 10)

    @Test
    fun `Should evaluate undetermined on no body weight documented`() {
        val weights: List<BodyWeight> = emptyList()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights))
        )
    }

    @Test
    fun `Should evaluate undetermined on weight in wrong unit`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(4)).value(149.0).build(),
            weight().date(referenceDate.minusDays(3)).value(148.0).unit("pounds").build()
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights))
        )
    }

    @Test
    fun `Should fail on most recent weight too high`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(6)).value(148.0).build(),
            weight().date(referenceDate.minusDays(5)).value(155.0).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    @Test
    fun `Should pass on most recent weight below max`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(5)).value(155.0).build(),
            weight().date(referenceDate.minusDays(4)).value(148.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    @Test
    fun `Should pass on most recent weight equal to max`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(5)).value(155.0).build(),
            weight().date(referenceDate.minusDays(3)).value(150.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    companion object {
        private fun weight(): ImmutableBodyWeight.Builder {
            return VitalFunctionTestFactory.bodyWeight().unit(BodyWeightFunctions.EXPECTED_UNIT)
        }
    }
}