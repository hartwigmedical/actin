package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MEASURE = "measure"

class HasSufficientPDL1ByIHCTest {
    private val minPdl1 = 2.0
    private val function = HasSufficientPDL1ByIHC(MEASURE, minPdl1)
    private val pdl1Test = MolecularTestFactory.priorIHCTest(test = "IHC", item = "PD-L1", measure = MEASURE)

    @Test
    fun `Should pass when test value is above min`() {
        val record =
            MolecularTestFactory.withIHCTests(pdl1Test.copy(scoreValue = minPdl1.plus(0.5)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass when test value is equal to minimum value`() {
        val record =
            MolecularTestFactory.withIHCTests(pdl1Test.copy(scoreValue = minPdl1))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when it is unclear if test value is above minimum due to its comparator`() {
        val record =
            MolecularTestFactory.withIHCTests(
                pdl1Test.copy(
                    scoreValue = minPdl1.plus(1.0),
                    scoreValuePrefix = ValueComparison.SMALLER_THAN
                )
            )
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly(
            "Undetermined if PD-L1 expression (< ${minPdl1.plus(1.0)}) above minimum of 2.0"
        )
    }

    @Test
    fun `Should fail when test value is below minimum value`() {
        val record =
            MolecularTestFactory.withIHCTests(pdl1Test.copy(scoreValue = minPdl1.minus(1.0)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }
}