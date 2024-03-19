package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test

class HasSymptomsOfPrimaryTumorInSituTest {

    @Test
    fun `Should evaluate to undetermined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasSymptomsOfPrimaryTumorInSitu().evaluate(TestPatientFactory.createMinimalTestPatientRecord())
        )
    }
}