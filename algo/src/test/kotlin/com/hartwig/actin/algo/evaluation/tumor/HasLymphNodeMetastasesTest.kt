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
    fun `should Pass When Has Lymph Node Lesions Is True`() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passSpecificMessages).contains("Lymph node metastases are present")
        assertThat(pass.passGeneralMessages).contains("Lymph node metastases")
    }

    @Test
    fun `should Fail When Has Lymph Node Lesions Is False`() {
        val fail = function.evaluate(TumorTestFactory.withLymphNodeLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
        assertThat(fail.failSpecificMessages).contains("No lymph node metastases present")
        assertThat(fail.failGeneralMessages).contains("No lymph node metastases")
    }

    @Test
    fun `should Pass When Has Suspected Lymph Node Lesions Is True`() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(null, true))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passSpecificMessages).contains("Lymph node metastases are present")
        assertThat(pass.passGeneralMessages).contains("Lymph node metastases")
    }

    @Test
    fun `should Pass When Has Suspected Lymph Node Lesions Is False`() {
        val fail = function.evaluate(TumorTestFactory.withLymphNodeLesions(null, false))
        assertEvaluation(EvaluationResult.FAIL, fail)
        assertThat(fail.failSpecificMessages).contains("No lymph node metastases present")
        assertThat(fail.failGeneralMessages).contains("No lymph node metastases")
    }

    @Test
    fun `should Pass When Has Lymph Node Lesions Is True and No Suspected Lymph Node Lesions`() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(true, false))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passSpecificMessages).contains("Lymph node metastases are present")
        assertThat(pass.passGeneralMessages).contains("Lymph node metastases")
    }

    @Test
    fun `should Pass When Has Lymph Node Lesions Is False Has Suspected Lymph Node Lesions`() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(false, true))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passSpecificMessages).contains("Lymph node metastases are present")
        assertThat(pass.passGeneralMessages).contains("Lymph node metastases")
    }
}