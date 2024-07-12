package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import org.junit.Test

private const val MEASURE = "measure"

class HasLimitedPDL1ByIHCTest {
    private val function = HasLimitedPDL1ByIHC(MEASURE, 2.0)
    private val pdl1Test = MolecularTestFactory.priorMolecularTest(test = "IHC", item = "PD-L1", measure = MEASURE)

    @Test
    fun `Should fail when test value is too high`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withMolecularTests(listOf(IHCMolecularTest(pdl1Test.copy(scoreValue = 3.0)))))
        )
    }
}