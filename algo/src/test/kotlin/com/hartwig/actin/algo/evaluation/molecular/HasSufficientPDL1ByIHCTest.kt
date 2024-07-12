package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import org.assertj.core.api.Assertions
import org.junit.Test

private const val MEASURE = "measure"

class HasSufficientPDL1ByIHCTest {
    private val minPdl1 = 2.0
    private val function = HasSufficientPDL1ByIHC(MEASURE, minPdl1)
    private val pdl1Test = MolecularTestFactory.priorMolecularTest(test = "IHC", item = "PD-L1", measure = MEASURE)

    @Test
    fun `Should fail when test value is too low`() {
        val evaluation = function.evaluate(MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = 1.0)))))
        assertEvaluation(
            EvaluationResult.FAIL,
            evaluation
        )
        Assertions.assertThat(evaluation.failGeneralMessages).containsExactly("PD-L1 expression below $minPdl1")
    }
}