package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.priorMolecularTest
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.junit.Test

private const val IHC = "IHC"
private const val PROTEIN = "protein 1"

class ProteinHasSufficientExpressionByIHCTest {
    @Test
    fun canEvaluate() {
        val exact = ProteinHasSufficientExpressionByIHC(PROTEIN, 2)

        // No prior tests
        val priorTests = mutableListOf<PriorMolecularTest>()
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with no result
        priorTests.add(ihcTest())
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with too low result
        priorTests.add(ihcTest(scoreValue = 1.0))
        assertEvaluation(EvaluationResult.FAIL, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with too low result but a suitable comparator
        priorTests.add(ihcTest(scoreValuePrefix = ValueComparison.LARGER_THAN, scoreValue = 1.0))
        assertEvaluation(EvaluationResult.UNDETERMINED, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with valid result
        priorTests.add(ihcTest(scoreValue = 3.0))
        assertEvaluation(EvaluationResult.PASS, exact.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Test with unclear result
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            exact.evaluate(MolecularTestFactory.withPriorTest(ihcTest(scoreText = "Negative")))
        )
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null): PriorMolecularTest {
        return priorMolecularTest(
            test = IHC, item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText
        )
    }
}