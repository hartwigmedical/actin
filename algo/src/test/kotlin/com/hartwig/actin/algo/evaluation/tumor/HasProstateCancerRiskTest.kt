package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasProstateCancerRiskTest {

    @Test
    fun `Should evaluate to undetermined when prostate cancer`() {
        val result = HasProstateCancerRisk(
            listOf("High", "Low"),
            TestDoidModelFactory.createMinimalTestDoidModel()
        ).evaluate(TumorTestFactory.withDoidAndName(DoidConstants.PROSTATE_CANCER_DOID, "prostate cancer"))
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedMessagesStrings()).containsExactly("Undetermined if patient has high or low risk prostate cancer")
    }

    @Test
    fun `Should evaluate to fail when breast cancer`() {
        val result = HasProstateCancerRisk(
            listOf("High", "Low"),
            TestDoidModelFactory.createMinimalTestDoidModel()
        ).evaluate(TumorTestFactory.withDoidAndName(DoidConstants.BREAST_CANCER_DOID, "breast cancer"))
        assertEvaluation(EvaluationResult.FAIL, result)
    }
}