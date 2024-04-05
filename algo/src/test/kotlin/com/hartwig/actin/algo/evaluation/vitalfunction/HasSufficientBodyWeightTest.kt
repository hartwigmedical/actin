package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.weight
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Test

class HasSufficientBodyWeightTest {

    private val function = HasSufficientBodyWeight(40.0, LocalDate.of(2023, 12, 1))
    private val referenceDate = LocalDateTime.of(2023, 12, 2, 0, 0)

    @Test
    fun `Should fail on median weight too low and outside margin of error`() {
        val weights = listOf(
            weight(referenceDate, 30.0, true),
            weight(referenceDate.plusDays(1), 35.0, true)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }

    @Test
    fun `Should pass on median weight above min`() {
        val weights = listOf(
            weight(referenceDate, 39.0, true),
            weight(referenceDate.plusDays(1), 41.5, true)
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)))
    }
}