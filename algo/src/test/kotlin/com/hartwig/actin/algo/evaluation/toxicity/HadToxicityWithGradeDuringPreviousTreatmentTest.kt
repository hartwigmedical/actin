package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HadToxicityWithGradeDuringPreviousTreatmentTest {

    @Test
    fun `Should evaluate to undetermined`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            HadToxicityWithGradeDuringPreviousTreatment("hepatic", 2, EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).toxicity)
                .evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}