package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasMeasurableDiseaseRecistTest {
    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createWithOneParentChild("100", "200")
        val function = HasMeasurableDiseaseRecist(doidModel)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withMeasurableDisease(null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withMeasurableDisease(true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withMeasurableDisease(false)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withMeasurableDiseaseAndDoid(true, "random")))
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TumorTestFactory.withMeasurableDiseaseAndDoid(
                    true,
                    HasMeasurableDiseaseRecist.NON_RECIST_TUMOR_DOIDS.iterator().next()
                )
            )
        )
    }
}