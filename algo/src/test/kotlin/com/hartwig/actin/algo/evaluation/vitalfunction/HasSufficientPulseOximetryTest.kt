package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HasSufficientPulseOximetryTest {
    val referenceDateTime = LocalDateTime.of(2023, 12, 2, 0, 0)
    val function = HasSufficientPulseOximetry(90.0, LocalDate.of(2023, 12, 1))

    @Test
    fun `Should evaluate to undetermined when no measurements are present`() {
        val pulseOximetries: List<VitalFunction> = emptyList()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should evaluate to undetermined if all measurements in wrong unit`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDateTime).value(92.0).unit("test").valid(false).build()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should only consider measurements within date cutoff for data validity`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDateTime.minusMonths(3)).value(92.0).unit("percent").valid(true).build(),
            pulseOximetry().date(referenceDateTime.minusMonths(2)).value(92.0).unit("percent").valid(true).build(),
            pulseOximetry().date(referenceDateTime).value(89.0).unit("percent").valid(true).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should only consider valid measurements`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDateTime).value(8.0).unit("percent").valid(false).build(),
            pulseOximetry().date(referenceDateTime).value(99.0).unit("percent").valid(true).build(),
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should pass when median SpO2 is above reference value`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDateTime).value(90.0).unit("percent").valid(true).build(),
            pulseOximetry().date(referenceDateTime).value(89.0).unit("percent").valid(true).build(),
            pulseOximetry().date(referenceDateTime).value(91.0).unit("percent").valid(true).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should fail when median SpO2 is below reference value`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDateTime).value(89.0).unit("percent").valid(true).build(),
            pulseOximetry().date(referenceDateTime.plusDays(1)).value(89.0).unit("percent").valid(true).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    companion object {
        private fun pulseOximetry(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.SPO2)
        }
    }
}