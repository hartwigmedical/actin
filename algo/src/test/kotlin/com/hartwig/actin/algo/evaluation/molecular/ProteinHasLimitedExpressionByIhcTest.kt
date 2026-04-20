package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.ihcTest
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
        val test = ihcTest(item = "other", scoreLowerBound = 1.0, scoreUpperBound = 1.0)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIhcTests(test)))
    }

    @Test
    fun `Should warn when only score text is provided and exact value is unclear`() {
        val test = ihcTest(scoreText = "negative")
        assertMolecularEvaluation(EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withIhcTests(test)))
    }

    @Test
    fun `Should fail when only correct IHC test in record has no value`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withIhcTests(ihcTest())))
    }

    @Test
    fun `Should pass when ihc test below requested value`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = REFERENCE.minus(1.0), scoreUpperBound = REFERENCE.minus(1.0)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass when ihc test at exact requested value`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = REFERENCE.toDouble(), scoreUpperBound = REFERENCE.toDouble()))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass when only upper bound is set and below requested value`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreUpperBound = REFERENCE.minus(1.0)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should fail when only lower bound is set and above requested value`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = REFERENCE.plus(1.0)))
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail when both bounds are above requested value with differing bounds`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = REFERENCE.plus(1.0), scoreUpperBound = REFERENCE.plus(2.0)))
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should warn when unclear if below requested value due to bounds`() {
        val record = MolecularTestFactory.withIhcTests(ihcTest(scoreLowerBound = REFERENCE.minus(1).toDouble()))
        assertMolecularEvaluation(EvaluationResult.WARN, function.evaluate(record))
    }

    private fun ihcTest(scoreLowerBound: Double? = null, scoreUpperBound: Double? = null, scoreText: String? = null) =
        ihcTest(
            item = PROTEIN, scoreLowerBound = scoreLowerBound, scoreUpperBound = scoreUpperBound, scoreText = scoreText
        )
}
