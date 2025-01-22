package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.Test

class HasHadLimitedTreatmentsWithCategoryTest {

    @Test
    fun `Should pass (treatment optional) or fail (treatment required) in case patient had no treatments`() {
        evaluateFunctions(EvaluationResult.PASS, EvaluationResult.FAIL, TYPICAL_REQUIRED_CATEGORY, emptyList())
    }

    @Test
    fun `Should pass (treatment optional) or fail (treatment required) if patient has had only treatment of wrong category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        evaluateFunctions(EvaluationResult.PASS, EvaluationResult.FAIL, TYPICAL_REQUIRED_CATEGORY, listOf(treatmentHistoryEntry))
    }

    @Test
    fun `Should pass (both functions) when treatments with correct category within limit`() {
        evaluateFunctions(EvaluationResult.PASS, EvaluationResult.PASS, TYPICAL_REQUIRED_CATEGORY, listOf(MATCHING_TREATMENT))
    }

    @Test
    fun `Should fail (both functions) when treatments with correct category exceed limit`() {
        evaluateFunctions(
            EvaluationResult.FAIL,
            EvaluationResult.FAIL,
            TYPICAL_REQUIRED_CATEGORY,
            listOf(MATCHING_TREATMENT, MATCHING_TREATMENT)
        )
    }

    @Test
    fun `Should return pass (treatment optional) or undetermined (treatment required) if there is a potentially matching trial option within the limit`() {
        evaluateFunctions(
            EvaluationResult.PASS,
            EvaluationResult.UNDETERMINED,
            TYPICAL_REQUIRED_CATEGORY,
            listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY)
        )
    }

    @Test
    fun `Should return undetermined (both functions) when possibly matching trial options could exceed limit`() {
        evaluateFunctions(
            EvaluationResult.UNDETERMINED,
            EvaluationResult.UNDETERMINED,
            TYPICAL_REQUIRED_CATEGORY,
            listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY, TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY)
        )
    }

    @Test
    fun `Should ignore trial matches and pass (treatment optional) or fail (treatment required) when looking for unlikely trial categories`() {
        evaluateFunctions(
            EvaluationResult.PASS,
            EvaluationResult.FAIL,
            RARE_REQUIRED_CATEGORY,
            listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY, TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY)
        )
    }

    private fun evaluateFunctions(
        expectedResultTreatmentOptional: EvaluationResult,
        expectedResultTreatmentRequired: EvaluationResult,
        treatmentCategory: TreatmentCategory,
        treatmentList: List<TreatmentHistoryEntry>
    ) {
        assertEvaluation(
            expectedResultTreatmentOptional,
            HasHadLimitedTreatmentsWithCategory(treatmentCategory, 1, false).evaluate(withTreatmentHistory(treatmentList))
        )
        assertEvaluation(
            expectedResultTreatmentRequired,
            HasHadLimitedTreatmentsWithCategory(treatmentCategory, 1, true).evaluate(withTreatmentHistory(treatmentList))
        )
    }

    companion object {
        private val TYPICAL_REQUIRED_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val RARE_REQUIRED_CATEGORY = TreatmentCategory.TRANSPLANTATION
        private val MATCHING_TREATMENT = treatmentHistoryEntry(setOf(drugTreatment("test", TYPICAL_REQUIRED_CATEGORY)))
        private val TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY =
            treatmentHistoryEntry(setOf(treatment("trial", true, emptySet())), isTrial = true)
    }
}