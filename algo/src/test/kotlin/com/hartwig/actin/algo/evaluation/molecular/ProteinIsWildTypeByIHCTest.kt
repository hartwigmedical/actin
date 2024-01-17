package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import org.junit.Test

class ProteinIsWildTypeByIHCTest {
    @Test
    fun shouldReturnUndeterminedForEmptyListOfTests() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(MolecularTestFactory.withPriorTests(emptyList())))
    }

    @Test
    fun shouldReturnUndeterminedForTestsThatDoNotMeetCriteria() {
        val priorTests = listOf(builder().test("other").build(), builder().item("other").build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(MolecularTestFactory.withPriorTests(priorTests)))
    }

    @Test
    fun shouldReturnPassWhenAllMatchingTestsIndicateWildType() {
        val priorTests = listOf(
            builder().test("other").build(),
            builder().item("other").build(),
            builder().build(),
            builder().scoreText("WILD TYPE").build(),
            builder().scoreText("WILD-type").build()
        )
        assertEvaluation(EvaluationResult.PASS, function().evaluate(MolecularTestFactory.withPriorTests(priorTests)))
    }

    @Test
    fun shouldReturnUndeterminedWhenSomeMatchingTestsDoNotIndicateWildType() {
        val priorTests = listOf(
            builder().build(),
            builder().scoreText("WILD TYPE").build(),
            builder().scoreText("WILD-type").build(),
            builder().scoreText("other").build()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(MolecularTestFactory.withPriorTests(priorTests)))
    }

    companion object {
        private const val PROTEIN = "p53"
        private fun function(): ProteinIsWildTypeByIHC {
            return ProteinIsWildTypeByIHC(PROTEIN)
        }

        private fun builder(): ImmutablePriorMolecularTest.Builder {
            return MolecularTestFactory.priorMolecularTest().test("IHC").item(PROTEIN).scoreText("WildType")
        }
    }
}