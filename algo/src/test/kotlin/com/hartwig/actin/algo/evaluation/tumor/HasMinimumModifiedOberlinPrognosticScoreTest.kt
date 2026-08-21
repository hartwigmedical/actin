package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasMinimumModifiedOberlinPrognosticScoreTest {

    @Test
    fun `Should evaluate to undetermined`() {
        val result = HasMinimumModifiedOberlinPrognosticScore(2).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            result,
            "Undetermined if patient has modified oberlin prognostic score of at least 2"
        )
    }
}