package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasSufficientLifeExpectancyTest {

    private val function = HasSufficientLifeExpectancy(EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).general)

    @Test
    fun `Should pass always`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}