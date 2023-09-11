package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorStatus
import org.junit.Test

class HasActiveSecondMalignancyTest {

    private val function = HasActiveSecondMalignancy()

    @Test
    fun `Should fail on no second primaries`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(emptyList())))
    }

    @Test
    fun `Should fail on no active prior second primaries`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                PriorTumorTestFactory.withPriorSecondPrimaries(
                    listOf(
                        PriorTumorTestFactory.builder().tumorStatus(TumorStatus.INACTIVE).build()
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn on at least one expectative prior second primaries`() {
        assertEvaluation(
            EvaluationResult.WARN, function.evaluate(
                PriorTumorTestFactory.withPriorSecondPrimaries(
                    listOf(
                        PriorTumorTestFactory.builder().tumorStatus(TumorStatus.EXPECTATIVE).build()
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass on at least one active second primaries`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                PriorTumorTestFactory.withPriorSecondPrimaries(
                    listOf(
                        PriorTumorTestFactory.builder().tumorStatus(TumorStatus.ACTIVE).build()
                    )
                )
            )
        )
    }
}