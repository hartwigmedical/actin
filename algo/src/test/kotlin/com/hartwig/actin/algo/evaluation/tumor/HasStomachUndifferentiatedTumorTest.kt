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
        val missingType = TumorTestFactory.withTumorDetails(
            TumorTestFactory.builder()
                .addDoids(DoidConstants.STOMACH_CANCER_DOID)
                .primaryTumorType("wrong")
                .build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(missingType))
        val missingDoid = TumorTestFactory.withTumorDetails(
            TumorTestFactory.builder()
                .addDoids("wrong")
                .primaryTumorType(HasStomachUndifferentiatedTumor.UNDIFFERENTIATED_TYPES.iterator().next())
                .build()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(missingDoid))
        val correct = TumorTestFactory.withTumorDetails(
            TumorTestFactory.builder()
                .addDoids(DoidConstants.STOMACH_CANCER_DOID)
                .primaryTumorType(HasStomachUndifferentiatedTumor.UNDIFFERENTIATED_TYPES.iterator().next())
                .build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(correct))
    }
}