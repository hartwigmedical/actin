package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.ImmutableDosage
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsStableMedicationOfCategoryTest {
    @Test
    fun shouldFailWhenNoMedication() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, ONE_CATEGORY_FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenSingleMedicationWithDosing() {
        val medications = listOf(TestMedicationFactory.createMinimal().dosage(fixedDosing()).atc(ATC_CATEGORY_1).build())
        assertEvaluation(EvaluationResult.PASS, ONE_CATEGORY_FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenAnotherMedicationWithNoCategoryAndSameDosing() {
        val medications = listOf(
            TestMedicationFactory.createMinimal().dosage(fixedDosing()).atc(ATC_CATEGORY_1).build(),
            TestMedicationFactory.createMinimal().dosage(fixedDosing()).build()
        )
        assertEvaluation(EvaluationResult.PASS, ONE_CATEGORY_FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenSameCategoryAndOtherDosing() {
        val medications = listOf(
            TestMedicationFactory.createMinimal().dosage(fixedDosing()).atc(ATC_CATEGORY_1).build(),
            TestMedicationFactory.createMinimal().dosage(fixedDosing()).build(),
            TestMedicationFactory.createMinimal().dosage(ImmutableDosage.builder().from(fixedDosing()).frequencyUnit("other").build())
                .atc(
                    ATC_CATEGORY_1
                ).build()
        )
        assertEvaluation(EvaluationResult.FAIL, ONE_CATEGORY_FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenDosingIsCombinedWithMedicationWithoutDosing() {
        val medications = listOf(
            TestMedicationFactory.createMinimal()
                .dosage(fixedDosing())
                .build(), TestMedicationFactory.createMinimal().build()
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            ONE_CATEGORY_FUNCTION.evaluate(
                MedicationTestFactory.withMedications(
                    medications
                )
            )
        )
    }

    @Test
    fun shouldPassWithMultipleMedicationsWithDosing() {
        val medications = listOf(
            TestMedicationFactory.createMinimal().dosage(fixedDosing()).atc(ATC_CATEGORY_1).build(),
            TestMedicationFactory.createMinimal().dosage(fixedDosing()).atc(ATC_CATEGORY_2).build()
        )
        assertEvaluation(EvaluationResult.PASS, MULTIPLE_CATEGORIES_FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassOnSameCategoryAndOneWithStableDosing() {
        val medications = listOf(
            TestMedicationFactory.createMinimal().dosage(fixedDosing()).atc(ATC_CATEGORY_1).build(),
            TestMedicationFactory.createMinimal().dosage(fixedDosing()).atc(ATC_CATEGORY_2).build(),
            TestMedicationFactory.createMinimal().dosage(ImmutableDosage.builder().from(fixedDosing()).frequencyUnit("other").build())
                .atc(ATC_CATEGORY_1).build()
        )
        assertEvaluation(EvaluationResult.PASS, MULTIPLE_CATEGORIES_FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenBothCategoriesHaveWrongDosing() {
        val medications = listOf(
            TestMedicationFactory.createMinimal().dosage(fixedDosing()).atc(ATC_CATEGORY_1).build(),
            TestMedicationFactory.createMinimal().dosage(fixedDosing()).atc(ATC_CATEGORY_2).build(),
            TestMedicationFactory.createMinimal().dosage(ImmutableDosage.builder().from(fixedDosing()).frequencyUnit("other").build())
                .atc(ATC_CATEGORY_1).build(),
            TestMedicationFactory.createMinimal().dosage(ImmutableDosage.builder().from(fixedDosing()).frequencyUnit("other").build())
                .atc(ATC_CATEGORY_2).build(),
        )
        assertEvaluation(EvaluationResult.FAIL, MULTIPLE_CATEGORIES_FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
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

        private val ATC_CATEGORY_1 = AtcTestFactory.atcClassification(CATEGORY_1)
        private val ATC_CATEGORY_2 = AtcTestFactory.atcClassification(CATEGORY_2)

        private val ONE_CATEGORY_FUNCTION = CurrentlyGetsStableMedicationOfCategory(
            MedicationTestFactory.alwaysActive(), mapOf(
                CATEGORY_1 to setOf(
                    AtcLevel(code = CATEGORY_1, name = "")
                )
            )
        )
        private val MULTIPLE_CATEGORIES_FUNCTION =
            CurrentlyGetsStableMedicationOfCategory(
                MedicationTestFactory.alwaysActive(),
                mapOf(
                    CATEGORY_1 to setOf(AtcLevel(code = CATEGORY_1, name = "")),
                    CATEGORY_2 to setOf(AtcLevel(code = CATEGORY_2, name = ""))
                )
            )
    }
}