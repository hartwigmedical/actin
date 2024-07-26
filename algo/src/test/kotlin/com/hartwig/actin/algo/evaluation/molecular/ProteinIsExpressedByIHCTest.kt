package com.hartwig.actin.algo.evaluation.molecular

private const val IHC = "IHC"
private const val PROTEIN = "protein 1"

class ProteinIsExpressedByIHCTest {
 /*   private val function = ProteinIsExpressedByIHC(PROTEIN)

    @Test
    fun canEvaluate() {
        // No prior tests
        val priorTests = mutableListOf<MolecularTest>()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(priorTests)))

        // Add test with no result
        priorTests.add(ihcTest())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMolecularTests(priorTests)))

        // Add test with negative result
        priorTests.add(ihcTest(scoreText = "negative"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMolecularTests(priorTests)))

        // Add test with positive result
        priorTests.add(ihcTest(scoreValue = 2.0))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMolecularTests(priorTests)))

        // Also works for score texts.
        val otherPriorTests = listOf(ihcTest(scoreText = "positive"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMolecularTests(otherPriorTests)))
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null): MolecularTest {
        return IHCMolecularTest(
            MolecularTestFactory.priorMolecularTest(
                test = IHC, item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText
            )
        )
    }*/
}