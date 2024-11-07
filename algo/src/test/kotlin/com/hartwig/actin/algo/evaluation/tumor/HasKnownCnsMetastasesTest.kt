package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasKnownCnsMetastasesTest {

    private val function = HasKnownCnsMetastases()

    @Test
    fun `Should return undetermined when both CNS and brain lesion data are missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainAndCnsLesions(null, null))
        )
    }

    @Test
    fun `Should return undetermined when no CNS lesions but brain lesion data is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainAndCnsLesions(hasBrainLesions = null, hasCnsLesions = false))
        )
    }

    @Test
    fun `Should return undetermined when no brain lesions but CNS lesion data is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainAndCnsLesions(hasBrainLesions = false, hasCnsLesions = null))
        )
    }

    @Test
    fun `Should fail when neither CNS nor brain lesions are present`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainAndCnsLesions(hasBrainLesions = false, hasCnsLesions = false))
        )
    }

    @Test
    fun `Should pass when CNS lesions present`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBrainAndCnsLesions(hasBrainLesions = false, hasCnsLesions = true))
        )
    }

    @Test
    fun `Should pass when brain lesions present`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBrainAndCnsLesions(hasBrainLesions = true, hasCnsLesions = false))
        )
    }

    @Test
    fun `Should pass when both brain and CNS metastases are present`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBrainAndCnsLesions(hasBrainLesions = true, hasCnsLesions = true))
        )
    }

    @Test
    fun `Should return undetermined when only suspected brain lesions present`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withBrainAndCnsLesions(
                    hasBrainLesions = false,
                    hasCnsLesions = false,
                    hasSuspectedBrainLesions = true,
                    hasSuspectedCnsLesions = false
                )
            )
        )
    }

    @Test
    fun `Should return undetermined when only suspected CNS lesions present`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withBrainAndCnsLesions(
                    hasBrainLesions = false,
                    hasCnsLesions = false,
                    hasSuspectedBrainLesions = false,
                    hasSuspectedCnsLesions = true
                )
            )
        )
    }
}