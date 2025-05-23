package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStatus
import org.junit.Test

class HasActiveSecondMalignancyTest {

    private val function = HasActiveSecondMalignancy()

    @Test
    fun `Should fail on no second primaries`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorPrimaries(emptyList())))
    }

    @Test
    fun `Should fail on no active prior second primaries`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                PriorTumorTestFactory.withPriorPrimaries(listOf(PriorTumorTestFactory.priorPrimary()))
            )
        )
    }

    @Test
    fun `Should warn on at least one expectative prior second primaries`() {
        assertEvaluation(
            EvaluationResult.WARN, function.evaluate(
                PriorTumorTestFactory.withPriorPrimaries(
                    listOf(PriorTumorTestFactory.priorPrimary(status = TumorStatus.EXPECTATIVE))
                )
            )
        )
    }

    @Test
    fun `Should pass on at least one active second primaries`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                PriorTumorTestFactory.withPriorPrimaries(
                    listOf(PriorTumorTestFactory.priorPrimary(status = TumorStatus.ACTIVE))
                )
            )
        )
    }
}