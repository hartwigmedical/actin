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
        val atc = AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("category 3").build())
            .build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasNonExactCategory() {
        val atc = AtcTestFactory.atcClassificationBuilder()
            .anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("this is category 1").build()).build()
        val medications = listOf(TestMedicationFactory.builder().atc(atc).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasRightCategory() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("category 1").build())
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
        private val FUNCTION = CurrentlyGetsMedicationOfCategory(MedicationTestFactory.alwaysActive(), setOf("category 1"))
    }
}