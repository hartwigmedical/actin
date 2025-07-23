package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import org.junit.Test

private const val PROTEIN = "protein 1"

class ProteinIsExpressedByIhcTest {
    private val function = ProteinIsExpressedByIhc(PROTEIN)

    @Test
    fun canEvaluate() {
        // No prior tests
        val priorTests = mutableListOf<IhcTest>()
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIhcTests(priorTests)))

        // Add test with no result
        priorTests.add(ihcTest())
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(priorTests)))

        // Add test with negative result
        priorTests.add(ihcTest(scoreText = "negative"))
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(priorTests)))

        // Add test with positive result
        priorTests.add(ihcTest(scoreValue = 2.0))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIhcTests(priorTests)))

        // Also works for score texts.
        val otherPriorTests = listOf(ihcTest(scoreText = "positive"))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIhcTests(otherPriorTests)))
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null) =
        MolecularTestFactory.ihcTest(
            item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText
        )
}