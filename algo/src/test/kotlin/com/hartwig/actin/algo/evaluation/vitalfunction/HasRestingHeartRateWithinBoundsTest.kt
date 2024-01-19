package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HasRestingHeartRateWithinBoundsTest {

    val referenceDateTime = LocalDateTime.of(2023, 12, 2, 0, 0)
    val function = HasRestingHeartRateWithinBounds(60.0, 80.0, LocalDate.of(2023, 12, 1))

    @Test
    fun `Should evaluate to undetermined when no heart rate measurements present`() {
        val heartRates: List<VitalFunction> = emptyList()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should evaluate to undetermined if all heart rate measurements invalid`() {
        val heartRates: List<VitalFunction> = listOf(
            heartRate().date(referenceDateTime).value(70.0).unit("test").valid(false).build()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should pass when median heart rate within reference values`() {
        val heartRates = listOf(
            heartRate().date(referenceDateTime).value(70.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(1)).value(70.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(2)).value(90.0).valid(true).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should fail when median heart rate outside reference values`() {
        val heartRates = listOf(
            heartRate().date(referenceDateTime).value(80.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(1)).value(85.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(2)).value(90.0).valid(true).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should only use measurements within date cutoff for data validity`() {
        val heartRates = listOf(
            heartRate().date(referenceDateTime.minusMonths(1)).value(70.0).valid(true).build(),
            heartRate().date(referenceDateTime.minusMonths(1)).value(70.0).valid(true).build(),
            heartRate().date(referenceDateTime).value(85.0).valid(true).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should only use 5 most recent data points (medians of medians per day)`() {
        val heartRates = listOf(
            heartRate().date(referenceDateTime).value(180.0).valid(true).build(),
            heartRate().date(referenceDateTime).value(180.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(1)).value(180.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(1)).value(50.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(2)).value(85.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(3)).value(70.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(4)).value(75.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(5)).value(70.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(6)).value(70.0).valid(true).build(),
            heartRate().date(referenceDateTime.plusDays(7)).value(90.0).valid(true).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    companion object {
        private fun heartRate(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.HEART_RATE)
                .unit(HasRestingHeartRateWithinBounds.HEART_RATE_EXPECTED_UNIT)
        }
    }
}