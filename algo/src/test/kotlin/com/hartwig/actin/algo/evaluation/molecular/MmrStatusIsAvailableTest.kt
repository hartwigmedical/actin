package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class MmrStatusIsAvailableTest {
    private val function = MmrStatusIsAvailable()

    @Test
    fun `Should fail when unknown MMR status`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIsMicrosatelliteUnstable(null)))
    }

    @Test
    fun `Should fail when molecular record not available`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord()))
    }

    @Test
    fun `Should pass with MSI true`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIsMicrosatelliteUnstable(true)))
    }

    @Test
    fun `Should pass with MSI false`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIsMicrosatelliteUnstable(false)))
    }
}
