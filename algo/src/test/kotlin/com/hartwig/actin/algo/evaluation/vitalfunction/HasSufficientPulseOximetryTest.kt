package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDateTime

class HasSufficientPulseOximetryTest {
    val referenceDate = LocalDateTime.of(2023, 12, 7, 12, 30, 0)
    val function = HasSufficientPulseOximetry(90.0)

    @Test
    fun `Should evaluate to undetermined when no measurements are present`() {
        val pulseOximetries: List<VitalFunction> = emptyList()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should only consider measurements within date cutoff for data validity`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDate.minusMonths(3)).value(92.0).unit("percent").build(),
            pulseOximetry().date(referenceDate.minusMonths(2)).value(92.0).unit("percent").build(),
            pulseOximetry().date(referenceDate).value(89.0).unit("percent").build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should only consider measurements with percentage as unit`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDate).value(40.0).unit("test").build(),
            pulseOximetry().date(referenceDate).value(99.0).unit("percent").build(),
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should pass when median SpO2 is above reference value`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDate).value(90.0).unit("percent").build(),
            pulseOximetry().date(referenceDate).value(89.0).unit("percent").build(),
            pulseOximetry().date(referenceDate).value(91.0).unit("percent").build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should fail when median SpO2 is below reference value`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDate).value(89.0).unit("percent").build(),
            pulseOximetry().date(referenceDate.minusDays(4)).value(89.0).unit("percent").build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    companion object {
        private fun pulseOximetry(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.SPO2)
        }
    }
}