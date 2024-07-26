package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import org.junit.Test

class HasHadSomeTreatmentsWithCategoryWithIntentsTest {

    @Test
    fun `Should fail for no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should fail for correct treatment category with wrong intent`() {
        val treatment = treatment("matching category", isSystemic = true, categories = setOf(MATCHING_CATEGORY))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = setOf(Intent.CONSOLIDATION)
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(patientRecord))
    }

    @Test
    fun `Should pass when treatments with correct category and intent`() {
        val treatment = treatment("matching category", isSystemic = true, categories = setOf(MATCHING_CATEGORY))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment),
                    intents = MATCHING_INTENT_SET
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(patientRecord))
    }

    @Test
    fun `Should return undetermined when treatments with correct category and no intent`() {
        val treatment = treatment("matching category", isSystemic = true, categories = setOf(MATCHING_CATEGORY))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment)
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(patientRecord))
    }

    @Test
    fun `Should return undetermined when trial treatments`() {
        val treatment = treatment("trial", isSystemic = true, categories = setOf(MATCHING_CATEGORY))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment), isTrial = true
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(patientRecord))
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val treatment = treatment("trial", isSystemic = true, categories = setOf(TreatmentCategory.TRANSPLANTATION))
        val patientRecord = withTreatmentHistory(
            listOf(
                treatmentHistoryEntry(
                    setOf(treatment)
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(patientRecord))
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val MATCHING_INTENT_SET = setOf(Intent.PALLIATIVE)
        private val FUNCTION = HasHadSomeTreatmentsWithCategoryWithIntents(MATCHING_CATEGORY, MATCHING_INTENT_SET)
    }
}