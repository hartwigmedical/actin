package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasLesionsInfiltratingBloodVesselTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).tumor

    @Test
    fun `Should evaluate to undetermined`() {
        val evaluation = HasLesionsInfiltratingBloodVessel(labels).evaluate(TestPatientFactory.createProperTestPatientRecord())
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }
}
