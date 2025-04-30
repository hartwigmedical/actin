package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import org.junit.Test

class HasMinimumLesionsInSpecificBodyLocationTest {

    private val functionRequiringTwoLesionsInEvaluableCategory = HasMinimumLesionsInSpecificBodyLocation(2, BodyLocationCategory.LUNG)
    private val functionRequiringOneLesionInEvaluableCategory = HasMinimumLesionsInSpecificBodyLocation(1, BodyLocationCategory.LUNG)
    private val functionRequiringOneLesionInNonEvaluableCategory = HasMinimumLesionsInSpecificBodyLocation(1, BodyLocationCategory.BLADDER)

    @Test
    fun `Should be pass in case of known lesions in requested body location and requiring at most one lesion`() {
        assertEvaluation(
            EvaluationResult.PASS,
            functionRequiringOneLesionInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = true
                )
            )
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
            )
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
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionRequiringTwoLesionsInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = false,
                    hasSuspectedLungLesions = true
                )
            )
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
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionRequiringTwoLesionsInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = null
                )
            )
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
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionRequiringTwoLesionsInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = false,
                    hasSuspectedLungLesions = false
                )
            )
        )
    }

    @Test
    fun `Should fail for no known lesions also if data on suspected lesions is missing  whether requiring one or two lesions`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionRequiringOneLesionInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(hasLungLesions = false, hasSuspectedLungLesions = null)
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionRequiringTwoLesionsInEvaluableCategory.evaluate(
                TumorTestFactory.withLungLesions(hasLungLesions = false, hasSuspectedLungLesions = null)
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if requested body location is of other type than bone, brain, cns, liver, lung or lymph node`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionRequiringOneLesionInNonEvaluableCategory.evaluate(TumorTestFactory.withOtherLesions(listOf("one", "two")))
        )
    }
}