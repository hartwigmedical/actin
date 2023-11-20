package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureCategory.DIASTOLIC
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureCategory.SYSTOLIC
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureFuncions.evaluatePatientBloodPressureAgainstMax
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureFuncions.evaluatePatientBloodPressureAgainstMin
import com.hartwig.actin.clinical.datamodel.ImmutableVitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate

class BloodPressureFunctionsTest {

    val referenceDate = LocalDate.of(2020, 11, 19)

    @Test
    fun `Should evaluate undetermined when no blood pressures known`() {
        val bloodPressures = emptyList<VitalFunction>()
        assertEvaluation(EvaluationResult.UNDETERMINED,
            evaluatePatientBloodPressureAgainstMin(VitalFunctionTestFactory.withVitalFunctions(bloodPressures), DIASTOLIC, 60))
        assertEvaluation(EvaluationResult.UNDETERMINED,
            evaluatePatientBloodPressureAgainstMax(VitalFunctionTestFactory.withVitalFunctions(bloodPressures), SYSTOLIC, 100))
    }

    @Test
    fun `Should fail when median systolic blood pressure under minimum`() {
        val bloodPressures = listOf(systolic().date(referenceDate).value(85.0).build(), systolic().date(referenceDate).value(105.0).build())

        assertEvaluation(EvaluationResult.FAIL,
            evaluatePatientBloodPressureAgainstMin(VitalFunctionTestFactory.withVitalFunctions(bloodPressures), SYSTOLIC, 100)
        )
    }

    @Test
    fun `Should fail when median diastolic blood pressure under minimum`() {
        val bloodPressures = listOf(
            diastolic().date(referenceDate.minusDays(3)).value(65.0).build(),
            diastolic().date(referenceDate.minusDays(2)).value(90.0).build())

        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePatientBloodPressureAgainstMin(VitalFunctionTestFactory.withVitalFunctions(bloodPressures), DIASTOLIC, 80)
        )
    }

    @Test
    fun `Should pass when median systolic blood pressure above minimum`() {
        val bloodPressures = listOf(
            systolic().date(referenceDate.minusDays(3)).value(110.0).build(),
            systolic().date(referenceDate.minusDays(2)).value(95.0).build())

        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientBloodPressureAgainstMin(VitalFunctionTestFactory.withVitalFunctions(bloodPressures), SYSTOLIC, 100)
        )
    }

    @Test
    fun `Should pass when median diastolic blood pressure above minimum`() {
        val bloodPressures = listOf(
            systolic().date(referenceDate.minusDays(3)).value(80.0).build(),
            systolic().date(referenceDate.minusDays(2)).value(75.0).build())

        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientBloodPressureAgainstMin(VitalFunctionTestFactory.withVitalFunctions(bloodPressures), DIASTOLIC, 75)
        )
    }

    @Test
    fun `Should fail when median diastolic blood pressure above maximum`() {
        val bloodPressures = listOf(
            diastolic().date(referenceDate.minusDays(3)).value(110.0).build(),
            diastolic().date(referenceDate.minusDays(2)).value(95.0).build())

        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePatientBloodPressureAgainstMax(VitalFunctionTestFactory.withVitalFunctions(bloodPressures), DIASTOLIC, 100)
        )
    }

    @Test
    fun `Should pass when median systolic blood pressure under maximum`() {
        val bloodPressures = listOf(
            systolic().date(referenceDate.minusDays(3)).value(110.0).build(),
            systolic().date(referenceDate.minusDays(2)).value(140.0).build())

        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientBloodPressureAgainstMin(VitalFunctionTestFactory.withVitalFunctions(bloodPressures), SYSTOLIC, 130)
        )
    }

    @Test
    fun `Should pass since only most recent are taken into account`() {
        val bloodPressures = listOf(
            systolic().date(referenceDate.minusDays(3)).value(110.0).build(),
            systolic().date(referenceDate.minusDays(2)).value(105.0).build(),
            systolic().date(referenceDate.minusDays(1)).value(105.0).build(),
            systolic().date(referenceDate.minusDays(5)).value(20.0).build(),
            systolic().date(referenceDate.minusDays(6)).value(20.0).build(),
            systolic().date(referenceDate.minusDays(7)).value(20.0).build())
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientBloodPressureAgainstMin(VitalFunctionTestFactory.withVitalFunctions(bloodPressures), SYSTOLIC, 100)
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