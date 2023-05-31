package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import org.junit.Test

class HasHadAdjuvantTreatmentWithCategoryTest {
    private val categoryFunction = HasHadAdjuvantTreatmentWithCategory(CATEGORY, null)
    private val specificFunction = HasHadAdjuvantTreatmentWithCategory(CATEGORY, setOf("specific", "another"))

    @Test
    fun shouldFailForEmptyTreatmentList() {
        assertEvaluation(EvaluationResult.FAIL, categoryFunction.evaluate(TreatmentTestFactory.withPriorTumorTreatments(emptyList())))
    }

    @Test
    fun shouldFailForAdjuvantTreatmentNotMatchingCategory() {
        assertResultForCategoryAndName(EvaluationResult.FAIL, TreatmentCategory.IMMUNOTHERAPY, "adjuvant treatment")
    }

    @Test
    fun shouldFailForNonAdjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndName(EvaluationResult.FAIL, CATEGORY, "typical treatment")
    }

    @Test
    fun shouldFailForNeoadjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndName(EvaluationResult.FAIL, CATEGORY, "neoadjuvant treatment")
    }

    @Test
    fun shouldPassForAdjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndName(EvaluationResult.PASS, CATEGORY, "adjuvant treatment")
    }

    @Test
    fun shouldPassForNeoadjuvantAndAdjuvantTreatmentMatchingCategory() {
        assertResultForCategoryAndName(EvaluationResult.PASS, CATEGORY, "neoadjuvant and adjuvant treatment")
    }

    @Test
    fun shouldFailForNonAdjuvantTreatmentMatchingType() {
        assertResultForCategoryAndTypeAndName(EvaluationResult.FAIL, specificFunction, CATEGORY, "another", "treatment")
    }

    @Test
    fun shouldFailForAdjuvantTreatmentMatchingTypeButNotCategory() {
        assertResultForCategoryAndTypeAndName(
            EvaluationResult.FAIL,
            specificFunction,
            TreatmentCategory.IMMUNOTHERAPY,
            "another",
            "adjuvant treatment"
        )
    }

    @Test
    fun shouldPassForAdjuvantTreatmentMatchingType() {
        assertResultForCategoryAndTypeAndName(EvaluationResult.PASS, specificFunction, CATEGORY, "another", "adjuvant treatment")
    }

    @Test
    fun shouldFailForAdjuvantTreatmentMatchingCategoryButNotType() {
        assertResultForCategoryAndTypeAndName(EvaluationResult.FAIL, specificFunction, CATEGORY, "unrelated", "adjuvant treatment")
    }

    @Test
    fun shouldWarnForAdjuvantTreatmentMatchingCategoryWithNoType() {
        assertResultForCategoryAndTypeAndName(EvaluationResult.WARN, specificFunction, CATEGORY, "", "adjuvant treatment")
    }

    private fun assertResultForCategoryAndName(expectedResult: EvaluationResult, category: TreatmentCategory, name: String) {
        assertResultForCategoryAndTypeAndName(expectedResult, categoryFunction, category, "", name)
    }

    private fun assertResultForCategoryAndTypeAndName(
        expectedResult: EvaluationResult,
        function: HasHadAdjuvantTreatmentWithCategory,
        category: TreatmentCategory,
        type: String,
        name: String
    ) {
        assertEvaluation(
            expectedResult, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatments(
                    listOf(
                        TreatmentTestFactory.builder().addCategories(category).targetedType(type).name(name).build()
                    )
                )
            )
        )
    }

    companion object {
        private val CATEGORY = TreatmentCategory.TARGETED_THERAPY
    }
}