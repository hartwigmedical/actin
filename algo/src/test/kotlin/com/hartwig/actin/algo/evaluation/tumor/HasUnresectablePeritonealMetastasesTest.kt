package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class HasUnresectablePeritonealMetastasesTest {

    @Test
    fun `Should fail if patient has no peritoneal metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(TumorTestFactory.withOtherLesions(listOf("Lymph node")))
        )
    }

    @Test
    fun `Should fail if patient has no metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(TumorTestFactory.withOtherLesions(emptyList()))
        )
    }

    @Test
    fun `Should warn if patient has peritoneal metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            FUNCTION.evaluate(TumorTestFactory.withOtherLesions(listOf("Abdominal lesion located in peritoneum")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            FUNCTION.evaluate(TumorTestFactory.withOtherLesions(listOf("Multiple depositions abdominal and peritoneal")))
        )
    }

    companion object {
        val FUNCTION = HasUnresectablePeritonealMetastases()
    }
}