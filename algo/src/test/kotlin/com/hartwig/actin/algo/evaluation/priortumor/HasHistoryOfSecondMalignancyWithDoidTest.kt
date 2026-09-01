package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasHistoryOfSecondMalignancyWithDoidTest {

    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("1", "2").copy(termForDoidMap = mapOf("1" to "breast cancer"))
        val function = HasHistoryOfSecondMalignancyWithDoid(doidModel, "1")

        // No prior tumors.
        val priorTumors: MutableList<PriorPrimary> = mutableListOf()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)),
            "No history of previous malignancy belonging to breast cancer"
        )

        // Wrong doid
        priorTumors.add(PriorTumorTestFactory.priorPrimary(doid = "3"))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)),
            "No history of previous malignancy belonging to breast cancer"
        )

        // Right doid
        priorTumors.add(PriorTumorTestFactory.priorPrimary(doid = "2"))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(PriorTumorTestFactory.withPriorPrimaries(priorTumors)),
            "History of previous malignancy belonging to breast cancer"
        )
    }
}