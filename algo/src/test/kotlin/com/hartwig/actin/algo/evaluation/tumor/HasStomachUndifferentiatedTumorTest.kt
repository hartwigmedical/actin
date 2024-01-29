package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasStomachUndifferentiatedTumorTest {
    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val function = HasStomachUndifferentiatedTumor(doidModel)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))

        val missingType = TumorTestFactory.withDoidAndType(DoidConstants.STOMACH_CANCER_DOID, "wrong")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(missingType))

        val targetType = HasStomachUndifferentiatedTumor.UNDIFFERENTIATED_TYPES.iterator().next()
        val missingDoid = TumorTestFactory.withDoidAndType("wrong", targetType)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(missingDoid))

        val correct = TumorTestFactory.withDoidAndType(DoidConstants.STOMACH_CANCER_DOID, targetType)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(correct))
    }
}