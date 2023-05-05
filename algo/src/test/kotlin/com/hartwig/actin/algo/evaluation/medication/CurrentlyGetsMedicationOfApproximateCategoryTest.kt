package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsMedicationOfApproximateCategoryTest {
    @Test
    fun canEvaluate() {
        val function = CurrentlyGetsMedicationOfApproximateCategory(MedicationTestFactory.alwaysActive(), "category 1")

        // No medications yet
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with wrong category
        medications.add(TestMedicationFactory.builder().addCategories("category 2", "category 3").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with non-exact category
        medications.add(TestMedicationFactory.builder().addCategories("this is category 1").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with right category
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(
                        TestMedicationFactory.builder().addCategories("category 4", "category 1").build()
                    )
                )
            )
        )
    }
}