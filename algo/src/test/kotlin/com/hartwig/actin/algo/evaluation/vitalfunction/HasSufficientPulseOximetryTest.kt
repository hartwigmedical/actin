package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionTestFactory.vitalFunction
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.jupiter.api.Test

class HasSufficientPulseOximetryTest {

    private val referenceDateTime = LocalDateTime.of(2023, 12, 2, 0, 0)
    private val function = HasSufficientPulseOximetry(90.0, LocalDate.of(2023, 12, 1))

    @Test
    fun `Should evaluate to undetermined when no measurements are present`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(emptyList())),
            "No (recent) pulse oximetry data found"
        )
    }

    @Test
    fun `Should evaluate to undetermined if all measurements in wrong unit`() {
        val pulseOximetries = listOf(pulseOximetry(referenceDateTime, 92.0, false, "test"))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)),
            "No (recent) pulse oximetry data found"
        )
    }

    @Test
    fun `Should only consider measurements within date cutoff for data validity`() {
        val pulseOximetries = listOf(
            pulseOximetry(referenceDateTime.minusMonths(3), 92.0),
            pulseOximetry(referenceDateTime.minusMonths(2), 92.0),
            pulseOximetry(referenceDateTime, 84.0)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)),
            "Median pulse oximetry (84.0%) below 90.0"
        )
    }

    @Test
    fun `Should only consider valid measurements`() {
        val pulseOximetries = listOf(
            pulseOximetry(referenceDateTime, 8.0, valid = false),
            pulseOximetry(referenceDateTime, 99.0, valid = true),
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)),
            "Median pulse oximetry (99.0%) above 90.0"
        )
    }

    @Test
    fun `Should pass when median SpO2 is above reference value`() {
        val pulseOximetries = listOf(
            pulseOximetry(referenceDateTime, 90.0),
            pulseOximetry(referenceDateTime, 89.0),
            pulseOximetry(referenceDateTime, 91.0)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)),
            "Median pulse oximetry (90.0%) above 90.0"
        )
    }

    @Test
    fun `Should evaluate to undetermined when median SpO2 is below reference value but within margin of error`() {
        val pulseOximetries = listOf(
            pulseOximetry(referenceDateTime, 88.0),
            pulseOximetry(referenceDateTime.plusDays(1), 89.0),
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)),
            "Median pulse oximetry (88.5%) below 90.0 but within margin of error"
        )
    }

    @Test
    fun `Should fail when median SpO2 is below reference value and outside margin of error`() {
        val pulseOximetries = listOf(
            pulseOximetry(referenceDateTime, 84.0),
            pulseOximetry(referenceDateTime.plusDays(1), 84.0)
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)),
            "Median pulse oximetry (84.0%) below 90.0"
        )
    }

    private fun pulseOximetry(date: LocalDateTime, value: Double, valid: Boolean = true, unit: String = "percent"): VitalFunction {
        return vitalFunction(category = VitalFunctionCategory.SPO2, date = date, value = value, valid = valid, unit = unit)
    }
}