package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.HasStomachUndifferentiatedTumor.Companion.UNDIFFERENTIATED_TERMS
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasStomachUndifferentiatedTumorTest {

    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val function = HasStomachUndifferentiatedTumor(doidModel)
    val targetType = UNDIFFERENTIATED_TERMS.first()

    @Test
    fun `Should evaluate to undetermined if there are no tumor doids configured`() {
        val tumor = TumorTestFactory.withDoids(null)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumor))
    }

    @Test
    fun `Should pass if tumor is stomach cancer of undifferentiated type`() {
        val tumor = TumorTestFactory.withDoidAndName(DoidConstants.STOMACH_CANCER_DOID, "name with $targetType")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumor))
    }

    @Test
    fun `Should warn if tumor is stomach cancer but unknown of undifferentiated type`() {
        val tumor = TumorTestFactory.withDoidAndName(DoidConstants.STOMACH_CANCER_DOID, "name without")
        assertEvaluation(EvaluationResult.WARN, function.evaluate(tumor))
    }

    @Test
    fun `Should fail if tumor type is not stomach cancer`() {
        val tumor = TumorTestFactory.withDoids(DoidConstants.BRAIN_CANCER_DOID)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(tumor))
    }
}