package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasKnownBrainMetastasesTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).tumor
    private val function = HasKnownBrainMetastases(labels)

    @Test
    fun `Should return undetermined when brain lesion data is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesions(null))
        )
    }

    @Test
    fun `Should fail when no brain lesions present`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesions(false))
        )
    }

    @Test
    fun `Should pass when brain lesions present`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBrainLesions(true))
        )
    }

    @Test
    fun `Should warn when only suspected brain lesions present`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withBrainLesions(hasBrainLesions = false, hasSuspectedBrainLesions = true))
        )
    }
}