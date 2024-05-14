package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasOvarianBorderlineTumorTest {
    private val function = HasOvarianBorderlineTumor(TestDoidModelFactory.createMinimalTestDoidModel())
    private val targetedType = HasOvarianBorderlineTumor.OVARIAN_BORDERLINE_TYPES.iterator().next()

    @Test
    fun canEvaluate() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestTumorFactory.withDoids(null)))

        val missingType = TestTumorFactory.withDoidAndType(DoidConstants.OVARIAN_CANCER_DOID, "wrong")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(missingType))

        val missingDoid = TestTumorFactory.withDoidAndType("wrong", targetedType)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(missingDoid))

        val correct = TestTumorFactory.withDoidAndType(DoidConstants.OVARIAN_CANCER_DOID, targetedType)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(correct))
    }
}