package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasReceivedNonLiveVaccineWithinWeeksTest {

    @Test
    fun `Should resolve to undetermined always`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasReceivedNonLiveVaccineWithinWeeks(2).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "Non-live vaccine use within 2 weeks undetermined based on provided history"
        )
    }
}