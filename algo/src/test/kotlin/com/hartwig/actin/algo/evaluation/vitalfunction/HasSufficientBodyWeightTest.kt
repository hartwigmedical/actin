package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight
import org.junit.Test
import java.time.LocalDateTime

class HasSufficientBodyWeightTest {

    private val function = HasSufficientBodyWeight(40.0)
    private val referenceDate = LocalDateTime.of(2023, 11, 10, 12, 30, 0)

    @Test
    fun `Should fail on median weight too low`() {
        val weights = listOf(
            weight().date(referenceDate).value(40.0).build(),
            weight().date(referenceDate.minusDays(1)).value(39.0).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    @Test
    fun `Should pass on median weight above min`() {
        val weights = listOf(
            weight().date(referenceDate).value(39.0).build(),
            weight().date(referenceDate.minusDays(4)).value(41.5).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    companion object {
        private fun weight(): ImmutableBodyWeight.Builder {
            return VitalFunctionTestFactory.bodyWeight().unit(BodyWeightFunctions.EXPECTED_UNIT)
        }
    }
}