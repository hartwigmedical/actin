package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HasSufficientBloodPressureTest {
    @Test
    fun canEvaluate() {
        val referenceDate = LocalDate.of(2020, 11, 19)
        val function = HasSufficientBloodPressure(BloodPressureCategory.SYSTOLIC, 100.0)
        val bloodPressures: MutableList<VitalFunction> = mutableListOf()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)))
        bloodPressures.add(systolic().date(referenceDate).value(110.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)))

        // Undetermined when the median falls below 100 but one measure above 100
        bloodPressures.add(systolic().date(referenceDate.minusDays(1)).value(70.0).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)))

        // Succeed again when the median goes above 100
        bloodPressures.add(systolic().date(referenceDate.minusDays(2)).value(110.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(3)).value(110.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(4)).value(110.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)))

        // Still succeed since we only take X most recent measures.
        bloodPressures.add(systolic().date(referenceDate.minusDays(5)).value(20.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(6)).value(20.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(7)).value(20.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)))

        // Fail when we have lots of low measures prior to evaluation
        bloodPressures.add(systolic().date(referenceDate.plusDays(1)).value(90.0).build())
        bloodPressures.add(systolic().date(referenceDate.plusDays(2)).value(90.0).build())
        bloodPressures.add(systolic().date(referenceDate.plusDays(3)).value(90.0).build())
        bloodPressures.add(systolic().date(referenceDate.plusDays(4)).value(90.0).build())
        bloodPressures.add(systolic().date(referenceDate.plusDays(5)).value(90.0).build())
        val actual = function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        assertEvaluation(EvaluationResult.FAIL, actual)
        assertTrue(actual.recoverable())
    }

    @Test
    fun canFilterOnCategory() {
        val referenceDate = LocalDate.of(2020, 11, 19)
        val function = HasSufficientBloodPressure(BloodPressureCategory.SYSTOLIC, 100.0)
        val bloodPressures = listOf(diastolic().date(referenceDate).value(110.0).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)))
    }

    companion object {
        private fun systolic(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory(BloodPressureCategory.SYSTOLIC.display())
        }

        private fun diastolic(): ImmutableVitalFunction.Builder {
            return VitalFunctionTestFactory.vitalFunction()
                .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
                .subcategory(BloodPressureCategory.DIASTOLIC.display())
        }
    }
}