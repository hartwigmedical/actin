package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureCategory.DIASTOLIC
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureCategory.SYSTOLIC
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureFunctions.evaluatePatientMaximumBloodPressure
import com.hartwig.actin.algo.evaluation.vitalfunction.BloodPressureFunctions.evaluatePatientMinimumBloodPressure
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.VitalFunction
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

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
    fun `Should fail when median systolic blood pressure under minimum and outside margin`() {
        val bloodPressures = listOf(
            systolic(referenceDateTime, 75.0),
            systolic(referenceDateTime, 85.0)
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
    fun `Should fail when median diastolic blood pressure under minimum and outside margin`() {
        val bloodPressures = listOf(
            diastolic(referenceDateTime, 65.0),
            diastolic(referenceDateTime.plusDays(1), 70.0)
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
    fun `Should evaluate to recoverable undetermined when median systolic blood pressure under minimum but within margin of error`() {
        val bloodPressures = listOf(
            systolic(referenceDateTime, 90.0),
            systolic(referenceDateTime.plusDays(1), 100.0)
        )
        val evaluation = evaluatePatientMinimumBloodPressure(
            VitalFunctionTestFactory.withVitalFunctions(bloodPressures), SYSTOLIC, 100, minimumValidDate
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        Assertions.assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should evaluate to recoverable undetermined when median systolic blood pressure above maximum but within margin of error`() {
        val bloodPressures = listOf(
            systolic(referenceDateTime, 110.0),
            systolic(referenceDateTime.plusDays(1), 140.0)
        )
        val evaluation = evaluatePatientMaximumBloodPressure(
            VitalFunctionTestFactory.withVitalFunctions(bloodPressures), SYSTOLIC, 120, minimumValidDate
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        Assertions.assertThat(evaluation.recoverable).isTrue()
    }

    @Test
    fun `Should pass when median systolic blood pressure above minimum`() {
        val bloodPressures = listOf(
            systolic(referenceDateTime, 110.0),
            systolic(referenceDateTime.plusDays(1), 95.0)
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
            diastolic(referenceDateTime, 80.0),
            diastolic(referenceDateTime.plusDays(1), 75.0)
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
    fun `Should fail when median diastolic blood pressure above maximum and outside margin`() {
        val bloodPressures = listOf(
            diastolic(referenceDateTime, 125.0),
            diastolic(referenceDateTime.plusDays(1), 100.0)
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
            systolic(referenceDateTime, 110.0),
            systolic(referenceDateTime.plusDays(1), 140.0)
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
            systolic(referenceDateTime.plusDays(5), 110.0),
            systolic(referenceDateTime.plusDays(4), 105.0),
            systolic(referenceDateTime.plusDays(3), 105.0),
            systolic(referenceDateTime.plusDays(2), 20.0),
            systolic(referenceDateTime.plusDays(1), 20.0),
            systolic(referenceDateTime, 20.0)
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
            VitalFunctionTestFactory.withVitalFunctions(listOf(diastolic(referenceDateTime, 110.0))),
            SYSTOLIC, 100, minimumValidDate
        )
        )
    }

    private fun vitalFunction(subcategory: String, date: LocalDateTime, value: Double): VitalFunction {
        return VitalFunctionTestFactory.vitalFunction(
            category = VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE,
            subcategory = subcategory,
            date = date,
            value = value
        )
    }

    private fun systolic(date: LocalDateTime, value: Double): VitalFunction {
        return vitalFunction(SYSTOLIC.display(), date, value)
    }

    private fun diastolic(date: LocalDateTime, value: Double): VitalFunction {
        return vitalFunction(DIASTOLIC.display(), date, value)
    }
}