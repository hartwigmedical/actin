package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasKnownActiveCnsMetastasesTest {

    private val function = HasKnownActiveCnsMetastases()

    @Test
    fun `Should return undetermined when unknown if (active) CNS or brain metastases present`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withActiveBrainAndCnsLesionStatus(
                    hasBrainLesions = null,
                    hasActiveBrainLesions = null,
                    hasCnsLesions = null,
                    hasActiveCnsLesions = null
                )
            )
        )
    }

    @Test
    fun `Should return undetermined when CNS metastases present but unknown if active`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withActiveBrainAndCnsLesionStatus(
                    hasBrainLesions = false,
                    hasActiveBrainLesions = null,
                    hasCnsLesions = true,
                    hasActiveCnsLesions = null
                )
            )
        )
    }

    @Test
    fun `Should return undetermined when brain metastases present but unknown if active`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withActiveBrainAndCnsLesionStatus(
                    hasBrainLesions = true,
                    hasActiveBrainLesions = null,
                    hasCnsLesions = false,
                    hasActiveCnsLesions = null
                )
            )
        )
    }

    @Test
    fun `Should return undetermined when brain metastases are suspected but unknown if active`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withActiveBrainAndCnsLesionStatus(
                    hasBrainLesions = false,
                    hasActiveBrainLesions = null,
                    hasCnsLesions = null,
                    hasActiveCnsLesions = null,
                    hasSuspectedBrainLesions = true
                )
            )
        )
    }

    @Test
    fun `Should return undetermined when CNS metastases are suspected but unknown if active`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withActiveBrainAndCnsLesionStatus(
                    hasBrainLesions = null,
                    hasActiveBrainLesions = null,
                    hasCnsLesions = false,
                    hasActiveCnsLesions = null,
                    hasSuspectedCnsLesions = true
                )
            )
        )
    }

    @Test
    fun `Should fail when there are no CNS or brain metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withActiveBrainAndCnsLesionStatus(
                    hasBrainLesions = false,
                    hasActiveBrainLesions = null,
                    hasCnsLesions = false,
                    hasActiveCnsLesions = null
                )
            )
        )
    }

    @Test
    fun `Should fail when CNS metastases are present but not active`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withActiveBrainAndCnsLesionStatus(
                    hasBrainLesions = true,
                    hasActiveBrainLesions = null,
                    hasCnsLesions = true,
                    hasActiveCnsLesions = false
                )
            )
        )
    }

    @Test
    fun `Should pass when CNS metastases are present and active`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withActiveBrainAndCnsLesionStatus(
                    hasBrainLesions = false,
                    hasActiveBrainLesions = null,
                    hasCnsLesions = true,
                    hasActiveCnsLesions = true
                )
            )
        )
    }

    @Test
    fun `Should pass when brain metastases are present and active`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withActiveBrainAndCnsLesionStatus(
                    hasBrainLesions = true,
                    hasActiveBrainLesions = true,
                    hasCnsLesions = false,
                    hasActiveCnsLesions = false
                )
            )
        )
    }

    @Test
    fun `Should pass when brain metastases are present and active and CNS metastases are present but not active`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withActiveBrainAndCnsLesionStatus(
                    hasBrainLesions = true,
                    hasActiveBrainLesions = true,
                    hasCnsLesions = true,
                    hasActiveCnsLesions = false
                )
            )
        )
    }
}
