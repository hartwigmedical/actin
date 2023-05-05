package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsStableMedicationOfCategoryTest {
    @Test
    fun canEvaluateOnOneTerm() {
        val category1 = "category 1"
        val function = CurrentlyGetsStableMedicationOfCategory(MedicationTestFactory.alwaysActive(), setOf(category1))

        // Fails on no medication
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Passes with single medication with dosing.
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category1).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Passes with another medication with no category and same dosing
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).categories(emptySet()).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Fails on same category and other dosing.
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category1).frequencyUnit("other").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Also fail in case a dosing is combined with medication without dosing.
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(
                        TestMedicationFactory.builder()
                            .from(fixedDosing())
                            .addCategories(category1)
                            .build(), TestMedicationFactory.builder().addCategories(category1).build()
                    )
                )
            )
        )
    }

    @Test
    fun canEvaluateForMultipleTerms() {
        val category1 = "category 1"
        val category2 = "category 2"
        val function = CurrentlyGetsStableMedicationOfCategory(MedicationTestFactory.alwaysActive(), setOf(category1, category2))

        // Passes with single medication with dosing.
        val medications: MutableList<Medication> = mutableListOf()
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category1).build())
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category2).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Passes on same category and other dosing.
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category1).frequencyUnit("other").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Start failing when both categories have wrong dosing.
        medications.add(TestMedicationFactory.builder().from(fixedDosing()).addCategories(category2).frequencyUnit("other").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    companion object {
        private fun fixedDosing(): Medication {
            return TestMedicationFactory.builder()
                .dosageMin(1.0)
                .dosageMax(2.0)
                .dosageUnit("unit 1")
                .frequency(3.0)
                .frequencyUnit("unit 2")
                .ifNeeded(false)
                .build()
        }
    }
}