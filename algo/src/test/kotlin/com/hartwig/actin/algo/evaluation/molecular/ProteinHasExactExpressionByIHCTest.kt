package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.junit.Test

class ProteinHasExactExpressionByIHCTest {
    @Test
    fun canEvaluate() {
        val protein = "protein 1"
        val exact = ProteinHasExactExpressionByIHC(protein, 2)

        // No prior tests
        val priorTests = mutableListOf<PriorMolecularTest>()
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with no result
        priorTests.add(ihcBuilder(protein).build())
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with too low result
        priorTests.add(ihcBuilder(protein).scoreValue(1.0).build())
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with too high result
        priorTests.add(ihcBuilder(protein).scoreValue(3.0).build())
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with exact result but with prefix
        priorTests.add(ihcBuilder(protein).scoreValuePrefix(">").scoreValue(2.0).build())
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with 'positive' result
        priorTests.add(ihcBuilder(protein).scoreText("Positive").build())
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with exact result
        priorTests.add(ihcBuilder(protein).scoreValue(2.0).build())
        assertEvaluation(EvaluationResult.PASS, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))
    }

    companion object {
        private fun ihcBuilder(protein: String): ImmutablePriorMolecularTest.Builder {
            return MolecularTestFactory.priorBuilder().test("IHC").item(protein)
        }
    }
}