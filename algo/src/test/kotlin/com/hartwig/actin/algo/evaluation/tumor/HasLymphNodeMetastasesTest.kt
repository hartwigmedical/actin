package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Assert.assertTrue
import org.junit.Test

class HasLymphNodeMetastasesTest {
    private val function: HasLymphNodeMetastases = HasLymphNodeMetastases()

    @Test
    fun shouldBeUndeterminedWhenHasLymphNodeLesionsIsNull() {
        val undetermined = function.evaluate(TumorTestFactory.withLymphNodeLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertTrue(
            undetermined.undeterminedSpecificMessages().contains(
                "Data regarding presence of lymph node metastases is missing"
            )
        )
        assertTrue(undetermined.undeterminedGeneralMessages().contains("Missing lymph node metastasis data"))
    }

    @Test
    fun shouldPassWhenHasLymphNodeLesionsIsTrue() {
        val pass = function.evaluate(TumorTestFactory.withLymphNodeLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
        assertTrue(pass.passSpecificMessages().contains("Lymph node metastases are present"))
        assertTrue(pass.passGeneralMessages().contains("Lymph node metastases"))
    }

    @Test
    fun shouldFailWhenHasLymphNodeLesionsIsFalse() {
        val fail = function.evaluate(TumorTestFactory.withLymphNodeLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
        assertTrue(fail.failSpecificMessages().contains("No lymph node metastases present"))
        assertTrue(fail.failGeneralMessages().contains("No lymph node metastases"))
    }
}