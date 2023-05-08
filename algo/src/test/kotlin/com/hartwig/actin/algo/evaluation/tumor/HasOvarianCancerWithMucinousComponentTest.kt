package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasOvarianCancerWithMucinousComponentTest {
    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val function = HasOvarianCancerWithMucinousComponent(doidModel)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))
        val matchSingle: PatientRecord =
            TumorTestFactory.withDoids(HasOvarianCancerWithMucinousComponent.OVARIAN_MUCINOUS_DOIDS.iterator().next())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(matchSingle))
        val matchCombination: PatientRecord =
            TumorTestFactory.withDoids(HasOvarianCancerWithMucinousComponent.OVARIAN_MUCINOUS_DOID_SET)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(matchCombination))
        val somethingElse = TumorTestFactory.withDoids("something else")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(somethingElse))
    }
}