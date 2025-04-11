package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val IHC = "IHC"
private const val PROTEIN = "protein 1"
private const val GENE = "gene 1"

class ProteinIsExpressedByIHCTest {
    private val function = ProteinIsExpressedByIHC(PROTEIN, GENE)

    @Test
    fun `Should evaluate to undetermined when no IHC tests present in record`() {
        val priorTests = mutableListOf<PriorIHCTest>()
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(priorTests)))
    }

    @Test
    fun `Should evaluate to undetermined if there are no tests for protein but gene is wild type in panel`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withIHCTests(emptyList()).copy(
                molecularHistory = MolecularHistory(
                    listOf(
                        TestMolecularFactory.createMinimalTestPanelRecord()
                            .copy(geneCoverage = TestMolecularFactory.panelSpecifications(setOf(GENE)))
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessages).containsExactly("No $PROTEIN IHC test result though $GENE is wild-type in recent molecular test")
    }

    @Test
    fun `Should fail when only correct IHC test has no value`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(ihcTest())))
    }

    @Test
    fun `Should fail when test has negative result`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(ihcTest(scoreText = "negative"))))
    }

    @Test
    fun `Should pass when test has positive result`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIHCTests(ihcTest(scoreValue = 2.0))))
    }

    @Test
    fun `Should pass when score text is positive `() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withIHCTests(listOf(ihcTest(scoreText = "positive"))))
        )
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null) =
        MolecularTestFactory.priorIHCTest(
            test = IHC, item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText
        )
}