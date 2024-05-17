package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import org.junit.Test

private const val IHC = "IHC"
private const val PROTEIN = "protein 1"

class ProteinIsWildTypeByIHCTest {
    private val function = ProteinIsWildTypeByIHC(PROTEIN)

    @Test
    fun shouldReturnUndeterminedForEmptyListOfTests() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(emptyList())))
    }

    @Test
    fun shouldReturnUndeterminedForTestsThatDoNotMeetCriteria() {
        val priorTests = listOf(ihcTest(test = "other"), ihcTest(item = "other"))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(priorTests)))
    }

    @Test
    fun shouldReturnPassWhenAllMatchingTestsIndicateWildType() {
        val priorTests = listOf(
            ihcTest(test = "other"),
            ihcTest(item = "other"),
            ihcTest(),
            ihcTest(scoreText = "WILD TYPE"),
            ihcTest(scoreText = "WILD-type")
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMolecularTests(priorTests)))
    }

    @Test
    fun shouldReturnUndeterminedWhenSomeMatchingTestsDoNotIndicateWildType() {
        val priorTests = listOf(
            ihcTest(),
            ihcTest(scoreText = "WILD TYPE"),
            ihcTest(scoreText = "WILD-type"),
            ihcTest(scoreText = "other")
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMolecularTests(priorTests)))
    }

    private fun ihcTest(test: String = IHC, item: String = PROTEIN, scoreText: String? = "WildType"): IHCMolecularTest {
        return IHCMolecularTest((MolecularTestFactory.priorMolecularTest(test = test, item = item, scoreText = scoreText)))
    }
}