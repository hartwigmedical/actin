package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class MolecularResultsAreGenerallyAvailableTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).molecular

    @Test
    fun `Should pass if molecular history is not empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            MolecularResultsAreGenerallyAvailable(labels).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }

    @Test
    fun `Should fail if molecular history is empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            MolecularResultsAreGenerallyAvailable(labels).evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord())
        )
    }
}