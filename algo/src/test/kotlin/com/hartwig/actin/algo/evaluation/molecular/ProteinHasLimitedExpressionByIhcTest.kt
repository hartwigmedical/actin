package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.ihcTest
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

private const val PROTEIN = "protein 1"
private const val REFERENCE = 2

class ProteinHasLimitedExpressionByIhcTest {

    private val function = ProteinHasLimitedExpressionByIhc(PROTEIN, REFERENCE)

    @Test
    fun `Should evaluate to undetermined when no IHC tests present in record`() {
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIhcTests(emptyList())))
    }

    @Test
    fun `Should evaluate to undetermined when no IHC test of correct protein present in record`() {
        val test = ihcTest(item = "other", scoreValue = 1.0)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIhcTests(test)))
    }

    @Test
    fun `Should evaluate to undetermined when only score text is provided and exact value is unclear`() {
        val test = MolecularTestFactory.ihcTest(scoreText = "negative")
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIhcTests(test)))
    }

    @Test
    fun `Should fail when only correct IHC test in record has no value`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(ihcTest())))
    }

    @Test
    fun `Should pass when ihc test below requested value`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreValue = REFERENCE.minus(1.0)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when unclear if above requested value due to comparator`() {
        val test = MolecularTestFactory.ihcTest(scoreValue = REFERENCE.minus(1).toDouble(), scoreValuePrefix = ValueComparison.LARGER_THAN)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIhcTests(test)))
    }

    private fun ihcTest(scoreValue: Double? = null, scoreValuePrefix: String? = null, scoreText: String? = null) =
        ihcTest(
            item = PROTEIN, scoreValue = scoreValue, scoreValuePrefix = scoreValuePrefix, scoreText = scoreText
        )
}