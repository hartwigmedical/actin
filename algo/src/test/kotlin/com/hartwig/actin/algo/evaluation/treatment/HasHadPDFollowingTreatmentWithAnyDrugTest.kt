package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.junit.jupiter.api.Test

class HasHadPDFollowingTreatmentWithAnyDrugTest {

    @Test
    fun `Should fail for empty treatments`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for other treatment with PD`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(NON_MATCHING_TREATMENTS, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for matching treatment but no PD`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            MATCHING_TREATMENTS,
            stopReason = StopReason.TOXICITY,
            bestResponse = TreatmentResponse.MIXED
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for matching treatment missing stop reason`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENTS)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for matching treatment with no stop reason but subsequent treatment line within 26 weeks`() {
        val matchingEntry = treatmentHistoryEntry(MATCHING_TREATMENTS, stopYear = 2020, stopMonth = 6)
        val subsequentEntry = treatmentHistoryEntry(NON_MATCHING_TREATMENTS, startYear = 2020, startMonth = 9)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(listOf(matchingEntry, subsequentEntry))))
    }

    @Test
    fun `Should return undetermined for matching treatment with no stop reason when gap to next line exceeds 26 weeks`() {
        val matchingEntry = treatmentHistoryEntry(MATCHING_TREATMENTS, stopYear = 2020, stopMonth = 6)
        val subsequentEntry = treatmentHistoryEntry(NON_MATCHING_TREATMENTS, startYear = 2021, startMonth = 1)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistory(listOf(matchingEntry, subsequentEntry))))
    }

    @Test
    fun `Should fail for matching treatment stopped due to toxicity even if subsequent line exists`() {
        val matchingEntry = treatmentHistoryEntry(MATCHING_TREATMENTS, stopReason = StopReason.TOXICITY, stopYear = 2020, stopMonth = 6)
        val subsequentEntry = treatmentHistoryEntry(NON_MATCHING_TREATMENTS, startYear = 2020, startMonth = 9)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(matchingEntry, subsequentEntry))))
    }

    @Test
    fun `Should pass for matching treatment and stop reason PD`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENTS, stopReason = StopReason.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for matching treatment when PD is indicated in best response`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(MATCHING_TREATMENTS, bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail with trial treatment entry with different category in history`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.TARGETED_THERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined with uncategorized trial treatment entry in history`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(DrugTreatment("test", emptySet())), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    companion object {
        private val MATCHING_DRUGS = setOf(Drug("drug", emptySet(), emptySet(), TreatmentCategory.CHEMOTHERAPY))
        private val MATCHING_TREATMENTS = listOf(DrugTreatment("treatment", MATCHING_DRUGS))
        private val NON_MATCHING_TREATMENTS = setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY))
        private val FUNCTION = HasHadPDFollowingTreatmentWithAnyDrug(MATCHING_DRUGS)
    }
}