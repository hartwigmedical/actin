package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasMRIVolumeAmenableLesionTest {
    @Test
    fun canEvaluate() {
        val function = HasMRIVolumeAmenableLesion()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}