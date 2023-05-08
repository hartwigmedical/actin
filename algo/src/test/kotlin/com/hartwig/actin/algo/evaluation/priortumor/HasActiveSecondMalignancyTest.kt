package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import org.junit.Test

class HasActiveSecondMalignancyTest {
    @Test
    fun canEvaluate() {
        val function = HasActiveSecondMalignancy()

        // No active prior tumors.
        val priorTumors: MutableList<PriorSecondPrimary> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))

        // One prior tumor but inactive
        priorTumors.add(PriorTumorTestFactory.builder().isActive(false).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))

        // One other prior tumor, still active
        priorTumors.add(PriorTumorTestFactory.builder().isActive(true).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }
}