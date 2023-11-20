package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate

class HasSufficientBloodPressureTest {

    val referenceDate = LocalDate.of(2020, 11, 19)
    val function = HasSufficientBloodPressure(BloodPressureCategory.SYSTOLIC, 100)
    val bloodPressures: MutableList<VitalFunction> = mutableListOf()
    @Test
    fun `Should evaluate undetermined when no blood pressures known`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures)))
    }

    @Test
    fun `Should fail when systolic blood pressure under minimum`() {
        bloodPressures.add(systolic().date(referenceDate).value(95.0).build())
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    @Test
    fun `Should fail when median under minimum`() {
        bloodPressures.add(systolic().date(referenceDate.minusDays(3)).value(90.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(2)).value(90.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(1)).value(105.0).build())
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    @Test
    fun `Should pass when most recent systolic blood pressure above minimum`(){
        bloodPressures.add(systolic().date(referenceDate.minusDays(3)).value(95.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(2)).value(110.0).build())
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    @Test
    fun `Should pass when median above minimum`(){
        bloodPressures.add(systolic().date(referenceDate.minusDays(3)).value(110.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(2)).value(105.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(1)).value(95.0).build())
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    @Test
    fun `Should pass since only most recent are taken into account`() {
        bloodPressures.add(systolic().date(referenceDate.minusDays(3)).value(110.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(2)).value(105.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(1)).value(105.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(5)).value(20.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(6)).value(20.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(7)).value(20.0).build())
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    @Test
    fun `Should evaluate undetermined when wrong blood pressure category`() {
        val diastBloodPressures = listOf(diastolic().date(referenceDate).value(110.0).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withVitalFunctions(diastBloodPressures)))
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