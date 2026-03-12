package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HrdStatusIsAvailableTest {

    private val function = HrdStatusIsAvailable()

    @Test
    fun `Should pass with HRD sequencing result true`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHomologousRecombination(true)))
    }

    @Test
    fun `Should pass with HRD sequencing result false`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withHomologousRecombination(false)))
    }

    @Test
    fun `Should fail when missing HRD information`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withHomologousRecombination(null)))
    }
}