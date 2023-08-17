package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsStableMedicationOfCategoryTest {
    @Test
    fun shouldFailWhenNoMedication() {
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_1.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenSingleMedicationWithDosing() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name(CATEGORY_1).build()).build()
        val medications = listOf(TestMedicationFactory.builder().dosage(fixedDosing()).atc(atc).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION_1.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenAnotherMedicationWithNoCategoryAndSameDosing() {
        val atc = AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("").build()).build()
        val medications = listOf(TestMedicationFactory.builder().dosage(fixedDosing()).atc(atc).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION_1.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenSameCategoryAndOtherDosing() {
        val atc = AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("").build()).build()
        val medications = listOf(
            TestMedicationFactory.builder().dosage(ImmutableDosage.builder().from(fixedDosing()).frequencyUnit("other").build()).atc(atc)
                .build()
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_1.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenDosingIsCombinedWithMedicationWithoutDosing() {
        val atc = AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name("").build()).build()
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION_1.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(
                        TestMedicationFactory.builder()
                            .dosage(fixedDosing())
                            .atc(atc)
                            .build(), TestMedicationFactory.builder().atc(atc).build()
                    )
                )
            )
        )
    }

    @Test
    fun shouldPassWithSingleMedicationWithDosing() {
        val medications: MutableList<Medication> = mutableListOf()
        val atc1 =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name(CATEGORY_1).build()).build()
        val atc2 =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name(CATEGORY_2).build()).build()
        medications.add(TestMedicationFactory.builder().dosage(fixedDosing()).atc(atc1).build())
        medications.add(TestMedicationFactory.builder().dosage(fixedDosing()).atc(atc2).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION_2.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassOnSameCategoryAndOtherDosing() {
        val atc1 =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name(CATEGORY_1).build()).build()
        val medications = listOf(
            TestMedicationFactory.builder().dosage(ImmutableDosage.builder().from(fixedDosing()).frequencyUnit("other").build())
                .atc(atc1).build()
        )
        assertEvaluation(EvaluationResult.PASS, FUNCTION_2.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenBothCategoriesHaveWrongDosing() {
        val atc2 =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().name(CATEGORY_2).build()).build()
        val medications = listOf(
            TestMedicationFactory.builder().dosage(ImmutableDosage.builder().from(fixedDosing()).frequencyUnit("other").build())
                .atc(atc2).build()
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_2.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    companion object {
        private fun fixedDosing(): Dosage {
            return ImmutableDosage.builder()
                .dosageMin(1.0)
                .dosageMax(2.0)
                .dosageUnit("unit 1")
                .frequency(3.0)
                .frequencyUnit("unit 2")
                .ifNeeded(false)
                .build()
        }

        private const val CATEGORY_1 = "category 1"
        private const val CATEGORY_2 = "category 2"
        private val FUNCTION_1 = CurrentlyGetsStableMedicationOfCategory(MedicationTestFactory.alwaysActive(), setOf(CATEGORY_1))
        private val FUNCTION_2 =
            CurrentlyGetsStableMedicationOfCategory(MedicationTestFactory.alwaysActive(), setOf(CATEGORY_1, CATEGORY_2))
    }
}