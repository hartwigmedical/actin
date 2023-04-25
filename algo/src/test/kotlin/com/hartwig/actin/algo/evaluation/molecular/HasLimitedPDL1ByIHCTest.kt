package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.util.ValueComparison
import com.hartwig.actin.clinical.datamodel.ImmutablePriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.junit.Test

class HasLimitedPDL1ByIHCTest {
    @Test
    fun canEvaluate() {
        val function = HasLimitedPDL1ByIHC(MEASURE, 2.0)

        // No prior tests
        val priorTests = mutableListOf<PriorMolecularTest>()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with no result
        priorTests.add(pdl1Builder().build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with value too high
        priorTests.add(pdl1Builder().scoreValue(3.0).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with right value but wrong prefix
        priorTests.add(pdl1Builder().scoreValuePrefix(ValueComparison.LARGER_THAN).scoreValue(1.0).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)))

        // Add test with right value
        priorTests.add(pdl1Builder().scoreValue(1.0).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withPriorTests(priorTests)))
    }

    companion object {
        private const val MEASURE = "measure"
        private fun pdl1Builder(): ImmutablePriorMolecularTest.Builder {
            return MolecularTestFactory.priorBuilder().test("IHC").item("PD-L1").measure(MEASURE)
        }
    }
}