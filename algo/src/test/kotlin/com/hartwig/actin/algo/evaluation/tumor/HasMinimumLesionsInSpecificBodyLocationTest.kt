package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import org.junit.Test

private const val REQUESTED_LESIONS = 2

class HasMinimumLesionsInSpecificBodyLocationTest {

    private val function = HasMinimumLesionsInSpecificBodyLocation(REQUESTED_LESIONS, BodyLocationCategory.LUNG)
    private val bladderLesionFunction = HasMinimumLesionsInSpecificBodyLocation(REQUESTED_LESIONS, BodyLocationCategory.BLADDER)

    @Test
    fun `Should pass for correct number of lesions in requested body location in case of known lesions`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = true,
                    hasSuspectedLungLesions = null,
                    minCount = REQUESTED_LESIONS
                )
            )
        )
    }

    @Test
    fun `Should pass for correct number of lesions in requested body location in case of suspected lesions`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = false,
                    hasSuspectedLungLesions = true,
                    minCount = REQUESTED_LESIONS
                )
            )
        )
    }

    @Test
    fun `Should be undetermined for too small number of lesions in requested body location in case of known lesions`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = true,
                    hasSuspectedLungLesions = null,
                    minCount = REQUESTED_LESIONS.minus(1)
                )
            )
        )
    }

    @Test
    fun `Should be undetermined for too small number of lesions in requested body location in case of suspect lesions`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = false,
                    hasSuspectedLungLesions = true,
                    minCount = REQUESTED_LESIONS.minus(1)
                )
            )
        )
    }

    @Test
    fun `Should fail for neither known or suspected lesions in requested body location`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = false, hasSuspectedLungLesions = false, minCount = null))
        )
    }

    @Test
    fun `Should fail for no known lesions also if data on suspected lesions is missing`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = false, hasSuspectedLungLesions = null, minCount = null))
        )
    }

    @Test
    fun `Should be undetermined when data on known lesions is missing and requested minimum is more than zero`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = null, hasSuspectedLungLesions = null, minCount = null))
        )
    }

    @Test
    fun `Should pass when data when data on known lesions is missing but requested minimum is zero`() {
        val functionRequestingZeroLesions = HasMinimumLesionsInSpecificBodyLocation(0, BodyLocationCategory.LUNG)
        assertEvaluation(
            EvaluationResult.PASS,
            functionRequestingZeroLesions.evaluate(
                TumorTestFactory.withLungLesions(
                    hasLungLesions = null,
                    hasSuspectedLungLesions = null,
                    minCount = null
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if requested body location is of other type than bone, brain, cns, liver, lung or lymph node and number of other lesions is sufficient`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            bladderLesionFunction.evaluate(TumorTestFactory.withOtherLesions(listOf("one", "two")))
        )
    }
}