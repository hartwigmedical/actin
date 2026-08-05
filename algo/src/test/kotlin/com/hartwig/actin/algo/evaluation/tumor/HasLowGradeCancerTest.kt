package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasLowGradeCancerTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).tumor
    private val function = HasLowGradeCancer(labels)

    @Test
    fun `Should fail when high grade cancer`() {
        val highGradeTumor = TumorTestFactory.withDoidAndName("", "high-grade tumor")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(highGradeTumor))
    }

    @Test
    fun `Should pass when low grade cancer`() {
        val lowGradeTumor = TumorTestFactory.withDoidAndName("", "low-grade tumor")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(lowGradeTumor))
    }

    @Test
    fun `Should resolve to undetermined if terms not found`() {
        val undefinedTumor = TumorTestFactory.withDoidAndName("", "other term")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(undefinedTumor))
    }
}