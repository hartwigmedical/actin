package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.junit.Test

class ProteinHasSufficientExpressionByIHCTest {
    @Test
    fun canEvaluate() {
        val protein = "protein 1"
        val exact = ProteinHasSufficientExpressionByIHC(protein, 2)

        // No prior tests
        val priorTests = mutableListOf<PriorMolecularTest>()
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with no result
        priorTests.add(ihcBuilder(protein).build())
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with too low result
        priorTests.add(ihcBuilder(protein).scoreValue(1.0).build())
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with too low result but a suitable comparator
        priorTests.add(ihcBuilder(protein).scoreValuePrefix(ValueComparison.LARGER_THAN).scoreValue(1.0).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with valid result
        priorTests.add(ihcBuilder(protein).scoreValue(3.0).build())
        assertEvaluation(EvaluationResult.PASS, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Test with unclear result
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            exact.evaluate(
                MolecularTestFactory.withPriorTests(
                    listOf(ihcBuilder(protein).scoreText("Negative").build())
                )
            )
        )
    }

    companion object {
        private fun ihcBuilder(protein: String): ImmutablePriorMolecularTest.Builder {
            return MolecularTestFactory.priorMolecularTest().test("IHC").item(protein)
        }
    }
}