package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class HasUnresectablePeritonealMetastasesTest {

    private val function = HasUnresectablePeritonealMetastases()

    @Test
    fun `Should fail if patient has no other lesions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(null))
        )
    }

    @Test
    fun `Should fail if patient has no peritoneal metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Lymph node")))
        )
    }

    @Test
    fun `Should fail if patient has no metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(emptyList()))
        )
    }

    @Test
    fun `Should warn if patient has peritoneal metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Abdominal lesion located in Peritoneum")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Multiple depositions abdominal and peritoneal")))
        )
    }
}