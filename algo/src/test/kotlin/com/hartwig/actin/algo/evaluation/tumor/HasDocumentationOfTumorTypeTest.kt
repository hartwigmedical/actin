package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasDocumentationOfTumorTypeTest {

    private val function = HasDocumentationOfTumorType("Cytological", EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).tumor)

    @Test
    fun `Should pass always`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}