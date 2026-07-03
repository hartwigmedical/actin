package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasSpleenMetastasesTest {

    private val function = HasSpleenMetastases()

    @Test
    fun `Should be undetermined if other lesions are unknown`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withOtherLesions(null))
        )
    }

    @Test
    fun `Should fail if patient has no spleen metastases`() {
        listOf("abdominal lesion", "Lymph node").forEach {
            EvaluationAssert.assertEvaluation(
                EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withOtherLesions(listOf(it)))
            )
        }
    }

    @Test
    fun `Should fail if patient has no other lesions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(emptyList()))
        )
    }

    @Test
    fun `Should pass if patient has spleen metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Splenic metastases")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Multiple depositions in spleen")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("intrasplenic")))
        )
    }

    @Test
    fun `Should warn if patient has suspected spleen metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherSuspectedLesions(listOf("spleen")))
        )
    }
}