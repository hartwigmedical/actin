package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDateTime

class HasRestingHeartRateWithinBoundsTest {

    val referenceDate = LocalDateTime.of(2023, 12, 11, 12, 30, 0)
    val function = HasRestingHeartRateWithinBounds(60.0, 80.0)

    @Test
    fun `Should evaluate to undetermined when no heart rate measurements present`() {
        val heartRates: List<VitalFunction> = emptyList()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should pass when median heart rate within reference values`() {
        val heartRates = listOf(
            heartRate().date(referenceDate).value(70.0).build(),
            heartRate().date(referenceDate.minusDays(1)).value(70.0).build(),
            heartRate().date(referenceDate.minusDays(2)).value(90.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should fail when median heart rate outside reference values`() {
        val heartRates = listOf(
            heartRate().date(referenceDate).value(80.0).build(),
            heartRate().date(referenceDate.minusDays(1)).value(85.0).build(),
            heartRate().date(referenceDate.minusDays(2)).value(90.0).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should only use measurements within date cutoff for data validity`() {
        val heartRates = listOf(
            heartRate().date(referenceDate).value(80.0).build(),
            heartRate().date(referenceDate.minusDays(1)).value(85.0).build(),
            heartRate().date(referenceDate.minusMonths(2)).value(60.0).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should only use 5 most recent data points (medians of medians per day)`() {
        val heartRates = listOf(
            heartRate().date(referenceDate).value(90.0).build(),
            heartRate().date(referenceDate).value(70.0).build(),
            heartRate().date(referenceDate.minusDays(1)).value(70.0).build(),
            heartRate().date(referenceDate.minusDays(1)).value(50.0).build(),
            heartRate().date(referenceDate.minusDays(2)).value(85.0).build(),
            heartRate().date(referenceDate.minusDays(3)).value(70.0).build(),
            heartRate().date(referenceDate.minusDays(4)).value(75.0).build(),
            heartRate().date(referenceDate.minusDays(5)).value(180.0).build(),
            heartRate().date(referenceDate.minusDays(6)).value(180.0).build(),
            heartRate().date(referenceDate.minusDays(7)).value(180.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    companion object {
        private fun heartRate(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.HEART_RATE)
                .unit(HasRestingHeartRateWithinBounds.UNIT_TO_SELECT)
        }
    }
}