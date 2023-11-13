package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.junit.Test
import java.time.LocalDate

class HasSufficientBodyWeightTest {

    private val function = HasSufficientBodyWeight(40.0)
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
    fun `Should evaluate undetermined on most recent weight in wrong unit`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(4)).value(55.0).build(),
            weight().date(referenceDate.minusDays(3)).value(120.0).unit("pounds").build()
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights))
        )
    }

    @Test
    fun `Should fail on most recent weight too low`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(6)).value(41.0).build(),
            weight().date(referenceDate.minusDays(5)).value(39.0).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    @Test
    fun `Should pass on most recent weight above or equal to min`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(5)).value(38.0).build(),
            weight().date(referenceDate.minusDays(4)).value(40.5).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    @Test
    fun `Should pass on most recent weight equal to min`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(5)).value(38.0).build(),
            weight().date(referenceDate.minusDays(3)).value(40.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    companion object {
        private fun weight(): ImmutableBodyWeight.Builder {
            return VitalFunctionTestFactory.bodyWeight().unit(BodyWeightFunctions.EXPECTED_UNIT)
        }
    }
}