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

    private val functionWithoutCategoriesToIgnore = HasHadAnyCancerTreatment(emptySet(), setOf(ATC_LEVELS))
    private val functionWithCategoriesToIgnore = HasHadAnyCancerTreatment(setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.HORMONE_THERAPY), setOf(ATC_LEVELS))

    @Test
    fun `Should fail when treatment history is empty`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutCategoriesToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithCategoriesToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }

    @Test
    fun `Should pass if treatment history is not empty and contains treatments which should not be ignored`() {
        val treatments = TreatmentTestFactory.treatment("Radiotherapy", false, setOf(TreatmentCategory.RADIOTHERAPY))
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatments)))
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutCategoriesToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithCategoriesToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should pass if treatment history is not empty and contains treatment that should not be ignored`() {
        val treatment1 = TreatmentTestFactory.treatment("Radiotherapy", false, setOf(TreatmentCategory.RADIOTHERAPY))
        val treatment2 = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatment1, treatment2)))
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutCategoriesToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithCategoriesToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
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
        listOf(functionWithCategoriesToIgnore, functionWithoutCategoriesToIgnore).forEach { function ->
            assertEvaluation(
                EvaluationResult.PASS,
                function.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, medications))
            )
        }
    }

    @Test
    fun `Should fail if treatment history contains only treatments which should be ignored`() {
        val treatment1 = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
        val treatment2 = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.HORMONE_THERAPY))
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatment1, treatment2)))
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithCategoriesToIgnore.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should evaluate to undetermined if medication entry contains trial`() {
        val medications = listOf(WashoutTestFactory.medication(isTrialMedication = true))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithCategoriesToIgnore.evaluate(WashoutTestFactory.withMedications(medications))
        )
    }
}