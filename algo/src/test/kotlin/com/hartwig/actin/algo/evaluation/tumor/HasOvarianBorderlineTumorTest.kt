package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasOvarianBorderlineTumorTest {
    private val function = HasOvarianBorderlineTumor(TestDoidModelFactory.createMinimalTestDoidModel())
    private val targetedType = HasOvarianBorderlineTumor.OVARIAN_BORDERLINE_KEYWORDS.iterator().next()

    @Test
    fun canEvaluate() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))

        val wrongCancerType = TumorTestFactory.withDoidAndName("wrong", targetedType)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(wrongCancerType))

        val genericType = TumorTestFactory.withDoidAndName(DoidConstants.OVARIAN_CANCER_DOID, "wrong")
        assertEvaluation(EvaluationResult.WARN, function.evaluate(genericType))

        val correct = TumorTestFactory.withDoidAndName(DoidConstants.OVARIAN_CANCER_DOID, targetedType)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(correct))
    }
}