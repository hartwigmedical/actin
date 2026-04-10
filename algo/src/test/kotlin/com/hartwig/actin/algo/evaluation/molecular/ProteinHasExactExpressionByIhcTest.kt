package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

private const val PROTEIN = "protein 1"

class ProteinHasExactExpressionByIhcTest {

    private val function = ProteinHasExactExpressionByIhc(PROTEIN, 2)

    @Test
    fun `Should fail when there are no prior tests`() {
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(emptyList())))
    }

    @Test
    fun `Should fail when no prior test contains results`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(ihcTest())))
    }

    @Test
    fun `Should fail when prior test contains result that is too low`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = 1.0, scoreUpperBound = 1.0))))
    }

    @Test
    fun `Should fail when prior test contains result that is too high`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = 3.0, scoreUpperBound = 3.0))))
    }

    @Test
    fun `Should fail when prior test contains value with only lower bound (not exact)`() {
        val priorTest = ihcTest(scoreLowerBound = 2.0)
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(priorTest)))
    }

    @Test
    fun `Should warn when prior test contains unclear result`() {
        val priorTest = ihcTest(scoreText = "Positive")
        assertMolecularEvaluation(EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withIhcTests(priorTest)))
    }

    @Test
    fun `Should fail when prior test contains differing bounds spanning reference value`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = 1.0, scoreUpperBound = 3.0))))
    }

    @Test
    fun `Should pass when prior test contains exact result`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = 2.0, scoreUpperBound = 2.0))))
    }

    private fun ihcTest(scoreLowerBound: Double? = null, scoreUpperBound: Double? = null, scoreText: String? = null) =
        MolecularTestFactory.ihcTest(
            item = PROTEIN, scoreLowerBound = scoreLowerBound, scoreUpperBound = scoreUpperBound, scoreText = scoreText
        )
}
