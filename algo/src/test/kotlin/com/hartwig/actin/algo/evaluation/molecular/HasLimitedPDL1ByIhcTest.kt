package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val MEASURE = "measure"
private const val MAX_PDL1 = 2.0

class HasLimitedPDL1ByIhcTest {

    private val function = HasLimitedPDL1ByIhc(MEASURE, MAX_PDL1)
    private val pdl1Test = MolecularTestFactory.ihcTest(item = "PD-L1", measure = MEASURE)

    @Test
    fun `Should pass when test value is below max`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreLowerBound = MAX_PDL1.minus(0.5), scoreUpperBound = MAX_PDL1.minus(0.5)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass when test value is equal to maximum value`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreLowerBound = MAX_PDL1, scoreUpperBound = MAX_PDL1))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when it is unclear if test value is below maximum due to bounds`() {
        val record =
            MolecularTestFactory.withIhcTests(
                pdl1Test.copy(scoreLowerBound = MAX_PDL1.minus(1.0))
            )
        val evaluation = function.evaluate(record)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly(
            "Undetermined if PD-L1 expression (>= ${MAX_PDL1.minus(1.0)}) below maximum of 2.0"
        )
    }

    @Test
    fun `Should pass when only upper bound is set and below maximum value`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreLowerBound = null, scoreUpperBound = MAX_PDL1.minus(0.5)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should fail when only lower bound is set and above maximum value`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreLowerBound = MAX_PDL1.plus(1.0), scoreUpperBound = null))
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail when test value is above maximum value`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreLowerBound = MAX_PDL1.plus(1.0), scoreUpperBound = MAX_PDL1.plus(1.0)))
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail when both bounds are above maximum value with differing bounds`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreLowerBound = MAX_PDL1.plus(0.5), scoreUpperBound = MAX_PDL1.plus(1.5)))
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when range crosses maximum value`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreLowerBound = MAX_PDL1.minus(1.0), scoreUpperBound = MAX_PDL1.plus(1.0)))
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }
}
