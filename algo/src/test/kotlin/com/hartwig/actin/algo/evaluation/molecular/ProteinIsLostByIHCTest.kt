package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.priorIHCTest
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

private const val IHC = "IHC"
private const val PROTEIN = "protein 1"

class ProteinIsLostByIHCTest {

    private val function = ProteinIsLostByIHC(PROTEIN)
    private val passingTest = priorIHCTest(test = IHC, item = PROTEIN, scoreText = "loss")
    private val wrongTest = priorIHCTest(test = IHC, item = PROTEIN, scoreText = "no loss")
    private val inconclusiveTest = priorIHCTest(test = IHC, item = PROTEIN, scoreText = "something")

    @Test
    fun `Should be undetermined if there is an empty list`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(emptyList())))
    }

    @Test
    fun `Should be undetermined if there are no tests for protein`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withIHCTests(priorIHCTest(test = IHC, item = "No protein", scoreText = "loss")))
        )
    }

    @Test
    fun `Should pass if there is at least one test with 'loss' result`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIHCTests(listOf(passingTest, inconclusiveTest))))
    }

    @Test
    fun `Should be undetermined if there is at least one test with inconclusive result`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withIHCTests(listOf(wrongTest, inconclusiveTest)))
        )
    }

    @Test
    fun `Should fail if there is one test with 'no loss' result`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(wrongTest)))
    }
}