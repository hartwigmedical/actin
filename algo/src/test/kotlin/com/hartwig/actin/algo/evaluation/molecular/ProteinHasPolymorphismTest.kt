package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class ProteinHasPolymorphismTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).molecular

    @Test
    fun `Should evaluate to undetermined`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            ProteinHasPolymorphism("protein", "V1/V2", labels).evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }
}