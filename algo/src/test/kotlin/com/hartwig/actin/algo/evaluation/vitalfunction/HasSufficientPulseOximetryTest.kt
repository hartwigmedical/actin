package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate

class HasSufficientPulseOximetryTest {
    @Test
    fun canEvaluate() {
        val referenceDate = LocalDate.of(2021, 11, 19)
        val function = HasSufficientPulseOximetry(90.0)
        val pulses: MutableList<VitalFunction> = mutableListOf()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)))
        pulses.add(pulse().date(referenceDate).value(92.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)))

        // Undetermined when the median falls below 90 but one measure above 90.
        pulses.add(pulse().date(referenceDate.minusDays(1)).value(80.0).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)))

        // Succeed when median goes above 90.
        pulses.add(pulse().date(referenceDate).value(92.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)))

        // Still succeed again with multiple more good results.
        pulses.add(pulse().date(referenceDate.minusDays(2)).value(98.0).build())
        pulses.add(pulse().date(referenceDate.minusDays(3)).value(99.0).build())
        pulses.add(pulse().date(referenceDate.minusDays(4)).value(98.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)))

        // Still succeed since we only take X most recent measures.
        pulses.add(pulse().date(referenceDate.minusDays(5)).value(20.0).build())
        pulses.add(pulse().date(referenceDate.minusDays(6)).value(20.0).build())
        pulses.add(pulse().date(referenceDate.minusDays(7)).value(20.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)))

        // Fail if we add more recent measures that are too low
        pulses.add(pulse().date(referenceDate.plusDays(1)).value(20.0).build())
        pulses.add(pulse().date(referenceDate.plusDays(1)).value(20.0).build())
        pulses.add(pulse().date(referenceDate.plusDays(1)).value(20.0).build())
        pulses.add(pulse().date(referenceDate.plusDays(1)).value(20.0).build())
        pulses.add(pulse().date(referenceDate.plusDays(1)).value(20.0).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(pulses)))
    }

    companion object {
        private fun pulse(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction().category(VitalFunctionCategory.SPO2)
        }
    }
}