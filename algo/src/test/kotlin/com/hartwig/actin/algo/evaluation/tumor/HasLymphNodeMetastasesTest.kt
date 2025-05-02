package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasLymphNodeMetastasesTest {
    private val function: HasLymphNodeMetastases = HasLymphNodeMetastases()

    @Test
    fun `Should be undetermined when unknown if has lymph node lesions`() {
        val undetermined = function.evaluate(TumorTestFactory.withLymphNodeLesions(null, null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedMessages).contains("Undetermined if patient has lymph node metastases (missing lesion data)")
    }

    @Test
    fun `Should pass when has lymph node lesions is true`() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passMessages).contains("Has lymph node metastases")
    }

    @Test
    fun `Should fail when has lymph node lesions is false`() {
        val fail = function.evaluate(TumorTestFactory.withLymphNodeLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
        assertThat(fail.failMessages).contains("No lymph node metastases")
    }

    @Test
    fun `Should warn when has suspected lymph node lesions only`() {
        val warn = function.evaluate(TumorTestFactory.withLymphNodeLesions(false, true))
        val message = "Has suspected lymph node metastases and not yet confirmed"
        assertEvaluation(EvaluationResult.WARN, warn)
        listOf(warn.warnMessages).forEach {
            assertThat(it).contains(message)
        }
    }

    @Test
    fun `Should be undetermined when no suspected lymph node lesions but unknown certain lymph node lesions`() {
        val undetermined = function.evaluate(TumorTestFactory.withLymphNodeLesions(null, false))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedMessages).contains("Undetermined if patient has lymph node metastases (missing lesion data)")
    }

    @Test
    fun `Should pass when has lymph node lesions is true and no suspected lymph node lesions`() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(true, false))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passMessages).contains("Has lymph node metastases")
    }
}