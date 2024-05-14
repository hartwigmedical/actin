package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.HasStomachUndifferentiatedTumor.Companion.UNDIFFERENTIATED_DETAILS
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasStomachUndifferentiatedTumorTest {

    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val function = HasStomachUndifferentiatedTumor(doidModel)
    val targetType = HasStomachUndifferentiatedTumor.UNDIFFERENTIATED_TYPES.iterator().next()

    @Test
    fun `Should evaluate to undetermined if there are no tumor doids configured`() {
        val tumor = TumorTestFactory.withDoids(null)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumor))
    }

    @Test
    fun `Should evaluate to undetermined if there is no tumor type configured`() {
        val tumor =
            TumorTestFactory.withDoidAndType(DoidConstants.CANCER_DOID, primaryTumorType = null)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumor))
    }

    @Test
    fun `Should pass if tumor is stomach cancer of undifferentiated type`() {
        val tumor =
            TumorTestFactory.withDoidAndType(DoidConstants.STOMACH_CANCER_DOID, targetType)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumor))
    }

    @Test
    fun `Should pass if tumor is stomach cancer with undifferentiated type specified in extra details`() {
        val tumor =
            TumorTestFactory.withDoidAndTypeAndDetails(
                DoidConstants.STOMACH_CANCER_DOID,
                "Stomach cancer",
                UNDIFFERENTIATED_DETAILS.iterator().next()
            )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumor))
    }

    @Test
    fun `Should fail if tumor type is not stomach cancer`() {
        val tumor =
            TumorTestFactory.withDoidAndType(
                DoidConstants.BRAIN_CANCER_DOID,
                HasStomachUndifferentiatedTumor.UNDIFFERENTIATED_TYPES.iterator().next(),
            )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(tumor))
    }
}