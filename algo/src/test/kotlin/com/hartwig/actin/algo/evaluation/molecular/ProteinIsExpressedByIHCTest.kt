package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import org.junit.Test

private const val IHC = "IHC"
private const val PROTEIN = "protein 1"

class ProteinIsExpressedByIHCTest {
    private val function = ProteinIsExpressedByIHC(PROTEIN)

    @Test
    fun canEvaluate() {
        // No prior tests
        val priorTests = mutableListOf<PriorIHCTest>()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(priorTests)))

        // Add test with no result
        priorTests.add(ihcTest())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(priorTests)))

        // Add test with negative result
        priorTests.add(ihcTest(scoreText = "negative"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(priorTests)))

        // Add test with positive result
        priorTests.add(ihcTest(scoreValue = 2.0))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIHCTests(priorTests)))

        // Also works for score texts.
        val otherPriorTests = listOf(ihcTest(scoreText = "positive"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIHCTests(otherPriorTests)))
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null) =
        MolecularTestFactory.priorIHCTest(
            test = IHC, item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText
        )
}