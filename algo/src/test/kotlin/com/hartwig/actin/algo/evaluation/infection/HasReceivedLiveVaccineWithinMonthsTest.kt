package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasReceivedLiveVaccineWithinMonthsTest {

    @Test
    fun `Should resolve to undetermined always`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HasReceivedLiveVaccineWithinMonths(2).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()),
            "Live vaccine use within 2 months undetermined based on provided history"
        )
    }
}