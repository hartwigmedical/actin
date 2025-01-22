package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadLimitedTreatmentsWithCategoryTest {

    @Test
    fun `Should pass (treatment optional) or fail (treatment required) in case patient had no treatments`() {
        assertEvaluation(EvaluationResult.PASS, FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(emptyList())))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should pass (treatment optional) or fail (treatment required) if patient has had only treatment of wrong category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should pass (both functions) when treatments with correct category within limit`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }


    @Test
    fun `Should fail (both functions) when treatments with correct category exceed limit`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)))
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should return pass (treatment optional) or undetermined (treatment required) if there is a potentially matching trial option within the limit`() {
        assertEvaluation(
            EvaluationResult.PASS,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(withTreatmentHistory(listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY)))
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_TREATMENT_REQUIRED.evaluate(withTreatmentHistory(listOf(TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY)))
        )
    }

    @Test
    fun `Should return undetermined (both functions) when possibly matching trial options could exceed limit`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_TREATMENT_OPTIONAL.evaluate(
                withTreatmentHistory(
                    listOf(
                        TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY,
                        TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY
                    )
                )
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_TREATMENT_REQUIRED.evaluate(
                withTreatmentHistory(
                    listOf(
                        TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY,
                        TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY
                    )
                )
            )
        )
    }

    @Test
    fun `Should ignore trial matches and pass (treatment optional) or fail (treatment required) when looking for unlikely trial categories`() {
        assertEvaluation(
            EvaluationResult.PASS,
            HasHadLimitedTreatmentsWithCategory(TreatmentCategory.TRANSPLANTATION, 1, false).evaluate(
                withTreatmentHistory(
                    listOf(
                        TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY,
                        TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY
                    )
                )
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            HasHadLimitedTreatmentsWithCategory(TreatmentCategory.TRANSPLANTATION, 1, true).evaluate(
                withTreatmentHistory(
                    listOf(
                        TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY,
                        TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY
                    )
                )
            )
        )
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val TRIAL_TREATMENT_WITH_UNKNOWN_CATEGORY =
            treatmentHistoryEntry(setOf(treatment("trial", true, emptySet())), isTrial = true)
        private val FUNCTION_TREATMENT_REQUIRED = HasHadLimitedTreatmentsWithCategory(MATCHING_CATEGORY, 1, true)
        private val FUNCTION_TREATMENT_OPTIONAL = HasHadLimitedTreatmentsWithCategory(MATCHING_CATEGORY, 1, false)
    }
}