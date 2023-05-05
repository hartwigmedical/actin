package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Intolerance
import org.junit.Test

class HasIntoleranceToTaxanesTest {
    @Test
    fun canEvaluate() {
        val function = HasIntoleranceToTaxanes()

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(emptyList())))

        // Mismatch allergy
        val mismatch: Intolerance = ToxicityTestFactory.intolerance().name("mismatch").build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(mismatch)))

        // Matching allergy
        val match: Intolerance = ToxicityTestFactory.intolerance().name(HasIntoleranceToTaxanes.TAXANES.iterator().next()).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(match)))
    }
}