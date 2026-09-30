package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.comorbidity.ComorbidityTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasIntoleranceWithSpecificNameTest {
    
    private val function = HasIntoleranceWithSpecificName("allergy")

    @Test
    fun `Should fail with no intolerances`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withIntolerances(emptyList())),
            "No allergies with name allergy"
        )
    }

    @Test
    fun `Should fail with intolerance that does not match`() {
        val mismatch = ComorbidityTestFactory.intolerance(name = "mismatch")
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withComorbidity(mismatch)),
            "No allergies with name allergy"
        )
    }

    @Test
    fun `Should pass with intolerance that matches`() {
        val match = ComorbidityTestFactory.intolerance(name = "matching allergy")
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(ComorbidityTestFactory.withComorbidity(match)),
            "Allergy matching allergy"
        )
    }
}