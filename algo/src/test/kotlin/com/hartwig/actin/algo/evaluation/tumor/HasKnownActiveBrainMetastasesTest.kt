package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasKnownActiveBrainMetastasesTest {

    private val function = HasKnownActiveBrainMetastases()

    @Test
    fun `Should return undetermined when unknown if (active) brain metastases present`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = null, hasActiveBrainLesions = null))
        )
    }

    @Test
    fun `Should return undetermined when brain metastases present but unknown if active`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasActiveBrainLesions = null))
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
            )
        )
    }

    @Test
    fun `Should fail when there are no brain metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = false, hasActiveBrainLesions = null))
        )
    }

    @Test
    fun `Should fail when brain metastases are present but not active`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasActiveBrainLesions = false))
        )
    }

    @Test
    fun `Should pass when brain metastases are present and active`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasActiveBrainLesions = true))
        )
    }
}