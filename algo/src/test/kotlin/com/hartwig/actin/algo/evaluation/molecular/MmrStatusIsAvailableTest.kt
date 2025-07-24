package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import org.assertj.core.api.Assertions.assertThat
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class MmrStatusIsAvailableTest {

    private val function = MmrStatusIsAvailable()

    @Test
    fun `Should pass with MSI true`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMicrosatelliteStability(true)))
    }

    @Test
    fun `Should pass with MSI false`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMicrosatelliteStability(false)))
    }

    @Test
    fun `Should fail when missing MMR information`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMicrosatelliteStability(null)))
    }

    @Test
    fun `Should fail when no molecular tests available`() {
        val evaluation = function.evaluate(TestPatientFactory.createEmptyMolecularTestPatientRecord())
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertThat(evaluation.failMessagesStrings()).containsExactly("No MMR status available (no molecular test)")
    }
}