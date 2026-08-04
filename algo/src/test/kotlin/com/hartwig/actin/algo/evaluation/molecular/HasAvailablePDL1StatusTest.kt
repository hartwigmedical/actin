package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasAvailablePDL1StatusTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).molecular

    @Test
    fun `Should pass if record contains PD-L1 IHC test`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, HasAvailablePDL1Status(labels).evaluate(
                MolecularTestFactory.withIhcTests(listOf(MolecularTestFactory.ihcTest(item = "PD-L1")))
            )
        )
    }

    @Test
    fun `Should fail if record does not contain PD-L1 IHC test`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, HasAvailablePDL1Status(labels).evaluate(MolecularTestFactory.withIhcTests(emptyList()))
        )
    }
}