package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasHistoryOfSecondMalignancyWithDoidTest {

    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200")
        val function = HasHistoryOfSecondMalignancyWithDoid(doidModel, "100")

        // No prior tumors.
        val priorTumors: MutableList<PriorSecondPrimary> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))

        // Wrong doid
        priorTumors.add(PriorTumorTestFactory.priorSecondPrimary(doid = "300"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))

        // Right doid
        priorTumors.add(PriorTumorTestFactory.priorSecondPrimary(doid = "200"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(priorTumors)))
    }
}