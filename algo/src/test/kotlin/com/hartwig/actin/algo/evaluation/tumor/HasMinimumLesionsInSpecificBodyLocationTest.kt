package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import org.junit.jupiter.api.Test

class HasMinimumLesionsInSpecificBodyLocationTest {

    private val evaluableCategory = BodyLocationCategory.LUNG
    private val nonEvaluableCategory = BodyLocationCategory.BLADDER
    private val functionRequiringTwoLesionsInEvaluableCategory = HasMinimumLesionsInSpecificBodyLocation(2, evaluableCategory)
    private val functionRequiringOneLesionInEvaluableCategory = HasMinimumLesionsInSpecificBodyLocation(1, evaluableCategory)
    private val functionRequiringOneLesionInNonEvaluableCategory = HasMinimumLesionsInSpecificBodyLocation(1, nonEvaluableCategory)

    @Test
    fun `Should pass in case of known lesions in requested body location and requiring at most one lesion`() {
        assertEvaluation(
            EvaluationResult.PASS,
            functionRequiringOneLesionInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = true
                )
            ),
            "At least 1 lesions in lung"
        )
    }

    @Test
    fun `Should be undetermined in case of known lesions in requested body location and requiring at least two lesions`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionRequiringTwoLesionsInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = true
                )
            ),
            "Undetermined if at least 2 lesions in lung based on provided lesions"
        )
    }

    @Test
    fun `Should be undetermined in case of suspected lesions whether requiring at least one or two lesions`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionRequiringOneLesionInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = false,
                    hasSuspectedLungLesions = true
                )
            ),
            "Undetermined if at least 1 lesions in lung based on provided lesions"
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionRequiringTwoLesionsInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = false,
                    hasSuspectedLungLesions = true
                )
            ),
            "Undetermined if at least 2 lesions in lung based on provided lesions"
        )
    }

    @Test
    fun `Should be undetermined in case of missing known lesions information whether requiring at least one or two lesions`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionRequiringOneLesionInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = null
                )
            ),
            "Undetermined if at least 1 lesions in lung based on provided lesions"
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionRequiringTwoLesionsInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = null
                )
            ),
            "Undetermined if at least 2 lesions in lung based on provided lesions"
        )
    }

    @Test
    fun `Should fail for neither known or suspected lesions in requested body location whether requiring one or two lesions`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionRequiringOneLesionInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = false,
                    hasSuspectedLungLesions = false
                )
            ),
            "Fewer than 1 lesions in lung in provided lesions"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionRequiringTwoLesionsInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = false,
                    hasSuspectedLungLesions = false
                )
            ),
            "Fewer than 2 lesions in lung in provided lesions"
        )
    }

    @Test
    fun `Should fail for no known lesions also if data on suspected lesions is missing  whether requiring one or two lesions`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionRequiringOneLesionInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(hasLungLesions = false, hasSuspectedLungLesions = null)
            ),
            "Fewer than 1 lesions in lung in provided lesions"
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionRequiringTwoLesionsInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(hasLungLesions = false, hasSuspectedLungLesions = null)
            ),
            "Fewer than 2 lesions in lung in provided lesions"
        )
    }

    @Test
    fun `Should evaluate to undetermined if requested body location is of other type than bone, brain, cns, liver, lung or lymph node`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionRequiringOneLesionInNonEvaluableCategory.evaluate(TumorTestFactory.withOtherLesions(listOf("one", "two"))),
            "Undetermined if at least 1 lesions in bladder based on provided lesions"
        )
    }
}