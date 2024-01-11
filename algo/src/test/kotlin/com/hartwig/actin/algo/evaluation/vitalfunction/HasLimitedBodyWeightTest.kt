package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HasLimitedBodyWeightTest {

    private val referenceDate = LocalDateTime.of(2023, 12, 2, 0, 0)
    private val function = HasLimitedBodyWeight(150.0, LocalDate.of(2023, 12, 1))

    @Test
    fun `Should fail on median weight too high`() {
        val weights = listOf(
            weight().date(referenceDate.plusDays(1)).value(148.0).valid(true).build(),
            weight().date(referenceDate.plusDays(2)).value(153.0).valid(true).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    @Test
    fun `Should pass on median weight below max`() {
        val weights = listOf(
            weight().date(referenceDate.plusDays(1)).value(151.0).valid(true).build(),
            weight().date(referenceDate.plusDays(2)).value(148.0).valid(true).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    companion object {
        private fun weight(): ImmutableBodyWeight.Builder {
            return VitalFunctionTestFactory.bodyWeight().unit(BodyWeightFunctions.EXPECTED_UNIT)
        }
    }
}