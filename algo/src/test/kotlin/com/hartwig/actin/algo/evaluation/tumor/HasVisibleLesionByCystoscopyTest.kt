package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasVisibleLesionByCystoscopyTest {

    @Test
    fun `Should evaluate to undetermined`() {
        val evaluation = HasVisibleLesionByCystoscopy(EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).tumor)
            .evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
    }
}