package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Intolerance
import org.junit.Test

class HasIntoleranceWithSpecificNameTest {

    @Test
    fun canEvaluate() {
        val function = HasIntoleranceWithSpecificName("allergy")

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(emptyList())))

        // Mismatch allergy
        val mismatch: Intolerance = ToxicityTestFactory.intolerance(name = "mismatch")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(mismatch)))

        // Matching allergy
        val match: Intolerance = ToxicityTestFactory.intolerance(name = "matching allergy")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(match)))
    }
}