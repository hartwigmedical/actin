package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasKnownBrainMetastasesTest {

    private val function = HasKnownBrainMetastases()

    @Test
    fun `Should return undetermined when brain lesion data is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesions(null)),
            "Undetermined if brain metastases present (data missing)"
        )
    }

    @Test
    fun `Should fail when no brain lesions present`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesions(false)),
            "No known brain metastases present"
        )
    }

    @Test
    fun `Should pass when brain lesions present`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBrainLesions(true)),
            "Has brain metastases"
        )
    }

    @Test
    fun `Should warn when only suspected brain lesions present`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withBrainLesions(hasBrainLesions = false, hasSuspectedBrainLesions = true)),
            "Brain metastases present but suspected lesions only"
        )
    }
}