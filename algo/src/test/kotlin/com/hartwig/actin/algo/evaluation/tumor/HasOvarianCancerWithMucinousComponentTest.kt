package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasOvarianCancerWithMucinousComponentTest {
    val function = HasOvarianCancerWithMucinousComponent(TestDoidModelFactory.createMinimalTestDoidModel())
    
    @Test
    fun canEvaluate() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestTumorFactory.withDoids(null)))

        val matchSingle = TestTumorFactory.withDoids(HasOvarianCancerWithMucinousComponent.OVARIAN_MUCINOUS_DOIDS.iterator().next())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(matchSingle))

        val matchCombination = TestTumorFactory.withDoids(HasOvarianCancerWithMucinousComponent.OVARIAN_MUCINOUS_DOID_SET)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(matchCombination))

        val somethingElse = TestTumorFactory.withDoids("something else")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(somethingElse))
    }
}