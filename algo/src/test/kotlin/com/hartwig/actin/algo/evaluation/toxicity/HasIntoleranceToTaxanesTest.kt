package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Intolerance
import org.junit.Test

class HasIntoleranceToTaxanesTest {

    @Test
    fun `Should fail when no known intolerances are present`() {
        assertEvaluation(EvaluationResult.FAIL, HasIntoleranceToTaxanes().evaluate(ToxicityTestFactory.withIntolerances(emptyList())))
    }

    @Test
    fun `Should fail when intolerances are not of Taxane category`() {
        val mismatch: Intolerance = ToxicityTestFactory.intolerance(name = "mismatch")
        assertEvaluation(EvaluationResult.FAIL, HasIntoleranceToTaxanes().evaluate(ToxicityTestFactory.withIntolerance(mismatch)))
    }

    @Test
    fun `Should pass with known Taxane intolerance present`() {
        val match: Intolerance = ToxicityTestFactory.intolerance(name = HasIntoleranceToTaxanes.TAXANES.iterator().next())
        assertEvaluation(EvaluationResult.PASS, HasIntoleranceToTaxanes().evaluate(ToxicityTestFactory.withIntolerance(match)))
    }

    @Test
    fun `Should pass when substring of intolerance name matches`() {
        val match: Intolerance =
            ToxicityTestFactory.intolerance(name = "docetaxel chemotherapy allergy")
        assertEvaluation(
            EvaluationResult.PASS, HasIntoleranceToTaxanes().evaluate(ToxicityTestFactory.withIntolerance(match))
        )
    }
}