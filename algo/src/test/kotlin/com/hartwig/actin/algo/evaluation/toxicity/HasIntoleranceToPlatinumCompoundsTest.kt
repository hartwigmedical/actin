package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasIntoleranceToPlatinumCompoundsTest {

    @Test
    fun `Should fail when no known intolerances are present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, HasIntoleranceToPlatinumCompounds().evaluate(ToxicityTestFactory.withIntolerances(emptyList()))
        )
    }

    @Test
    fun `Should fail when intolerances are not of Taxane category`() {
        val mismatch = ToxicityTestFactory.intolerance(name = "mismatch")
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, HasIntoleranceToPlatinumCompounds().evaluate(ToxicityTestFactory.withIntolerance(mismatch))
        )
    }

    @Test
    fun `Should pass with known Taxane intolerance present`() {
        val match = ToxicityTestFactory.intolerance(name = HasIntoleranceToPlatinumCompounds.PLATINUM_COMPOUNDS.iterator().next())
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, HasIntoleranceToPlatinumCompounds().evaluate(ToxicityTestFactory.withIntolerance(match))
        )
    }

    @Test
    fun `Should pass when substring of intolerance name matches`() {
        val match = ToxicityTestFactory.intolerance(name = "carboplatin chemotherapy allergy")
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, HasIntoleranceToPlatinumCompounds().evaluate(ToxicityTestFactory.withIntolerance(match))
        )
    }
}