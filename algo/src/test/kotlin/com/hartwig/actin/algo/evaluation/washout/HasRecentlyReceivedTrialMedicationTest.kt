package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.MedicationTestFactory
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class HasRecentlyReceivedTrialMedicationTest {
    @Test
    fun shouldFailWhenNoMedication() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_ACTIVE.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenMedicationIsNoTrialMedication() {
        val medications = listOf(TestMedicationFactory.builder().isTrialMedication(false).build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_ACTIVE.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationIsTrialMedication() {
        val medications = listOf(TestMedicationFactory.builder().isTrialMedication(true).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION_ACTIVE.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasCorrectDate() {
        val function = HasRecentlyReceivedTrialMedication(
            MedicationTestFactory.alwaysStopped(),
            EVALUATION_DATE.minusDays(1)
        )
        val medications = listOf(TestMedicationFactory.builder().isTrialMedication(true).stopDate(EVALUATION_DATE).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldBeUndeterminedWhenMedicationStoppedAfterMinStopDate() {
        val function = HasRecentlyReceivedTrialMedication(
            MedicationTestFactory.alwaysStopped(),
            EVALUATION_DATE.minusWeeks(2)
        )
        val medications = listOf(TestMedicationFactory.builder().isTrialMedication(true).stopDate(EVALUATION_DATE).build())
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    companion object {
        private val EVALUATION_DATE = TestClinicalFactory.createMinimalTestClinicalRecord().patient().registrationDate().plusWeeks(1)
        private val FUNCTION_ACTIVE = HasRecentlyReceivedTrialMedication(
            MedicationTestFactory.alwaysActive(),
            EVALUATION_DATE.plusDays(1)
        )
    }
}