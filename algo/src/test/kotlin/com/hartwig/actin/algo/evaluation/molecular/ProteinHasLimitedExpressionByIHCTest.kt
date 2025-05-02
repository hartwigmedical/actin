package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.ihcTest
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

private const val PROTEIN = "protein 1"
private const val REFERENCE = 2

class ProteinHasLimitedExpressionByIHCTest {

    private val function = ProteinHasLimitedExpressionByIHC(PROTEIN, REFERENCE)

    @Test
    fun `Should evaluate to undetermined when no IHC tests present in record`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(emptyList())))
    }

    @Test
    fun `Should evaluate to undetermined when no IHC test of correct protein present in record`() {
        val test = ihcTest(item = "other", scoreValue = 1.0)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(test)))
    }

    @Test
    fun `Should evaluate to undetermined when only score text is provided and exact value is unclear`() {
        val test = MolecularTestFactory.ihcTest(scoreText = "negative")
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(test)))
    }

    @Test
    fun `Should fail when only correct IHC test in record has no value`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIHCTests(ihcTest())))
    }

    @Test
    fun `Should pass when ihc test below requested value`() {
        val record = MolecularTestFactory.withIHCTests(ihcTest(scoreValue = REFERENCE.minus(1.0)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when unclear if above requested value due to comparator`() {
        val test = MolecularTestFactory.ihcTest(scoreValue = REFERENCE.minus(1).toDouble(), scoreValuePrefix = ValueComparison.LARGER_THAN)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(test)))
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null) =
        ihcTest(
            item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText
        )
}