package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate

class HasSufficientPulseOximetryTest {
    val referenceDate = LocalDate.of(2023, 12, 7)
    val function = HasSufficientPulseOximetry(90.0)

    @Test
    fun `Should evaluate to undetermined when no measurements are present`() {
        val pulseOximetries: List<VitalFunction> = emptyList()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should only consider measurements within date cutoff for data validity`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDate.minusMonths(3)).value(92.0).build(),
            pulseOximetry().date(referenceDate.minusMonths(2)).value(92.0).build(),
            pulseOximetry().date(referenceDate).value(89.0).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should pass when median SpO2 is above reference value`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDate).value(90.0).build(),
            pulseOximetry().date(referenceDate).value(89.0).build(),
            pulseOximetry().date(referenceDate).value(91.0).build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should evaluate to undetermined when median SpO2 is below but one measurement is above reference value`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDate).value(89.0).build(),
            pulseOximetry().date(referenceDate.minusDays(4)).value(91.0).build(),
            pulseOximetry().date(referenceDate.minusDays(5)).value(87.0).build()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    @Test
    fun `Should fail when median SpO2 is below reference value`() {
        val pulseOximetries: List<VitalFunction> = listOf(
            pulseOximetry().date(referenceDate).value(89.0).build(),
            pulseOximetry().date(referenceDate.minusDays(4)).value(89.0).build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulseOximetries)))
    }

    companion object {
        private fun pulseOximetry(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.SPO2)
        }
    }
}