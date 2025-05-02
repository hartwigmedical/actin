package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val METASTASIS_TYPE: String = "bone"

class TumorMetastasisEvaluatorTest {
    @Test
    fun `Should be undetermined when boolean is null`() {
        val undetermined = TumorMetastasisEvaluator.evaluate(null, null, METASTASIS_TYPE)
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedMessages).contains("Undetermined if patient has bone metastases (missing lesion data)")
    }

    @Test
    fun `Should pass when boolean is true`() {
        val pass = TumorMetastasisEvaluator.evaluate(true, false, METASTASIS_TYPE)
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passMessages).contains("Has bone metastases")
    }

    @Test
    fun `Should warn when only suspected metastasis boolean is true`() {
        listOf(false, null).forEach { hasKnownLesion ->
            val warn = TumorMetastasisEvaluator.evaluate(hasKnownLesion, true, METASTASIS_TYPE)
            val message = "Has suspected bone metastases and not yet confirmed"
            assertEvaluation(EvaluationResult.WARN, warn)
            listOf(warn.warnMessages).forEach {
                assertThat(it).contains(message)
            }
        }
    }

    @Test
    fun `Should fail when boolean is false`() {
        val fail = TumorMetastasisEvaluator.evaluate(false, false, METASTASIS_TYPE)
        assertEvaluation(EvaluationResult.FAIL, fail)
        assertThat(fail.failMessages).contains("No bone metastases")
    }
}