package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Assert
import org.junit.Test
import kotlin.String

class TumorMetastasisEvaluatorTest {
    @Test
    fun shouldBeUndeterminedWhenBooleanIsNull() {
        val undetermined = TumorMetastasisEvaluator.evaluate(null, metastasisType)
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        Assert.assertTrue(
            undetermined.undeterminedSpecificMessages().contains(
                "Data regarding presence of bone metastases is missing"
            )
        )
        Assert.assertTrue(undetermined.undeterminedGeneralMessages().contains("Missing bone metastasis data"))
    }

    @Test
    fun shouldPassWhenBooleanIsTrue() {
        val pass = TumorMetastasisEvaluator.evaluate(true, metastasisType)
        assertEvaluation(EvaluationResult.PASS, pass)
        Assert.assertTrue(pass.passSpecificMessages().contains("Bone metastases are present"))
        Assert.assertTrue(pass.passGeneralMessages().contains("Bone metastases"))
    }

    @Test
    fun shouldFailWhenBooleanIsFalse() {
        val fail = TumorMetastasisEvaluator.evaluate(false, metastasisType)
        assertEvaluation(EvaluationResult.FAIL, fail)
        Assert.assertTrue(fail.failSpecificMessages().contains("No bone metastases present"))
        Assert.assertTrue(fail.failGeneralMessages().contains("No bone metastases"))
    }

    companion object {
        private const val metastasisType: String = "bone"
    }
}