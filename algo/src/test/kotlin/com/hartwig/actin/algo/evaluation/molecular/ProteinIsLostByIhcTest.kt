package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.IhcTestEvaluationConstants
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.ihcTest
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

private const val PROTEIN = "protein 1"

class ProteinIsLostByIhcTest {

    private val function = ProteinIsLostByIhc(PROTEIN)
    private val passingTest = ihcTest(item = PROTEIN, scoreText = IhcTestEvaluationConstants.BROAD_NEGATIVE_TERMS.first())
    private val wrongTest = ihcTest(item = PROTEIN, scoreText = IhcTestEvaluationConstants.BROAD_POSITIVE_TERMS.first())
    private val inconclusiveTest = ihcTest(item = PROTEIN, scoreText = "something")

    @Test
    fun `Should be undetermined if there is an empty list`() {
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIhcTests(emptyList())))
    }

    @Test
    fun `Should be undetermined if there are no tests for protein`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withIhcTests(ihcTest(item = "Other protein", scoreText = "loss")))
        )
    }

    @Test
    fun `Should pass if all tests would pass`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIhcTests(listOf(passingTest))))
    }

    @Test
    fun `Should warn if there is at least one test with passing result`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withIhcTests(listOf(passingTest, inconclusiveTest)))
        )
    }

    @Test
    fun `Should warn if there is at least one test with inconclusive result`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withIhcTests(listOf(wrongTest, inconclusiveTest)))
        )
    }

    @Test
    fun `Should fail if there are only tests with failing result`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(wrongTest, wrongTest)))
    }
}