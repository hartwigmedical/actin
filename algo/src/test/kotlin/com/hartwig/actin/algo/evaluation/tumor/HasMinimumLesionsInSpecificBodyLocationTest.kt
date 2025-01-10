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
    fun `Should pass for correct amount of lesions in requested body location`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = true, count = REQUESTED_LESIONS))
        )
    }

    @Test
    fun `Should fail for too small amount of lesions in requested body location`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = true, count = REQUESTED_LESIONS.minus(1)))
        )
    }

    @Test
    fun `Should fail for no lesions in requested body location`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = false))
        )
    }

    @Test
    fun `Should evaluate to undetermined for unknown amount of lesions in requested body location`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = true, count = null))
        )
    }

    @Test
    fun `Should evaluate to undetermined when data on requested lesions is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = null, hasSuspectedLungLesions = null))
        )
    }

    @Test
    fun `Should evaluate to undetermined for suspected lesions in requested body location regardless the known lesion count`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withLungLesions(hasLungLesions = false, hasSuspectedLungLesions = true, count = 1))
        )
    }

    @Test
    fun `Should fail for less than requested otherLesions when requested location is of other type than bone, brain, cns, liver, lung or lymph node`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            bladderLesionFunction.evaluate(TumorTestFactory.withOtherLesions(listOf("one other lesion")))
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