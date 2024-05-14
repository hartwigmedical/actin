package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasLymphNodeMetastasesTest {
    private val function: HasLymphNodeMetastases = HasLymphNodeMetastases()

    @Test
    fun shouldBeUndeterminedWhenHasLymphNodeLesionsIsNull() {
        val undetermined = function.evaluate(TestTumorFactory.withLymphNodeLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedSpecificMessages).contains("Data regarding presence of lymph node metastases is missing")
        assertThat(undetermined.undeterminedGeneralMessages).contains("Missing lymph node metastasis data")
    }

    @Test
    fun shouldPassWhenHasLymphNodeLesionsIsTrue() {
        val pass = function.evaluate(TestTumorFactory.withLymphNodeLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertThat(pass.passSpecificMessages).contains("Lymph node metastases are present")
        assertThat(pass.passGeneralMessages).contains("Lymph node metastases")
    }

    @Test
    fun shouldFailWhenHasLymphNodeLesionsIsFalse() {
        val fail = function.evaluate(TestTumorFactory.withLymphNodeLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
        assertThat(fail.failSpecificMessages).contains("No lymph node metastases present")
        assertThat(fail.failGeneralMessages).contains("No lymph node metastases")
    }
}