package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private val ATC_LEVELS = AtcLevel(code = "category to find", name = "")

class HasHadAnyCancerTreatmentTest {

    private val functionWithoutCategoryToIgnore = HasHadAnyCancerTreatment(null, setOf(ATC_LEVELS))
    private val functionWithCategoryToIgnore = HasHadAnyCancerTreatment(TreatmentCategory.CHEMOTHERAPY, setOf(ATC_LEVELS))

    @Test
    fun `Should fail when treatment history is empty`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutCategoryToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithCategoryToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }

    @Test
    fun `Should pass if treatment history is not empty and contains treatments which should not be ignored`() {
        val treatments = TreatmentTestFactory.treatment("Radiotherapy", true, setOf(TreatmentCategory.RADIOTHERAPY))
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatments)))
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutCategoryToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithCategoryToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should pass if treatment history contains only treatments which should be ignored but medication entry present with category that should not be ignored`() {
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatments)))
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(
            WashoutTestFactory.medication(atc, null)
                .copy(drug = Drug(name = "", category = TreatmentCategory.IMMUNOTHERAPY, drugTypes = setOf(DrugType.ANTI_TISSUE_FACTOR)))
        )
        listOf(functionWithCategoryToIgnore, functionWithoutCategoryToIgnore).forEach { function ->
            assertEvaluation(
                EvaluationResult.PASS,
                function.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, medications))
            )
        }
    }

    @Test
    fun `Should fail if treatment history contains only treatments which should be ignored`() {
        val treatments = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatments)))
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithCategoryToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should evaluate to undetermined if medication entry contains trial`() {
        val medications = listOf(WashoutTestFactory.medication(isTrialMedication = true))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithCategoryToIgnore.evaluate(WashoutTestFactory.withMedications(medications))
        )
    }
}