package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HasRestingHeartRateWithinBoundsTest {

    private val referenceDateTime = LocalDateTime.of(2023, 12, 2, 0, 0)
    private val function = HasRestingHeartRateWithinBounds(60.0, 80.0, LocalDate.of(2023, 12, 1))

    @Test
    fun `Should evaluate to undetermined when no heart rate measurements present`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(emptyList())))
    }

    @Test
    fun `Should evaluate to undetermined if all heart rate measurements invalid`() {
        val heartRates = listOf(
            heartRate(referenceDateTime, 70.0, false).copy(unit = "test")
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should evaluate to undetermined when median heart rate outside reference values but within margin of error`() {
        val heartRates = listOf(
            heartRate(referenceDateTime, 80.0),
            heartRate(referenceDateTime.plusDays(1), 85.0)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should pass when median heart rate within reference values`() {
        val heartRates = listOf(
            heartRate(referenceDateTime, 70.0),
            heartRate(referenceDateTime.plusDays(1), 70.0),
            heartRate(referenceDateTime.plusDays(2), 90.0)
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should fail when median heart rate outside reference values and outside margin of error`() {
        val heartRates = listOf(
            heartRate(referenceDateTime, 90.0),
            heartRate(referenceDateTime.plusDays(1), 95.0),
            heartRate(referenceDateTime.plusDays(2), 90.0)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should only use measurements within date cutoff for data validity`() {
        val heartRates = listOf(
            heartRate(referenceDateTime.minusMonths(1), 70.0),
            heartRate(referenceDateTime.minusMonths(1), 70.0),
            heartRate(referenceDateTime, 95.0)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    @Test
    fun `Should only use 5 most recent data points (medians of medians per day)`() {
        val heartRates = listOf(
            heartRate(referenceDateTime, 180.0),
            heartRate(referenceDateTime, 180.0),
            heartRate(referenceDateTime.plusDays(1), 180.0),
            heartRate(referenceDateTime.plusDays(1), 50.0),
            heartRate(referenceDateTime.plusDays(2), 85.0),
            heartRate(referenceDateTime.plusDays(3), 70.0),
            heartRate(referenceDateTime.plusDays(4), 75.0),
            heartRate(referenceDateTime.plusDays(5), 70.0),
            heartRate(referenceDateTime.plusDays(6), 70.0),
            heartRate(referenceDateTime.plusDays(7), 90.0)
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
    }

    private fun heartRate(date: LocalDateTime, value: Double, valid: Boolean = true): VitalFunction {
        return VitalFunctionTestFactory.vitalFunction(
            category = VitalFunctionCategory.HEART_RATE,
            unit = HasRestingHeartRateWithinBounds.HEART_RATE_EXPECTED_UNIT,
            date = date,
            value = value,
            valid = valid
        )
    }
}