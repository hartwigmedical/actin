package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasInTransitMetastasesTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).tumor
    private val function = HasInTransitMetastases(labels)

    @Test
    fun `Should be undetermined always`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}