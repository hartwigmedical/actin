package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.junit.Test
import java.time.LocalDate

class HasLimitedBodyWeightTest {

    private val function = HasLimitedBodyWeight(150.0)
    private val referenceDate = LocalDate.of(2023, 11, 10)

    @Test
    fun `Should fail on median weight too high`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(6)).value(148.0).build(),
            weight().date(referenceDate.minusDays(5)).value(153.0).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    @Test
    fun `Should pass on median weight below max`() {
        val weights = listOf(
            weight().date(referenceDate.minusDays(5)).value(151.0).build(),
            weight().date(referenceDate.minusDays(4)).value(148.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    companion object {
        private fun weight(): ImmutableBodyWeight.Builder {
            return VitalFunctionTestFactory.bodyWeight().unit(BodyWeightFunctions.EXPECTED_UNIT)
        }
    }
}