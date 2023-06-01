package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import org.junit.Test

class HasHadAdjuvantTreatmentWithCategoryTest {
    private val function = HasHadAdjuvantTreatmentWithCategory(MATCH_CATEGORY)

    @Test
    fun shouldFailForEmptyTreatmentList() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(emptyList())))
    }

    @Test
    fun shouldFailForAdjuvantTreatmentNotMatchingCategory() {
        assertResultForCategoryAndName(EvaluationResult.FAIL, OTHER_CATEGORY, "adjuvant treatment")
    }

    @Test
    fun shouldFailForNonAdjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndName(EvaluationResult.FAIL, MATCH_CATEGORY, "typical treatment")
    }

    @Test
    fun shouldFailForNeoadjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndName(EvaluationResult.FAIL, MATCH_CATEGORY, "neoadjuvant treatment")
    }

    @Test
    fun shouldPassForAdjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndName(EvaluationResult.PASS, MATCH_CATEGORY, "adjuvant treatment")
    }

    @Test
    fun shouldPassForNeoadjuvantAndAdjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndName(EvaluationResult.PASS, MATCH_CATEGORY, "neoadjuvant and adjuvant treatment")
    }

    private fun assertResultForCategoryAndName(expectedResult: EvaluationResult, category: TreatmentCategory, name: String) {
        assertEvaluation(
            expectedResult, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatments(
                    listOf(
                        TreatmentTestFactory.builder().addCategories(category).name(name).build()
                    )
                )
            )
        )
    }

    companion object {
        private val MATCH_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val OTHER_CATEGORY = TreatmentCategory.IMMUNOTHERAPY
    }
}