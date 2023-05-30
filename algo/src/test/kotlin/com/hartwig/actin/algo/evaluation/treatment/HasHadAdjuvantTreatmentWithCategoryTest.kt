package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import org.junit.Test

class HasHadAdjuvantTreatmentWithCategoryTest {
    private val function = HasHadAdjuvantTreatmentWithCategory(CATEGORY)

    @Test
    fun shouldFailForEmptyTreatmentList() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(emptyList())))
    }

    @Test
    fun shouldFailForAdjuvantTreatmentNotMatchingCategory() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatments(
                    listOf(
                        TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).name("adjuvant treatment").build()
                    )
                )
            )
        )
    }

    @Test
    fun shouldFailForNonAdjuvantTreatmentMatchingCategory() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatments(
                    listOf(
                        TreatmentTestFactory.builder().addCategories(CATEGORY).name("typical treatment").build()
                    )
                )
            )
        )
    }

    @Test
    fun shouldFailForNeoadjuvantTreatmentMatchingCategory() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatments(
                    listOf(
                        TreatmentTestFactory.builder().addCategories(CATEGORY).name("neoadjuvant treatment").build()
                    )
                )
            )
        )
    }

    @Test
    fun shouldPassForAdjuvantTreatmentMatchingCategory() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatments(
                    listOf(
                        TreatmentTestFactory.builder().addCategories(CATEGORY).name("adjuvant treatment").build()
                    )
                )
            )
        )
    }

    @Test
    fun shouldPassForNeoadjuvantAndAdjuvantTreatmentMatchingCategory() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                TreatmentTestFactory.withPriorTumorTreatments(
                    listOf(
                        TreatmentTestFactory.builder().addCategories(CATEGORY).name("neoadjuvant and adjuvant treatment").build()
                    )
                )
            )
        )
    }

    companion object {
        private val CATEGORY = TreatmentCategory.TARGETED_THERAPY
    }
}