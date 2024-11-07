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
        assertThat(undetermined.undeterminedSpecificMessages).contains("Data regarding presence of lymph node metastases is missing")
        assertThat(undetermined.undeterminedGeneralMessages).contains("Missing lymph node metastasis data")
    }

    @Test
    fun `Should pass when has lymph node lesions is true`() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passSpecificMessages).contains("Lymph node metastases are present")
        assertThat(pass.passGeneralMessages).contains("Lymph node metastases")
    }

    @Test
    fun `Should fail when has lymph node lesions is false`() {
        val fail = function.evaluate(TumorTestFactory.withLymphNodeLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
        assertThat(fail.failSpecificMessages).contains("No lymph node metastases present")
        assertThat(fail.failGeneralMessages).contains("No lymph node metastases")
    }

    @Test
    fun `Should evaluate to undetermined when has suspected lymph node lesions is true`() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(null, true))
        assertEvaluation(EvaluationResult.UNDETERMINED, pass)
        assertThat(pass.undeterminedSpecificMessages).contains("Undetermined if Lymph node metastases present (only suspected lesions)")
        assertThat(pass.undeterminedGeneralMessages).contains("Undetermined Lymph node metastases (suspected lesions only)")
    }

    @Test
    fun `Should evaluate to undetermined when no suspected lymph node lesions but unknown certain lymph node lesions`() {
        val fail = function.evaluate(TumorTestFactory.withLymphNodeLesions(null, false))
        assertEvaluation(EvaluationResult.UNDETERMINED, fail)
        assertThat(fail.undeterminedSpecificMessages).contains("Data regarding presence of lymph node metastases is missing")
        assertThat(fail.undeterminedGeneralMessages).contains("Missing lymph node metastasis data")
    }

    @Test
    fun `Should pass when has lymph node lesions is true and no suspected lymph node lesions`() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(true, false))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passSpecificMessages).contains("Lymph node metastases are present")
        assertThat(pass.passGeneralMessages).contains("Lymph node metastases")
    }

    @Test
    fun `Should evaluate to undetermined when has lymph node lesions is false but has suspected lymph node lesions`() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(false, true))
        assertEvaluation(EvaluationResult.UNDETERMINED, pass)
        assertThat(pass.undeterminedSpecificMessages).contains("Undetermined if Lymph node metastases present (only suspected lesions)")
        assertThat(pass.undeterminedGeneralMessages).contains("Undetermined Lymph node metastases (suspected lesions only)")
    }
}