package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.Dosage
import org.assertj.core.api.Assertions
import org.junit.Test

private const val CATEGORY_1 = "category 1"
private const val CATEGORY_2 = "category 2"

class CurrentlyGetsStableMedicationOfCategoryTest {
    private val fixedDosing = Dosage(
        dosageMin = 1.0,
        dosageMax = 2.0,
        dosageUnit = "unit 1",
        frequency = 3.0,
        frequencyUnit = "unit 2",
        ifNeeded = false
    )

    private val atcCategory1 = AtcTestFactory.atcClassification(CATEGORY_1)
    private val atcCategory2 = AtcTestFactory.atcClassification(CATEGORY_2)

    private val oneCategoryFunction = CurrentlyGetsStableMedicationOfCategory(
        MedicationTestFactory.alwaysActive(), mapOf(CATEGORY_1 to setOf(AtcLevel(code = CATEGORY_1, name = "")))
    )

    private val multipleCategoriesFunction = CurrentlyGetsStableMedicationOfCategory(
        MedicationTestFactory.alwaysActive(),
        mapOf(
            CATEGORY_1 to setOf(AtcLevel(code = CATEGORY_1, name = "")),
            CATEGORY_2 to setOf(AtcLevel(code = CATEGORY_2, name = ""))
        )
    )

    @Test
    fun `Should fail when no medication`() {
        assertEvaluation(EvaluationResult.FAIL, oneCategoryFunction.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should pass when single medication with dosing`() {
        val medications = listOf(MedicationTestFactory.medication(dosage = fixedDosing, atc = atcCategory1))
        assertEvaluation(EvaluationResult.PASS, oneCategoryFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when another medication with no category and same dosing`() {
        val medications = listOf(
            MedicationTestFactory.medication(dosage = fixedDosing, atc = atcCategory1),
            MedicationTestFactory.medication(dosage = fixedDosing)
        )
        assertEvaluation(EvaluationResult.PASS, oneCategoryFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when same category and other dosing`() {
        val medications = listOf(
            MedicationTestFactory.medication(dosage = fixedDosing, atc = atcCategory1),
            MedicationTestFactory.medication(dosage = fixedDosing),
            MedicationTestFactory.medication(dosage = fixedDosing.copy(frequencyUnit = "other"), atc = atcCategory1)
        )
        assertEvaluation(EvaluationResult.FAIL, oneCategoryFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when dosing is combined with medication without dosing`() {
        val medications = listOf(MedicationTestFactory.medication(dosage = fixedDosing), MedicationTestFactory.medication())
        assertEvaluation(EvaluationResult.FAIL, oneCategoryFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass with multiple medications with dosing`() {
        val medications = listOf(
            MedicationTestFactory.medication(dosage = fixedDosing, atc = atcCategory1),
            MedicationTestFactory.medication(dosage = fixedDosing, atc = atcCategory2)
        )
        assertEvaluation(EvaluationResult.PASS, multipleCategoriesFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass on same category and one with stable dosing`() {
        val medications = listOf(
            MedicationTestFactory.medication(dosage = fixedDosing, atc = atcCategory1),
            MedicationTestFactory.medication(dosage = fixedDosing, atc = atcCategory2),
            MedicationTestFactory.medication(dosage = fixedDosing.copy(frequencyUnit = "other"), atc = atcCategory1)
        )
        assertEvaluation(EvaluationResult.PASS, multipleCategoriesFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when both categories have wrong dosing`() {
        val medications = listOf(
            MedicationTestFactory.medication(dosage = fixedDosing, atc = atcCategory1),
            MedicationTestFactory.medication(dosage = fixedDosing, atc = atcCategory2),
            MedicationTestFactory.medication(dosage = fixedDosing.copy(frequencyUnit = "other"), atc = atcCategory1),
            MedicationTestFactory.medication(dosage = fixedDosing.copy(frequencyUnit = "other"), atc = atcCategory2)
        )
        assertEvaluation(EvaluationResult.FAIL, multipleCategoriesFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should be undetermined if medication is not provided`() {
        val medicationNotProvided = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = null)
        val alwaysPlannedResult = oneCategoryFunction.evaluate(medicationNotProvided)
        assertEvaluation(EvaluationResult.UNDETERMINED, alwaysPlannedResult)
        Assertions.assertThat(alwaysPlannedResult.recoverable).isTrue()
        val alwaysActiveResult = multipleCategoriesFunction.evaluate(medicationNotProvided)
        assertEvaluation(EvaluationResult.UNDETERMINED, alwaysActiveResult)
        Assertions.assertThat(alwaysActiveResult.recoverable).isTrue()
    }
}