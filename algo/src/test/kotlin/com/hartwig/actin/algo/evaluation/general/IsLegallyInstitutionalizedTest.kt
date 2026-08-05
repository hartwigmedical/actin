package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class IsLegallyInstitutionalizedTest {

    private val function = IsLegallyInstitutionalized(EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).general)

    @Test
    fun `Should fail always`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}