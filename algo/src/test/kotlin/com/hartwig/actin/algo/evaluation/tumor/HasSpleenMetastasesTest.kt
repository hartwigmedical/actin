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
            function.evaluate(TumorTestFactory.withOtherLesions(null)),
            "Spleen metastases undetermined (metastases data missing)"
        )
    }

    @Test
    fun `Should fail if patient has no spleen metastases`() {
        listOf("abdominal lesion", "Lymph node").forEach {
            EvaluationAssert.assertEvaluation(
                EvaluationResult.FAIL,
                function.evaluate(TumorTestFactory.withOtherLesions(listOf(it))),
                "No spleen metastases"
            )
        }
    }

    @Test
    fun `Should fail if patient has no other lesions`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withOtherLesions(emptyList())),
            "No spleen metastases"
        )
    }

    @Test
    fun `Should pass if patient has spleen metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Splenic metastases"))),
            "Spleen metastases in provided lesions"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("Multiple depositions in spleen"))),
            "Spleen metastases in provided lesions"
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withOtherLesions(listOf("intrasplenic"))),
            "Spleen metastases in provided lesions"
        )
    }

    @Test
    fun `Should warn if patient has suspected spleen metastases`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withOtherSuspectedLesions(listOf("spleen"))),
            "Suspected spleen metastases in provided lesions"
        )
    }
}