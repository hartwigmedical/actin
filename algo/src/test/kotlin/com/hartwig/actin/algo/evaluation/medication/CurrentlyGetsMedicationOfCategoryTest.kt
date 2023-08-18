package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsMedicationOfCategoryTest {
    @Test
    fun shouldFailWhenNoMedication() {
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenMedicationHasWrongCategory() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("wrong category").build())
                .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasRightCategory() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("category to find").build())
                .build()
        assertEvaluation(
            EvaluationResult.PASS, FUNCTION.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(
                        TestMedicationFactory.builder().atc(atc).build()
                    )
                )
            )
        )
    }

    companion object {
        private val FUNCTION =
            CurrentlyGetsMedicationOfCategory(MedicationTestFactory.alwaysActive(), mapOf("category to find" to setOf("category to find")))
    }
}