package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class HasUnresectablePeritonealMetastasesTest {

    private val function = HasUnresectablePeritonealMetastases()

    @Test
    fun `Should be undetermined if other lesions are unknown`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestTumorFactory.withOtherLesions(null))
        )
    }

    @Test
    fun `Should fail if patient has no peritoneal metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestTumorFactory.withOtherLesions(listOf("retroperitoneal lesions")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestTumorFactory.withOtherLesions(listOf("metastases in subperitoneal region")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestTumorFactory.withOtherLesions(listOf("Lymph node")))
        )
    }

    @Test
    fun `Should fail if patient has no other lesions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestTumorFactory.withOtherLesions(emptyList()))
        )
    }

    @Test
    fun `Should warn if patient has peritoneal metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TestTumorFactory.withOtherLesions(listOf("Abdominal lesion located in Peritoneum")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TestTumorFactory.withOtherLesions(listOf("Multiple depositions abdominal and peritoneal")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TestTumorFactory.withOtherLesions(listOf("intraperitoneal")))
        )
    }
}