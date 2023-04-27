package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasIntratumoralHemorrhageByMRITest {
    @Test
    fun canEvaluate() {
        val function = HasIntratumoralHemorrhageByMRI()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))
    }
}