package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasMinimumMouthOpeningTest {

    @Test
    fun `Should evaluate to undetermined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasMinimumMouthOpening(10).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "Undetermined whether mouth opening is at least 10 mm"
        )
    }
}