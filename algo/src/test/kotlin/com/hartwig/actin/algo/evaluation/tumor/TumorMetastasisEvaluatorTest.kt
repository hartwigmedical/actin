package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

private const val METASTASIS_TYPE: String = "bone"

class TumorMetastasisEvaluatorTest {
    
    @Test
    fun `Should be undetermined when boolean is null`() {
        val undetermined = TumorMetastasisEvaluator.evaluate(null, null, METASTASIS_TYPE)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            undetermined,
            "Undetermined if bone metastases based on provided lesions"
        )
    }

    @Test
    fun `Should pass when boolean is true`() {
        val pass = TumorMetastasisEvaluator.evaluate(true, false, METASTASIS_TYPE)
        assertEvaluation(EvaluationResult.PASS, pass, "Bone metastases in provided lesions")
    }

    @Test
    fun `Should warn when only suspected metastasis boolean is true`() {
        listOf(false, null).forEach { hasKnownLesion ->
            val warn = TumorMetastasisEvaluator.evaluate(hasKnownLesion, true, METASTASIS_TYPE)
            assertEvaluation(
                EvaluationResult.WARN,
                warn,
                "Suspected bone metastases in provided lesions and not yet confirmed"
            )
        }
    }

    @Test
    fun `Should fail when boolean is false`() {
        val fail = TumorMetastasisEvaluator.evaluate(false, false, METASTASIS_TYPE)
        assertEvaluation(EvaluationResult.FAIL, fail, "No bone metastases")
    }
}