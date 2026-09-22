package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasKnownActiveBrainMetastasesTest {

    private val function = HasKnownActiveBrainMetastases()

    @Test
    fun `Should return undetermined when unknown if (active) brain metastases present`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = null, hasActiveBrainLesions = null)),
            "Undetermined if active brain metastases present (data missing)"
        )
    }

    @Test
    fun `Should return undetermined when brain metastases present but unknown if active`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasActiveBrainLesions = null)),
            "Brain metastases present but unknown if active (data missing)"
        )
    }

    @Test
    fun `Should return undetermined when brain metastases are suspected but unknown if active`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withBrainLesionStatus(
                    hasBrainLesions = false,
                    hasActiveBrainLesions = null,
                    hasSuspectedBrainLesions = true
                )
            ),
            "Suspected brain metastases present but unknown if active (data missing)"
        )
    }

    @Test
    fun `Should fail when there are no brain metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = false, hasActiveBrainLesions = null)),
            "No known active brain metastases present"
        )
    }

    @Test
    fun `Should fail when brain metastases are present but not active`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasActiveBrainLesions = false)),
            "No known active brain metastases present"
        )
    }

    @Test
    fun `Should pass when brain metastases are present and active`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasActiveBrainLesions = true)),
            "Has active brain metastases"
        )
    }
}