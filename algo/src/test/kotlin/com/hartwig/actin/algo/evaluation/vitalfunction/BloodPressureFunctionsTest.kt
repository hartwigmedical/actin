package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureCategory.DIASTOLIC
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureCategory.SYSTOLIC
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureFunctions.evaluatePatientMaximumBloodPressure
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureFunctions.evaluatePatientMinimumBloodPressure
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import org.junit.Test
import java.time.LocalDate

class BloodPressureFunctionsTest {

    private val minimumValidDate = LocalDate.of(2023, 12, 1)
    private val referenceDateTime = minimumValidDate.atStartOfDay().plusDays(1)

    @Test
    fun `Should evaluate to undetermined when no blood pressures known`() {
        val bloodPressures = emptyList<VitalFunction>()
        assertEvaluation(EvaluationResult.UNDETERMINED,
            evaluatePatientMinimumBloodPressure(
                VitalFunctionTestFactory.withVitalFunctions(bloodPressures),
                DIASTOLIC,
                60,
                minimumValidDate
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED,
            evaluatePatientMaximumBloodPressure(
                VitalFunctionTestFactory.withVitalFunctions(bloodPressures),
                SYSTOLIC,
                100,
                minimumValidDate
            )
        )
    }

    @Test
    fun `Should fail when median systolic blood pressure under minimum`() {
        val bloodPressures = listOf(
            systolic().date(referenceDateTime).value(85.0).valid(true).build(),
            systolic().date(referenceDateTime).value(105.0).valid(true).build()
        )

        assertEvaluation(EvaluationResult.FAIL,
            evaluatePatientMinimumBloodPressure(
                VitalFunctionTestFactory.withVitalFunctions(bloodPressures),
                SYSTOLIC,
                100,
                minimumValidDate
            )
        )
    }

    @Test
    fun `Should fail when median diastolic blood pressure under minimum`() {
        val bloodPressures = listOf(
            diastolic().date(referenceDateTime).value(65.0).valid(true).build(),
            diastolic().date(referenceDateTime.plusDays(1)).value(90.0).valid(true).build()
        )

        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePatientMinimumBloodPressure(
                VitalFunctionTestFactory.withVitalFunctions(bloodPressures),
                DIASTOLIC,
                80,
                minimumValidDate
            )
        )
    }

    @Test
    fun `Should pass when median systolic blood pressure above minimum`() {
        val bloodPressures = listOf(
            systolic().date(referenceDateTime).value(110.0).valid(true).build(),
            systolic().date(referenceDateTime.plusDays(1)).value(95.0).valid(true).build()
        )

        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientMinimumBloodPressure(
                VitalFunctionTestFactory.withVitalFunctions(bloodPressures),
                SYSTOLIC,
                100,
                minimumValidDate
            )
        )
    }

    @Test
    fun `Should pass when median diastolic blood pressure above minimum`() {
        val bloodPressures = listOf(
            diastolic().date(referenceDateTime).value(80.0).valid(true).build(),
            diastolic().date(referenceDateTime.plusDays(1)).value(75.0).valid(true).build()
        )

        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientMinimumBloodPressure(
                VitalFunctionTestFactory.withVitalFunctions(bloodPressures),
                DIASTOLIC,
                75,
                minimumValidDate
            )
        )
    }

    @Test
    fun `Should fail when median diastolic blood pressure above maximum`() {
        val bloodPressures = listOf(
            diastolic().date(referenceDateTime).value(110.0).valid(true).build(),
            diastolic().date(referenceDateTime.plusDays(1)).value(95.0).valid(true).build()
        )

        assertEvaluation(
            EvaluationResult.FAIL,
            evaluatePatientMaximumBloodPressure(
                VitalFunctionTestFactory.withVitalFunctions(bloodPressures),
                DIASTOLIC,
                100,
                minimumValidDate
            )
        )
    }

    @Test
    fun `Should pass when median systolic blood pressure under maximum`() {
        val bloodPressures = listOf(
            systolic().date(referenceDateTime).value(110.0).valid(true).build(),
            systolic().date(referenceDateTime.plusDays(1)).value(140.0).valid(true).build()
        )

        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientMaximumBloodPressure(
                VitalFunctionTestFactory.withVitalFunctions(bloodPressures),
                SYSTOLIC,
                130,
                minimumValidDate
            )
        )
    }

    @Test
    fun `Should pass since only most recent are taken into account`() {
        val bloodPressures = listOf(
            systolic().date(referenceDateTime.plusDays(5)).value(110.0).valid(true).build(),
            systolic().date(referenceDateTime.plusDays(4)).value(105.0).valid(true).build(),
            systolic().date(referenceDateTime.plusDays(3)).value(105.0).valid(true).build(),
            systolic().date(referenceDateTime.plusDays(2)).value(20.0).valid(true).build(),
            systolic().date(referenceDateTime.plusDays(1)).value(20.0).valid(true).build(),
            systolic().date(referenceDateTime).value(20.0).valid(true).build()
        )
        assertEvaluation(
            EvaluationResult.PASS,
            evaluatePatientMinimumBloodPressure(
                VitalFunctionTestFactory.withVitalFunctions(bloodPressures),
                SYSTOLIC,
                100,
                minimumValidDate
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined when wrong blood pressure category`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluatePatientMaximumBloodPressure(
            VitalFunctionTestFactory.withVitalFunctions(listOf(diastolic().date(referenceDateTime).value(110.0).valid(true).build())),
            SYSTOLIC, 100, minimumValidDate
        )
        )
    }

    private fun systolic(): VitalFunction {
        return VitalFunctionTestFactory.vitalFunction()
            .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
            .subcategory(SYSTOLIC.display())
    }

    private fun diastolic(): VitalFunction {
        return VitalFunctionTestFactory.vitalFunction()
            .category(VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE)
            .subcategory(DIASTOLIC.display())
    }
}