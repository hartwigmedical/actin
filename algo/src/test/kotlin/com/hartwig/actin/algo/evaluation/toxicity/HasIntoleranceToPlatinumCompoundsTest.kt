package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.Intolerance
import org.junit.Test

class HasIntoleranceToPlatinumCompoundsTest {

    @Test
    fun `Should fail when no known intolerances are present`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasIntoleranceToPlatinumCompounds().evaluate(ToxicityTestFactory.withIntolerances(emptyList()))
        )
    }

    @Test
    fun `Should fail when intolerances are not of Taxane category`() {
        val mismatch: Intolerance = ToxicityTestFactory.intolerance().name("mismatch").build()
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasIntoleranceToPlatinumCompounds().evaluate(ToxicityTestFactory.withIntolerance(mismatch))
        )
    }

    @Test
    fun `Should pass with known Taxane intolerance present`() {
        val match: Intolerance =
            ToxicityTestFactory.intolerance().name(HasIntoleranceToPlatinumCompounds.PLATINUM_COMPOUNDS.iterator().next()).build()
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasIntoleranceToPlatinumCompounds().evaluate(ToxicityTestFactory.withIntolerance(match))
        )
    }
}