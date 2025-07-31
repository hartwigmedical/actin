package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MEASURE = "measure"

class HasSufficientPDL1ByIhcTest {
    private val minPdl1 = 2.0
    private val function = HasSufficientPDL1ByIhc(MEASURE, minPdl1)
    private val pdl1Test = MolecularTestFactory.ihcTest(item = "PD-L1", measure = MEASURE)

    @Test
    fun `Should pass when test value is above min`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = minPdl1.plus(0.5)))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass when test value is equal to minimum value`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = minPdl1))
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when it is unclear if test value is above minimum due to its comparator`() {
        val record =
            MolecularTestFactory.withIhcTests(
                pdl1Test.copy(
                    scoreValue = minPdl1.plus(1.0),
                    scoreValuePrefix = ValueComparison.SMALLER_THAN
                )
            )
        val evaluation = function.evaluate(record)
        assertMolecularEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly(
            "Undetermined if PD-L1 expression (< ${minPdl1.plus(1.0)}) above minimum of 2.0"
        )
    }

    @Test
    fun `Should fail when test value is below minimum value`() {
        val record = MolecularTestFactory.withIhcTests(pdl1Test.copy(scoreValue = minPdl1.minus(1.0)))
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }
}