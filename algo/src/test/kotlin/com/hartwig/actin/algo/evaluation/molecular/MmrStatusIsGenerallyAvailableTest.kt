package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import org.junit.Test

class MmrStatusIsGenerallyAvailableTest {
    private val function = MmrStatusIsGenerallyAvailable()

    @Test
    fun `Should evaluate to undetermined when unknown MSI status`() {
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMicrosatelliteSatus(null)))
    }

    @Test
    fun `Should evaluate to undetermined when molecular record not available`() {
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord()))
    }

    @Test
    fun `Should pass with MSI true`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMicrosatelliteSatus(true)))
    }

    @Test
    fun `Should pass with MSI false`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMicrosatelliteSatus(false)))
    }
}
