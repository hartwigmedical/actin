package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate

class HasLimitedBloodPressureTest {

    val referenceDate = LocalDate.of(2020, 11, 19)
    val function = HasLimitedBloodPressure(BloodPressureCategory.SYSTOLIC, 140)
    val bloodPressures: MutableList<VitalFunction> = mutableListOf()

    @Test
    fun `Should evaluate undetermined when no blood pressures known`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    @Test
    fun `Should fail when systolic blood pressure above maximum`() {
        bloodPressures.add(systolic().date(referenceDate).value(145.0).build())
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    @Test
    fun `Should fail when median above maximum`() {
        bloodPressures.add(systolic().date(referenceDate.minusDays(3)).value(150.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(2)).value(155.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(1)).value(135.0).build())
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    @Test
    fun `Should pass when median systolic blood pressure under maximum`() {
        bloodPressures.add(systolic().date(referenceDate.minusDays(3)).value(145.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(2)).value(130.0).build())
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    @Test
    fun `Should pass since only most recent are taken into account`() {
        bloodPressures.add(systolic().date(referenceDate.minusDays(3)).value(130.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(2)).value(125.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(1)).value(120.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(5)).value(200.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(6)).value(200.0).build())
        bloodPressures.add(systolic().date(referenceDate.minusDays(7)).value(200.0).build())
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(bloodPressures))
        )
    }

    @Test
    fun `Should evaluate undetermined when wrong blood pressure category`() {
        val diastBloodPressures = listOf(diastolic().date(referenceDate).value(110.0).build())
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(VitalFunctionTestFactory.withVitalFunctions(diastBloodPressures))
        )
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
