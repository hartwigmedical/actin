package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Intolerance
import org.junit.Test

class HasIntoleranceToTaxanesTest {

    @Test
    fun `Should fail when no known intolerances are present`() {
        assertEvaluation(EvaluationResult.FAIL, HasIntoleranceToTaxanes().evaluate(ToxicityTestFactory.withIntolerances(emptyList())))
    }

    @Test
    fun `Should fail when intolerances are not of Taxane category`() {
        val mismatch: Intolerance = ToxicityTestFactory.intolerance().name("mismatch").build()
        assertEvaluation(EvaluationResult.FAIL, HasIntoleranceToTaxanes().evaluate(ToxicityTestFactory.withIntolerance(mismatch)))
    }

    @Test
    fun `Should pass with known Taxane intolerance present`() {
        val match: Intolerance = ToxicityTestFactory.intolerance().name(HasIntoleranceToTaxanes.TAXANES.iterator().next()).build()
        assertEvaluation(EvaluationResult.PASS, HasIntoleranceToTaxanes().evaluate(ToxicityTestFactory.withIntolerance(match)))
    }
}