package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsMedicationOfExactCategoryTest {
    @Test
    fun canEvaluate() {
        val function = CurrentlyGetsMedicationOfExactCategory(MedicationTestFactory.alwaysActive(), setOf("category 1"))

        // No medications yet
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with wrong ATC level name
        val wrongAtc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("category 1").build())
                .build()
        medications.add(TestMedicationFactory.builder().atc(wrongAtc).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with non-exact ATC level name
        val nonExactAtc = AtcTestFactory.atcClassificationBuilder()
            .anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("this is category 1").build()).build()
        medications.add(TestMedicationFactory.builder().atc(nonExactAtc).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))

        // Medication with right ATC level name
        val rightAtc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("category 1").build())
                .build()
        medications.add(TestMedicationFactory.builder().atc(rightAtc).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }
}