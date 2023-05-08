package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class HasRecentlyReceivedMedicationOfApproximateCategoryTest {
    @Test
    fun canEvaluateForActiveMedications() {
        val function = HasRecentlyReceivedMedicationOfApproximateCategory(
            MedicationTestFactory.alwaysActive(),
            "category to find",
            EVALUATION_DATE.plusDays(1)
        )

        // No medications
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Wrong category
        medications.add(TestMedicationFactory.builder().addCategories("wrong category").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Right category
        medications.add(TestMedicationFactory.builder().addCategories("category to find").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun canEvaluateForStoppedMedication() {
        val function = HasRecentlyReceivedMedicationOfApproximateCategory(
            MedicationTestFactory.alwaysStopped(),
            "category to find",
            EVALUATION_DATE.minusDays(1)
        )

        // Medication stopped after min stop date
        val medication: Medication = TestMedicationFactory.builder().addCategories("category to find").stopDate(EVALUATION_DATE).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(listOf(medication))))
    }

    @Test
    fun cantDetermineWithOldEvaluationDate() {
        val function = HasRecentlyReceivedMedicationOfApproximateCategory(
            MedicationTestFactory.alwaysStopped(),
            "category to find",
            EVALUATION_DATE.minusWeeks(2)
        )

        // Medication stopped after min stop date
        val medication: Medication = TestMedicationFactory.builder().addCategories("category to find").stopDate(EVALUATION_DATE).build()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MedicationTestFactory.withMedications(listOf(medication)))
        )
    }

    companion object {
        private val EVALUATION_DATE = TestClinicalFactory.createMinimalTestClinicalRecord().patient().registrationDate().plusWeeks(1)
    }
}