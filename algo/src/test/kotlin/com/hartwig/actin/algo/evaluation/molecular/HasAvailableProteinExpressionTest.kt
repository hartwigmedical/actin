package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

private const val PROTEIN = "protein 1"

class HasAvailableProteinExpressionTest {
    
    private val function = HasAvailableProteinExpression(PROTEIN)

    @Test
    fun `Should pass if record contains IHC test for protein`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withIhcTests(listOf(MolecularTestFactory.ihcTest(item = PROTEIN))))
        )
    }

    @Test
    fun `Should fail when record does not contain IHC test for protein`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(emptyList())))
    }

    @Test
    fun `Should fail when record does only contain IHC test for other protein`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withIhcTests(listOf(MolecularTestFactory.ihcTest(item = "other protein"))))
        )
    }
}