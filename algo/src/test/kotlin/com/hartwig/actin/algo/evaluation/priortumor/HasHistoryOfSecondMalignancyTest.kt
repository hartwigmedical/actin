package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import org.junit.Test

class HasHistoryOfSecondMalignancyTest {

    @Test
    fun canEvaluate() {
        val function = HasHistoryOfSecondMalignancy()

        // No active prior tumors.
        val priorTumors: MutableList<PriorPrimary> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)))

        // One prior tumor
        priorTumors.add(PriorTumorTestFactory.priorPrimary())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)))
    }
}