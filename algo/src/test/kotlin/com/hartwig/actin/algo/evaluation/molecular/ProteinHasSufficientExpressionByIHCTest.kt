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
private const val REFERENCE = 2

class ProteinHasSufficientExpressionByIHCTest {

    private val function = ProteinHasSufficientExpressionByIHC(PROTEIN, GENE, REFERENCE)

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
    fun `Should evaluate to undetermined when no IHC test of correct protein present in record`() {
        val test = priorIHCTest(test = IHC, item = "other", scoreValue = 1.0)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(test)))
    }

    @Test
    fun `Should evaluate to undetermined when only score text is provided and exact value is unclear`() {
        val test = priorIHCTest(scoreText = "negative")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(test)))
    }

    @Test
    fun `Should fail when only correct IHC test in record has no value`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(ihcTest())))
    }

    @Test
    fun `Should pass when ihc test above requested value`() {
        val record = MolecularTestFactory.withIHCTests(ihcTest(scoreValue = REFERENCE.plus(1.0)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when unclear if above requested value due to comparator`() {
        val test = priorIHCTest(scoreValue = REFERENCE.minus(1).toDouble(), scoreValuePrefix = ValueComparison.LARGER_THAN)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(test)))
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null) =
        priorIHCTest(
            test = IHC, item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText
        )
}