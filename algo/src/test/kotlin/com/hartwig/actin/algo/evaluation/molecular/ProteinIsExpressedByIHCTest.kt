package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.junit.Test

class ProteinIsExpressedByIHCTest {
    @Test
    fun canEvaluate() {
        val protein = "protein 1"
        val function = ProteinIsExpressedByIHC(protein)

        // No prior tests
        val priorTests = mutableListOf<PriorMolecularTest>()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with no result
        priorTests.add(ihcBuilder(protein).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with negative result
        priorTests.add(ihcBuilder(protein).scoreText("negative").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with positive result
        priorTests.add(ihcBuilder(protein).scoreValue(2.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Also works for score texts.
        val otherPriorTests: List<PriorMolecularTest> = listOf(ihcBuilder(protein).scoreText("positive").build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorTests(otherPriorTests)))
    }

    companion object {
        private fun ihcBuilder(protein: String): ImmutablePriorMolecularTest.Builder {
            return MolecularTestFactory.priorMolecularTest().test("IHC").item(protein)
        }
    }
}