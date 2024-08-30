package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasSecondaryGlioblastomaTest {
    @Test
    fun canEvaluate() {
        val function = HasSecondaryGlioblastoma(TestDoidModelFactory.createMinimalTestDoidModel())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withDoids(DoidConstants.GLIOBLASTOMA_DOID)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("arbitrary doid")))
    }
}