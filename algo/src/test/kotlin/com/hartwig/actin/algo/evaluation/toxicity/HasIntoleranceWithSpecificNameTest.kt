package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasIntoleranceWithSpecificNameTest {
    private val function = HasIntoleranceWithSpecificName("allergy")

    @Test
    fun `Should fail with no intolerances`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withIntolerances(emptyList())))
    }

    @Test
    fun `Should fail with intolerance that does not match`() {
        val mismatch = ComorbidityTestFactory.intolerance(name = "mismatch")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withComorbidity(mismatch)))
    }

    @Test
    fun `Should pass with intolerance that matches`() {
        val match = ComorbidityTestFactory.intolerance(name = "matching allergy")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withComorbidity(match)))
    }
}