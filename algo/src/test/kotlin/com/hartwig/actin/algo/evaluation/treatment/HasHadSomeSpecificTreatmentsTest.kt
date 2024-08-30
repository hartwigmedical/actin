package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadSomeSpecificTreatmentsTest {
    @Test
    fun `Should fail for empty treatments`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("wrong", true)))
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should pass for sufficient correct treatments`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment(MATCHING_TREATMENT_NAME, true)))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should return undetermined when trial entries without treatment meet threshold`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should fail when trial treatments meet threshold`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(
            EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val function =
            HasHadSomeSpecificTreatments(listOf(treatment(MATCHING_TREATMENT_NAME, false, setOf(TreatmentCategory.TRANSPLANTATION))), 2)
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    companion object {
        private const val MATCHING_TREATMENT_NAME = "treatment 1"
        private val FUNCTION = HasHadSomeSpecificTreatments(listOf(treatment(MATCHING_TREATMENT_NAME, true)), 2)
    }
}