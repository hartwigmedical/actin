package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

private const val PROTEIN = "protein 1"

class ProteinIsWildTypeByIHCTest {
    private val function = ProteinIsWildTypeByIHC(PROTEIN)

    @Test
    fun shouldReturnUndeterminedForEmptyListOfTests() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(emptyList())))
    }

    @Test
    fun shouldReturnUndeterminedForTestsThatDoNotMeetCriteria() {
        val priorTests = listOf(ihcTest(item = "other"), ihcTest(item = "other 2"))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(priorTests)))
    }

    @Test
    fun shouldReturnPassWhenAllMatchingTestsIndicateWildType() {
        val priorTests = listOf(
            ihcTest(item = "other"),
            ihcTest(item = "other2"),
            ihcTest(),
            ihcTest(scoreText = "WILD TYPE"),
            ihcTest(scoreText = "WILD-type")
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withIHCTests(priorTests)))
    }

    @Test
    fun shouldReturnUndeterminedWhenSomeMatchingTestsDoNotIndicateWildType() {
        val priorTests = listOf(
            ihcTest(),
            ihcTest(scoreText = "WILD TYPE"),
            ihcTest(scoreText = "WILD-type"),
            ihcTest(scoreText = "other")
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withIHCTests(priorTests)))
    }

    private fun ihcTest(item: String = PROTEIN, scoreText: String? = "WildType") =
        MolecularTestFactory.ihcTest(item = item, scoreText = scoreText)
}