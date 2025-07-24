package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.IhcTestEvaluationConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.ihcTest
import org.junit.Test

private const val PROTEIN = "protein 1"

class ProteinIsExpressedByIhcTest {

    private val function = ProteinIsExpressedByIhc(PROTEIN)
    private val passingTest = ihcTest(item = PROTEIN, scoreText = IhcTestEvaluationConstants.EXACT_POSITIVE_TERMS.first())
    private val wrongTest = ihcTest(item = PROTEIN, scoreText = IhcTestEvaluationConstants.EXACT_NEGATIVE_TERMS.first())
    private val inconclusiveTest = ihcTest(item = PROTEIN, scoreText = "something")

    @Test
    fun `Should be undetermined if there is an empty list`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIhcTests(emptyList())))
    }

    @Test
    fun `Should be undetermined if there are no tests for protein`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withIhcTests(ihcTest(item = "Other protein", scoreText = "loss")))
        )
    }

    @Test
    fun `Should pass if all tests would pass`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIhcTests(listOf(passingTest))))
    }

    @Test
    fun `Should warn if there is at least one test with passing result`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withIhcTests(listOf(passingTest, inconclusiveTest))))
    }

    @Test
    fun `Should warn if there is at least one test with inconclusive result`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withIhcTests(listOf(wrongTest, inconclusiveTest)))
        )
    }

    @Test
    fun `Should fail if there are only tests with failing result`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(wrongTest, wrongTest)))
    }
}