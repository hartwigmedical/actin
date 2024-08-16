package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MEASURE = "measure"
private const val MAX_PDL1 = 2.0

class HasLimitedPDL1ByIHCTest {
    private val function = HasLimitedPDL1ByIHC(MEASURE, MAX_PDL1)
    private val pdl1Test = MolecularTestFactory.priorIHCTest(test = "IHC", item = "PD-L1", measure = MEASURE)

    @Test
    fun `Should pass when test value is below max`() {
        val record =
            MolecularTestFactory.withIHCTests(pdl1Test.copy(scoreValue = MAX_PDL1.minus(0.5)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass when test value is equal to maximum value`() {
        val record =
            MolecularTestFactory.withIHCTests(pdl1Test.copy(scoreValue = MAX_PDL1))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when it is unclear if test value is below maximum due to its comparator`() {
        val record =
            MolecularTestFactory.withIHCTests(
                pdl1Test.copy(
                    scoreValue = MAX_PDL1.minus(1.0),
                    scoreValuePrefix = ValueComparison.LARGER_THAN
                )
            )
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly(
            "Undetermined if PD-L1 expression (> ${MAX_PDL1.minus(1.0)}) below maximum of 2.0"
        )
    }

    @Test
    fun `Should fail when test value is above maximum value`() {
        val record =
            MolecularTestFactory.withIHCTests(pdl1Test.copy(scoreValue = MAX_PDL1.plus(1.0)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }
}