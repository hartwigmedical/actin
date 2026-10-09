package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasSoftTissueMetastasesTest {

    private val function = HasSoftTissueMetastases()

    @Test
    fun `Should be undetermined if other lesions are unknown`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withOtherLesions(null)),
            "Soft tissue metastases undetermined (metastases data missing)"
        )
    }

    @Test
    fun `Should fail if patient has no soft tissue metastases`() {
        listOf("abdominal lesion", "Lymph node").forEach {
            EvaluationAssert.assertEvaluation(
                EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withOtherLesions(listOf(it))),
                "No soft tissue metastases"
            )
        }
    }

    @Test
    fun `Should fail if patient has no other lesions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(emptyList())),
            "No soft tissue metastases"
        )
    }

    @Test
    fun `Should pass if patient has soft tissue metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Soft tissue metastases"))),
            "Has soft tissue metastases"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("intramuscular depositions"))),
            "Has soft tissue metastases"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("skin"))),
            "Has soft tissue metastases"
        )
    }

    @Test
    fun `Should warn if patient has suspected soft tissue metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherSuspectedLesions(listOf("soft tissue"))),
            "Has suspected soft tissue metastases"
        )
    }
}