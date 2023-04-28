package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasIntoleranceWithSpecificDoidTest {
    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
        val function = HasIntoleranceWithSpecificDoid(doidModel, "parent")

        // No allergies
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerances(emptyList())))

        // Allergy with mismatching doid
        val mismatch: Intolerance = ToxicityTestFactory.intolerance().addDoids("other").build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ToxicityTestFactory.withIntolerance(mismatch)))

        // Matching with parent doid
        val matchParent: Intolerance = ToxicityTestFactory.intolerance().addDoids("other", "parent").build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(matchParent)))

        // Matching with child doid
        val matchChild: Intolerance = ToxicityTestFactory.intolerance().addDoids("child").build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ToxicityTestFactory.withIntolerance(matchChild)))
    }
}