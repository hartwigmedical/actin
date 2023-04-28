package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate

class HasRestingHeartRateWithinBoundsTest {
    @Test
    fun canEvaluate() {
        val referenceDate = LocalDate.of(2020, 11, 19)
        val function = HasRestingHeartRateWithinBounds(60.0, 80.0)
        val heartRates: MutableList<VitalFunction> = mutableListOf()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))
        heartRates.add(heartRate().date(referenceDate).value(70.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))

        // Fail when median drops below 60
        heartRates.add(heartRate().date(referenceDate).value(40.0).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))

        // Succeed again when median back in range
        heartRates.add(heartRate().date(referenceDate).value(80.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))

        // Fail again when median becomes too high
        heartRates.add(heartRate().date(referenceDate.plusDays(1)).value(200.0).build())
        heartRates.add(heartRate().date(referenceDate.plusDays(1)).value(200.0).build())
        heartRates.add(heartRate().date(referenceDate.plusDays(1)).value(200.0).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(heartRates)))

        // Succeed when we add a bunch of more recent correct measures.
        heartRates.add(heartRate().date(referenceDate.plusDays(2)).value(70.0).build())
        heartRates.add(heartRate().date(referenceDate.plusDays(2)).value(70.0).build())
        heartRates.add(heartRate().date(referenceDate.plusDays(2)).value(70.0).build())
        heartRates.add(heartRate().date(referenceDate.plusDays(2)).value(70.0).build())
        heartRates.add(heartRate().date(referenceDate.plusDays(2)).value(70.0).build())
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