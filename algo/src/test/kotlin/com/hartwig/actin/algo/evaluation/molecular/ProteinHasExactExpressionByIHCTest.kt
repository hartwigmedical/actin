package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.priorIHCTest
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val IHC = "IHC"
private const val PROTEIN = "protein 1"
private const val GENE = "gene 1"

class ProteinHasExactExpressionByIHCTest {
    private val function = ProteinHasExactExpressionByIHC(PROTEIN, GENE, 2)

    @Test
    fun `Should fail when there are no prior tests`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(emptyList())))
    }

    @Test
    fun `Should fail when no prior test contains results`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(ihcTest())))
    }

    @Test
    fun `Should fail when prior test contains result that is too low`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(ihcTest(scoreValue = 1.0))))
    }

    @Test
    fun `Should fail when prior test contains result that is too high`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(ihcTest(scoreValue = 3.0))))
    }

    @Test
    fun `Should fail when prior test contains exact result with prefix`() {
        val priorTest = ihcTest(scoreValuePrefix = ValueComparison.LARGER_THAN, scoreValue = 2.0)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(priorTest)))
    }

    @Test
    fun `Should fail when prior test contains unclear result`() {
        val priorTest = ihcTest(scoreText = "Positive")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(priorTest)))
    }

    @Test
    fun `Should evaluate to undetermined when no IHC tests present in record`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(emptyList())))
    }

    @Test
    fun `Should evaluate to undetermined if there are no tests for protein but gene is wild type in panel`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIHCTests(emptyList()).copy(
                molecularHistory = MolecularHistory(
                    listOf(
                        TestMolecularFactory.createMinimalTestPanelRecord().copy(testedGenes = setOf(GENE))
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("No $PROTEIN IHC test result though $GENE is wild-type in recent molecular test")
    }

    @Test
    fun `Should pass when prior test contains exact result`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIHCTests(ihcTest(scoreValue = 2.0))))
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null) =
        priorIHCTest(
            test = IHC, item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText
        )
}