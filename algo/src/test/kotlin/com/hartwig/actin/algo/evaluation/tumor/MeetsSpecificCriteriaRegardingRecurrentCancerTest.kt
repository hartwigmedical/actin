package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class MeetsSpecificCriteriaRegardingRecurrentCancerTest {

    @Test
    fun `Should evaluate to undetermined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            MeetsSpecificCriteriaRegardingRecurrentCancer().evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}