package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class MolecularResultsAreGenerallyAvailableTest {

    @Test
    fun `Should pass if molecular history is not empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, MolecularResultsAreGenerallyAvailable().evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())
        )
    }

    @Test
    fun `Should fail if molecular history is empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            MolecularResultsAreGenerallyAvailable().evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord())
        )
    }
}