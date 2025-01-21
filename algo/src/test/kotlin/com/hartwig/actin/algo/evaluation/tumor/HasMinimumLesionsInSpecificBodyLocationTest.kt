package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import org.junit.Test

private const val REQUESTED_LESIONS = 2

class HasMinimumLesionsInSpecificBodyLocationTest {

    private val function = HasMinimumLesionsInSpecificBodyLocation(REQUESTED_LESIONS, BodyLocationCategory.LUNG)
    private val bladderLesionFunction = HasMinimumLesionsInSpecificBodyLocation(2, BodyLocationCategory.BLADDER)

    @Test
    fun `Should pass for correct number of lesions in requested body location`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = true, count = REQUESTED_LESIONS))
        )
    }

    @Test
    fun `Should assume that there is one lesion when lesions present in requested location but count is unknown`() {
        val function = HasMinimumLesionsInSpecificBodyLocation(1, BodyLocationCategory.LUNG)
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = true, count = null))
        )
    }

    @Test
    fun `Should fail for too small number of lesions in requested body location`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = true, count = REQUESTED_LESIONS.minus(1)))
        )
    }

    @Test
    fun `Should fail for no lesions in requested body location`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = false, count = null))
        )
    }

    @Test
    fun `Should resolve to undetermined when data on presence and count of requested lesion is missing and requested minimum is more than zero`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = null, hasSuspectedLungLesions = null, count = null))
        )
    }

    @Test
    fun `Should pass when data on presence and count of requested lesion is missing but requested minimum is zero`() {
        val functionRequestingZeroLesions = HasMinimumLesionsInSpecificBodyLocation(0, BodyLocationCategory.LUNG)
        assertEvaluation(
            EvaluationResult.PASS,
            functionRequestingZeroLesions.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = null, hasSuspectedLungLesions = null))
        )
    }

    @Test
    fun `Should evaluate to undetermined for suspected lesions in requested body location regardless the known lesion count`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = null, hasSuspectedLungLesions = true, count = 1))
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