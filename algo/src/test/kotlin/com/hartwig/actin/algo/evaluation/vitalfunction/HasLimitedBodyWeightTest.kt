package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.weight
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Test

class HasLimitedBodyWeightTest {

    private val referenceDate = LocalDateTime.of(2023, 12, 2, 0, 0)
    private val function = HasLimitedBodyWeight(150.0, LocalDate.of(2023, 12, 1))

    @Test
    fun `Should fail on median weight too high and outside margin of error`() {
        val weights = listOf(
            weight(referenceDate.plusDays(1), 155.0, true),
            weight(referenceDate.plusDays(2), 165.0, true)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    @Test
    fun `Should pass on median weight below max`() {
        val weights = listOf(
            weight(referenceDate.plusDays(1), 151.0, true),
            weight(referenceDate.plusDays(2), 148.0, true)
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }
}