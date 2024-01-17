package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.junit.Test

class ProteinHasExactExpressionByIHCTest {
    private val protein = "protein 1"
    private val function = ProteinHasExactExpressionByIHC(protein, 2)

    @Test
    fun `Should fail when there are no prior tests`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withPriorTests(emptyList())))
    }

    @Test
    fun `Should fail when no prior test contains results`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTest(ihcTest())))
    }

    @Test
    fun `Should fail when prior test contains result that is too low`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTest(ihcTest(scoreValue = 1.0))))
    }

    @Test
    fun `Should fail when prior test contains result that is too high`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTest(ihcTest(scoreValue = 3.0))))
    }

    @Test
    fun `Should fail when prior test contains exact result with prefix`() {
        val priorTest = ihcTest(scoreValuePrefix = ">", scoreValue = 2.0)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTest(priorTest)))
    }

    @Test
    fun `Should fail when prior test contains unclear result`() {
        val priorTest = ihcTest(scoreText = "Positive")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withPriorTest(priorTest)))
    }

    @Test
    fun `Should pass when prior test contains exact result`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorTest(ihcTest(scoreValue = 2.0))))
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null): PriorMolecularTest {
        return MolecularTestFactory.priorMolecularTest(
            test = "IHC", item = protein, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText
        )
    }
}