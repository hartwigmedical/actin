package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HadToxicityWithGradeDuringPreviousTreatmentTest {

    @Test
    fun `Should evaluate to undetermined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HadToxicityWithGradeDuringPreviousTreatment("hepatic", 2).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}