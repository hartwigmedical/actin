package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class HasAvailablePDL1StatusTest {

    @Test
    fun `Should pass if record contains PD-L1 IHC test`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, HasAvailablePDL1Status().evaluate(
                MolecularTestFactory.withIHCTests(listOf(MolecularTestFactory.priorIHCTest(test = "IHC", item = "PD-L1")))
            )
        )
    }

    @Test
    fun `Should fail if record does not contain PD-L1 IHC test`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, HasAvailablePDL1Status().evaluate(MolecularTestFactory.withIHCTests(emptyList()))
        )
    }
}