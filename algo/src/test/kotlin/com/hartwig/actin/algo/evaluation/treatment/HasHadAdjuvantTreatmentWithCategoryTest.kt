package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import org.junit.Test

class HasHadAdjuvantTreatmentWithCategoryTest {

    @Test
    fun shouldFailForEmptyTreatmentList() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailForAdjuvantTreatmentNotMatchingCategory() {
        assertResultForCategoryAndIntent(EvaluationResult.FAIL, TreatmentCategory.IMMUNOTHERAPY, setOf(Intent.ADJUVANT))
    }

    @Test
    fun shouldFailForNonAdjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndIntent(EvaluationResult.FAIL, MATCH_CATEGORY, emptySet())
    }

    @Test
    fun shouldFailForNeoadjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndIntent(EvaluationResult.FAIL, MATCH_CATEGORY, setOf(Intent.NEOADJUVANT))
    }

    @Test
    fun shouldPassForAdjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndIntent(EvaluationResult.PASS, MATCH_CATEGORY, setOf(Intent.ADJUVANT))
    }

    @Test
    fun shouldPassForNeoadjuvantAndAdjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndIntent(EvaluationResult.PASS, MATCH_CATEGORY, setOf(Intent.NEOADJUVANT, Intent.ADJUVANT))
    }

    private fun assertResultForCategoryAndIntent(
        expectedResult: EvaluationResult,
        category: TreatmentCategory,
        intents: Set<Intent>
    ) {
        val treatment = TreatmentTestFactory.drugTherapy("drug therapy", category)
        val record =
            TreatmentTestFactory.withTreatmentHistoryEntry(TreatmentTestFactory.treatmentHistoryEntry(setOf(treatment), intents = intents))
        assertEvaluation(expectedResult, FUNCTION.evaluate(record))
    }

    companion object {
        private val MATCH_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val FUNCTION = HasHadAdjuvantTreatmentWithCategory(MATCH_CATEGORY)
    }
}