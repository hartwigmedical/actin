package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val METASTASIS_TYPE: String = "bone"

class TumorMetastasisEvaluatorTest {
    @Test
    fun `Should be undetermined when boolean is null`() {
        val undetermined = TumorMetastasisEvaluator.evaluate(null, METASTASIS_TYPE)
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedSpecificMessages).contains("Data regarding presence of bone metastases is missing")
        assertThat(undetermined.undeterminedGeneralMessages).contains("Missing bone metastasis data")
    }

    @Test
    fun `Should pass when boolean is true`() {
        val pass = TumorMetastasisEvaluator.evaluate(true, METASTASIS_TYPE)
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passSpecificMessages).contains("Bone metastases are present")
        assertThat(pass.passGeneralMessages).contains("Bone metastases")
    }

    @Test
    fun `Should fail when boolean is false`() {
        val fail = TumorMetastasisEvaluator.evaluate(false, METASTASIS_TYPE)
        assertEvaluation(EvaluationResult.FAIL, fail)
        assertThat(fail.failSpecificMessages).contains("No bone metastases present")
        assertThat(fail.failGeneralMessages).contains("No bone metastases")
    }
}