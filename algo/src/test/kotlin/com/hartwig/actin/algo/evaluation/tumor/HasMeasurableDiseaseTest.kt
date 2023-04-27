package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasMeasurableDiseaseTest {
    @Test
    fun canEvaluate() {
        val function = HasMeasurableDisease()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withMeasurableDisease(null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withMeasurableDisease(true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withMeasurableDisease(false)))
    }
}