package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsMedicationOfApproximateCategoryTest {
    @Test
    fun canEvaluate() {
        val function = CurrentlyGetsMedicationOfApproximateCategory(MedicationTestFactory.alwaysActive(), setOf("category 1"))

        // No medications yet
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with wrong category
        val wrongAtc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("category 3").build())
                .build()
        medications.add(TestMedicationFactory.builder().atc(wrongAtc).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with non-exact category
        val nonExactAtc = AtcTestFactory.atcClassificationBuilder()
            .anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("this is category 1").build()).build()
        medications.add(TestMedicationFactory.builder().atc(nonExactAtc).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with right category
        val rightAtc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("category 1").build())
                .build()
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(
                        TestMedicationFactory.builder().atc(rightAtc).build()
                    )
                )
            )
        )
    }
}