package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary
import org.junit.Test

class HasHistoryOfSecondMalignancyTest {

    @Test
    fun canEvaluate() {
        val function = HasHistoryOfSecondMalignancy()

        // No active prior tumors.
        val priorTumors: MutableList<PriorSecondPrimary> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))

        // One prior tumor
        priorTumors.add(PriorTumorTestFactory.priorSecondPrimary())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }
}