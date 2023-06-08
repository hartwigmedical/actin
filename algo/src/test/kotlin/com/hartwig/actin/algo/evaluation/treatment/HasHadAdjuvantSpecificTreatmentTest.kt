package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadAdjuvantSpecificTreatmentTest {

    private val function = HasHadAdjuvantSpecificTreatment(MATCH_NAMES, WARN_CATEGORY)

    @Test
    fun shouldFailForEmptyTreatmentList() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(emptyList()))
        )
    }

    @Test
    fun shouldFailForAdjuvantTreatmentNotMatchingCategoryOrName() {
        assertResultForCategoryAndName(EvaluationResult.FAIL, OTHER_CATEGORY, "adjuvant treatment")
    }

    @Test
    fun shouldFailForNonAdjuvantTreatmentMatchingCategoryAndName() {
        assertResultForCategoryAndName(EvaluationResult.FAIL, WARN_CATEGORY, "typical targeted treatment 1")
    }

    @Test
    fun shouldFailForNeoadjuvantTreatmentMatchingCategoryAndName() {
        assertResultForCategoryAndName(EvaluationResult.FAIL, WARN_CATEGORY, "neoadjuvant targeted treatment 1")
    }

    @Test
    fun shouldWarnForAdjuvantTreatmentMatchingCategoryButNotName() {
        assertResultForCategoryAndName(EvaluationResult.WARN, WARN_CATEGORY, "adjuvant generic targeted treatment")
    }

    @Test
    fun shouldPassForAdjuvantTreatmentMatchingCategoryAndName() {
        assertResultForCategoryAndName(EvaluationResult.PASS, WARN_CATEGORY, "adjuvant targeted treatment 2")
    }

    @Test
    fun shouldPassForNeoadjuvantAndAdjuvantTreatmentMatchingCategoryAndName() {
        assertResultForCategoryAndName(EvaluationResult.PASS, WARN_CATEGORY, "neoadjuvant and adjuvant targeted treatment 1")
    }

    private fun assertResultForCategoryAndName(expectedResult: EvaluationResult, category: TreatmentCategory, name: String) {
        EvaluationAssert.assertEvaluation(
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
        private val MATCH_NAMES = setOf("targeted treatment 1", "targeted treatment 2")

        private val WARN_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val OTHER_CATEGORY = TreatmentCategory.IMMUNOTHERAPY
    }
}

