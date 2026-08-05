package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasOvarianCancerWithMucinousComponentTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).tumor
    val function = HasOvarianCancerWithMucinousComponent(TestDoidModelFactory.createMinimalTestDoidModel(), labels)
    
    @Test
    fun canEvaluate() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))

        val matchSingle = TumorTestFactory.withDoids(HasOvarianCancerWithMucinousComponent.OVARIAN_MUCINOUS_DOIDS.iterator().next())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(matchSingle))

        val matchCombination = TumorTestFactory.withDoids(HasOvarianCancerWithMucinousComponent.OVARIAN_MUCINOUS_DOID_SET)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(matchCombination))

        val somethingElse = TumorTestFactory.withDoids("something else")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(somethingElse))
    }
}